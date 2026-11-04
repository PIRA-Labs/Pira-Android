package com.taak.watch.signedtest

import org.json.JSONObject
import java.util.UUID

/** Only this schema can turn a provider response into a physical-action proposal. */
data class TtsProposal(val text: String, val requestId: String = UUID.randomUUID().toString()) {
    fun asAction() = ActionProposal(Capability.TEXT_TO_SPEECH, parameters = mapOf("text" to text))
}

object AgentActionParser {
    /** Reject prose and malformed JSON. A model response is never an executable command by itself. */
    fun parseTtsProposal(raw: String): TtsProposal? = try {
        val root = JSONObject(raw)
        parse(root) ?: root.optJSONArray("choices")?.let { choices ->
            (0 until choices.length()).firstNotNullOfOrNull { index ->
                choices.optJSONObject(index)?.optJSONObject("message")?.optString("content")?.let(::parseTtsProposal)
            }
        }
    } catch (_: Exception) { null }

    private fun parse(root: JSONObject): TtsProposal? {
        val action = root.optJSONObject("pira_action") ?: root
        if (action.optString("capability") != "TEXT_TO_SPEECH") return null
        val text = action.optString("text").trim()
        return if (text.isBlank() || text.length > 500) null else TtsProposal(text)
    }
}

/** Claim removes a grant before execution, so a second execution cannot reuse it. */
interface OneTimeGrantStore {
    fun issue(proposal: ActionProposal, nowMs: Long = System.currentTimeMillis()): Authorization
    fun claim(nonce: String, nowMs: Long = System.currentTimeMillis()): Authorization?
}

class InMemoryGrantStore(private val ttlMs: Long = 60_000L) : OneTimeGrantStore {
    private val grants = mutableMapOf<String, Authorization>()
    override fun issue(proposal: ActionProposal, nowMs: Long): Authorization = synchronized(grants) {
        val grant = Authorization(proposal.capability, nowMs + ttlMs, UUID.randomUUID().toString(), proposal.parameters)
        grants[grant.nonce] = grant
        grant
    }
    override fun claim(nonce: String, nowMs: Long): Authorization? = synchronized(grants) {
        val grant = grants.remove(nonce) ?: return null
        if (grant.expiresAtMs <= nowMs) null else grant
    }
}
