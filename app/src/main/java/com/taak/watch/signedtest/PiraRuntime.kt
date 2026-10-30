package com.taak.watch.signedtest

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

enum class Capability { CAMERA_LOOK, BATTERY_READ, WEARABLE_MESSAGE, WEB_SEARCH }

data class ActionProposal(val capability: Capability, val target: String? = null, val parameters: Map<String, String> = emptyMap())
data class Authorization(val capability: Capability, val expiresAtMs: Long, val nonce: String, val parameters: Map<String, String> = emptyMap(), val used: Boolean = false)
data class Decision(val allowed: Boolean, val reason: String)

class PolicyEngine(private val emergencyStop: () -> Boolean = { false }) {
    fun evaluate(proposal: ActionProposal, authorization: Authorization?, nowMs: Long = System.currentTimeMillis()): Decision {
        if (proposal.capability !in setOf(Capability.CAMERA_LOOK, Capability.BATTERY_READ, Capability.WEARABLE_MESSAGE, Capability.WEB_SEARCH))
            return Decision(false, "unsupported capability")
        if (emergencyStop()) return Decision(false, "emergency stop active")
        if (authorization == null) return Decision(false, "missing authorization")
        if (authorization.used) return Decision(false, "authorization already used")
        if (authorization.expiresAtMs <= nowMs) return Decision(false, "authorization expired")
        if (authorization.capability != proposal.capability) return Decision(false, "authorization scope mismatch")
        if (authorization.parameters != proposal.parameters) return Decision(false, "authorization parameters mismatch")
        if (proposal.parameters.keys.any { it !in setOf("prompt", "message") }) return Decision(false, "unknown parameter")
        if (proposal.capability == Capability.WEARABLE_MESSAGE && proposal.parameters["message"].orEmpty().length > 120)
            return Decision(false, "message exceeds allowlist")
        return Decision(true, "authorized")
    }
}

class AuditLogger(context: Context) {
    private val prefs = context.getSharedPreferences("pira_audit", Context.MODE_PRIVATE)
    fun record(event: String) { prefs.edit().putString("last", "${System.currentTimeMillis()}: $event").apply() }
    fun last(): String = prefs.getString("last", "No activity yet") ?: "No activity yet"
}

class SecureKeyStore(private val alias: String = "pira-nebius-key") {
    fun clear() { KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(alias) }
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (store.containsAlias(alias)) return (store.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP)
    }
    fun decrypt(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), javax.crypto.spec.GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), StandardCharsets.UTF_8)
    }
}

class NebiusClient(private val context: Context) {
    private val prefs = context.getSharedPreferences("pira_config", Context.MODE_PRIVATE)
    private val keyStore = SecureKeyStore("pira-nebius-key")
    fun saveKey(value: String) { prefs.edit().putString("key", keyStore.encrypt(value)).apply() }
    fun clear() { prefs.edit().clear().apply(); keyStore.clear() }
    fun hasKey() = !prefs.getString("key", null).isNullOrBlank()
    private fun key() = keyStore.decrypt(prefs.getString("key", "") ?: "")
    fun models(): String = request("GET", "/models", null)
    fun chat(model: String, prompt: String): String {
        val body = "{\"model\":\"${json(model)}\",\"messages\":[{\"role\":\"user\",\"content\":\"${json(prompt)}\"}],\"max_tokens\":300}"
        return request("POST", "/chat/completions", body)
    }
    private fun request(method: String, path: String, body: String?): String {
        val connection = java.net.URL("https://api.tokenfactory.nebius.com/v1$path").openConnection() as java.net.HttpURLConnection
        connection.requestMethod = method; connection.setRequestProperty("Authorization", "Bearer ${key()}"); connection.setRequestProperty("Content-Type", "application/json"); connection.connectTimeout = 15_000; connection.readTimeout = 30_000
        if (body != null) { connection.doOutput = true; connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) } }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        return stream.bufferedReader().use { it.readText() }.also { connection.disconnect() }
    }
    private fun json(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}

data class WebSearchProposal(val query: String, val purpose: String, val requestId: String, val scope: String = "tavily.basic")
data class WebSearchAuthorization(val query: String, val purpose: String, val requestId: String, val expiresAtMs: Long, val used: Boolean = false)
data class WebSearchResult(val title: String, val url: String, val snippet: String)

class TavilyClient(private val context: Context) {
    private val prefs = context.getSharedPreferences("pira_tavily_config", Context.MODE_PRIVATE)
    private val keyStore = SecureKeyStore("pira-tavily-key")
    fun saveKey(value: String) { prefs.edit().putString("key", keyStore.encrypt(value)).apply() }
    fun clear() { prefs.edit().clear().apply(); keyStore.clear() }
    fun hasKey() = !prefs.getString("key", null).isNullOrBlank()
    private fun key() = keyStore.decrypt(prefs.getString("key", "") ?: "")
    fun search(proposal: WebSearchProposal, authorization: WebSearchAuthorization): List<WebSearchResult> {
        require(authorization.query == proposal.query && authorization.purpose == proposal.purpose && authorization.requestId == proposal.requestId) { "search authorization mismatch" }
        require(!authorization.used && authorization.expiresAtMs > System.currentTimeMillis()) { "search authorization expired or used" }
        require(proposal.query.isNotBlank() && proposal.query.length <= 240) { "invalid search query" }
        val body = "{\"query\":\"${json(proposal.query)}\",\"search_depth\":\"basic\",\"max_results\":5}"
        val connection = java.net.URL("https://api.tavily.com/search").openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"; connection.setRequestProperty("Authorization", "Bearer ${key()}"); connection.setRequestProperty("Content-Type", "application/json"); connection.connectTimeout = 15_000; connection.readTimeout = 30_000; connection.doOutput = true
        connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val raw = stream.bufferedReader().use { it.readText() }; connection.disconnect()
        if (raw.isBlank() || !raw.contains("results")) throw IllegalStateException("Tavily search failed (${raw.take(120)})")
        return Regex("\\\"title\\\"\\s*:\\s*\\\"(.*?)\\\".*?\\\"url\\\"\\s*:\\s*\\\"(.*?)\\\".*?\\\"content\\\"\\s*:\\s*\\\"(.*?)\\\"", RegexOption.DOT_MATCHES_ALL).findAll(raw).take(5).map { WebSearchResult(unescape(it.groupValues[1]), unescape(it.groupValues[2]), unescape(it.groupValues[3]).take(240)) }.toList()
    }
    private fun json(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    private fun unescape(value: String) = value.replace("\\\"", "\"").replace("\\n", " ").replace("\\u0026", "&")
}
