package com.taak.watch.signedtest

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.InputType
import android.view.View
import android.graphics.Color
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.LinearLayout
import org.json.JSONArray
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val cameraRequest = 41
    private val voiceRequest = 77
    private lateinit var status: TextView
    private lateinit var audit: TextView
    private lateinit var keyInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var promptInput: EditText
    private lateinit var tavilyKeyInput: EditText
    private lateinit var searchInput: EditText
    private lateinit var chatTranscript: TextView
    private lateinit var visionTranscript: TextView
    private lateinit var eyesQuestion: EditText
    private var latestPhoto: Bitmap? = null
    private var selectedModelId: String? = null
    private val policy by lazy { PolicyEngine { getSharedPreferences("pira_security", MODE_PRIVATE).getBoolean("emergency_stop", false) } }
    private val logger by lazy { AuditLogger(this) }
    private val nebius by lazy { NebiusClient(this) }
    private val tavily by lazy { TavilyClient(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        status = findViewById(R.id.status); audit = findViewById(R.id.audit); keyInput = findViewById(R.id.nebiusKey)
        modelInput = findViewById(R.id.model); promptInput = findViewById(R.id.askPrompt)
        getSharedPreferences("pira_config", MODE_PRIVATE).getString("model", null)?.let { selectedModelId = it; modelInput.setText(humanModelName(it)) }
        tavilyKeyInput = findViewById(R.id.tavilyKey); searchInput = findViewById(R.id.searchQuery)
        chatTranscript = findViewById(R.id.chatTranscript); visionTranscript = findViewById(R.id.visionTranscript)
        eyesQuestion = findViewById(R.id.eyesQuestion)
        findViewById<Button>(R.id.myBrain).setOnClickListener { showPanel(R.id.brainPanel); showBrainMode(R.id.askContent) }
        findViewById<Button>(R.id.myEyes).setOnClickListener { showPanel(R.id.eyesPanel) }
        findViewById<Button>(R.id.mySafety).setOnClickListener { showPanel(R.id.safetyPanel) }
        findViewById<Button>(R.id.myVoice).setOnClickListener { status.text = "My voice is coming soon — nothing is enabled yet" }
        findViewById<Button>(R.id.askMode).setOnClickListener { showBrainMode(R.id.askContent) }
        findViewById<Button>(R.id.connections).setOnClickListener { showConnections() }
        findViewById<Button>(R.id.closeConnections).setOnClickListener { closeConnections() }
        findViewById<Button>(R.id.useSetup).setOnClickListener { activateSetup() }
        findViewById<Button>(R.id.clearSetup).setOnClickListener { clearSetup() }
        findViewById<Button>(R.id.devices).setOnClickListener { showPanel(R.id.eyesPanel) }
        findViewById<android.widget.CheckBox>(R.id.showKey).setOnCheckedChangeListener { _, checked ->
            keyInput.inputType = if (checked) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            keyInput.setSelection(keyInput.text.length)
        }
        audit.text = logger.last()
        if (nebius.hasKey()) {
            findViewById<TextView>(R.id.connectionState).text = "●  KEY SAVED"
            status.text = "Your brain is ready — discover models"
            findViewById<TextView>(R.id.nebiusConnected).visibility = View.VISIBLE
            keyInput.visibility = View.GONE
            findViewById<View>(R.id.showKey).visibility = View.GONE
            findViewById<Button>(R.id.saveKey).visibility = View.GONE
        }
        findViewById<Button>(R.id.saveKey).setOnClickListener {
            if (keyInput.text.isNullOrBlank()) { status.text = "Enter a key to continue"; return@setOnClickListener }
            nebius.saveKey(keyInput.text.toString()); keyInput.text?.clear()
            logger.record("Nebius credential provisioned (value never logged)")
            status.text = "Credential saved — discover models to verify connection"; findViewById<TextView>(R.id.connectionState).text = "●  SAVED"; audit.text = logger.last()
        }
        findViewById<Button>(R.id.testNebius).setOnClickListener { runNebiusTest() }
        findViewById<Button>(R.id.voiceNote).setOnClickListener { startVoiceNote() }
        findViewById<Button>(R.id.saveTavily).setOnClickListener { saveTavilyKey() }
        findViewById<Button>(R.id.searchWeb).setOnClickListener { runTavilySearch() }
        findViewById<Button>(R.id.investigate).setOnClickListener { runContextualResearch() }
        findViewById<Button>(R.id.discoverModels).setOnClickListener { discoverModels() }
        findViewById<Button>(R.id.look).setOnClickListener { requestCameraAction() }
        findViewById<Button>(R.id.analyzeEyes).setOnClickListener { analyzePhoto() }
        findViewById<Button>(R.id.eyesVoice).setOnClickListener { startVoiceNote(voiceRequest + 1) }
        findViewById<Button>(R.id.denyTest).setOnClickListener {
            val decision = policy.evaluate(ActionProposal(Capability.CAMERA_LOOK), null)
            logger.record("Denied adversarial proposal: ${decision.reason}"); status.text = "DENIED: ${decision.reason}"; audit.text = logger.last()
        }
        findViewById<Button>(R.id.stop).setOnClickListener {
            getSharedPreferences("pira_security", MODE_PRIVATE).edit().putBoolean("emergency_stop", true).apply()
            logger.record("Emergency stop enabled"); status.text = "EMERGENCY STOP ACTIVE — execution blocked"; audit.text = logger.last()
        }
    }
    private fun saveTavilyKey() {
        if (tavilyKeyInput.text.isNullOrBlank()) { status.text = "Enter a Tavily key to continue"; return }
        tavily.saveKey(tavilyKeyInput.text.toString()); tavilyKeyInput.text?.clear(); logger.record("Tavily credential provisioned (value never logged)"); status.text = "Web search is ready"; audit.text = logger.last()
    }
    private fun runTavilySearch() {
        if (getSharedPreferences("pira_security", MODE_PRIVATE).getBoolean("emergency_stop", false)) { status.text = "DENIED: emergency stop active"; return }
        if (!tavily.hasKey()) { status.text = "Connect Tavily first"; return }
        val query = searchInput.text.toString().trim(); if (query.isBlank()) { status.text = "Enter what you want to find"; return }
        val proposal = WebSearchProposal(query, "user-approved related information", "search-${System.currentTimeMillis()}")
        val grant = WebSearchAuthorization(proposal.query, proposal.purpose, proposal.requestId, System.currentTimeMillis() + 60_000)
        status.text = "Searching trusted web sources…"
        Thread { try { val results = tavily.search(proposal, grant); runOnUiThread { status.text = if (results.isEmpty()) "No sources returned" else results.joinToString("\n\n") { "${it.title}\n${it.url}\n${it.snippet}" }; logger.record("Tavily search completed (${results.size} sources)"); audit.text = logger.last() } } catch (error: Exception) { runOnUiThread { status.text = "Search failed: ${error.message ?: error.javaClass.simpleName}"; logger.record("Tavily search failed"); audit.text = logger.last() } } }.start()
    }
    private fun runContextualResearch() {
        val query = promptInput.text.toString().trim()
        if (query.isBlank()) { status.text = "Write a message first, then ask PIRA to investigate it"; return }
        searchInput.setText(query)
        runTavilySearch()
    }
    private fun showPanel(panelId: Int) {
        listOf(R.id.brainPanel, R.id.eyesPanel, R.id.safetyPanel).forEach { findViewById<View>(it).visibility = if (it == panelId) View.VISIBLE else View.GONE }
    }
    private fun showBrainMode(modeId: Int) {
        findViewById<Button>(R.id.askMode).visibility = View.VISIBLE
        listOf(R.id.askContent, R.id.brainConfig).forEach { findViewById<View>(it).visibility = if (it == modeId) View.VISIBLE else View.GONE }
        status.text = if (modeId == R.id.brainConfig) "Connections" else "Ask PIRA anything"
        findViewById<TextView>(R.id.brainGreeting).text = if (modeId == R.id.brainConfig) "Connect your tools" else "What are we curious about?"
    }
    private fun showConnections() {
        showPanel(R.id.brainPanel)
        findViewById<Button>(R.id.askMode).visibility = View.GONE
        findViewById<View>(R.id.askContent).visibility = View.GONE
        findViewById<View>(R.id.searchContent).visibility = View.GONE
        findViewById<View>(R.id.brainConfig).visibility = View.VISIBLE
        findViewById<View>(R.id.useSetup).visibility = View.VISIBLE
        findViewById<View>(R.id.clearSetup).visibility = View.VISIBLE
        findViewById<TextView>(R.id.brainGreeting).text = "Connect your tools"
        status.text = "Connections"
        findViewById<TextView>(R.id.currentSetup).text = when {
            nebius.hasKey() && tavily.hasKey() -> "Connected: Nebius brain + Tavily search"
            nebius.hasKey() -> "Connected: Nebius brain · Search not connected"
            tavily.hasKey() -> "Connected: Tavily search · Brain not connected"
            else -> "No active connections"
        }
    }
    private fun closeConnections() {
        findViewById<View>(R.id.brainPanel).visibility = View.GONE
        status.text = if (nebius.hasKey()) "Your brain is ready" else "Ready when you are"
    }
    private fun activateSetup() {
        if (!nebius.hasKey()) { status.text = "Connect Nebius before activating setup"; return }
        getSharedPreferences("pira_security", MODE_PRIVATE).edit().putBoolean("connection_active", true).apply()
        findViewById<TextView>(R.id.currentSetup).text = "Current setup: active"
        status.text = "Setup active — PIRA is ready"
        logger.record("Connection setup activated")
        audit.text = logger.last()
    }
    private fun clearSetup() {
        nebius.clear(); tavily.clear(); getSharedPreferences("pira_security", MODE_PRIVATE).edit().putBoolean("connection_active", false).apply()
        findViewById<TextView>(R.id.nebiusConnected).visibility = View.GONE
        keyInput.visibility = View.VISIBLE; findViewById<View>(R.id.showKey).visibility = View.VISIBLE; findViewById<Button>(R.id.saveKey).visibility = View.VISIBLE
        findViewById<TextView>(R.id.currentSetup).text = "No active setup"; status.text = "Configuration cleared"; logger.record("Connection configuration cleared"); audit.text = logger.last()
    }
    private fun discoverModels() {
        if (!nebius.hasKey()) { status.text = "Connect a Nebius key first"; return }
        status.text = "Discovering models…"
        Thread {
            try { val response = nebius.models(); runOnUiThread { showModelOptions(response); findViewById<TextView>(R.id.connectionState).text = "●  MODELS READY"; status.text = "Choose the brain you want to use"; logger.record("Nebius model discovery completed"); audit.text = logger.last() } }
            catch (error: Exception) { runOnUiThread { status.text = "Model discovery error: ${error.message ?: error.javaClass.simpleName}"; logger.record("Nebius model discovery failed"); audit.text = logger.last() } }
        }.start()
    }
    private fun showModelOptions(response: String) {
        val options = findViewById<LinearLayout>(R.id.modelOptions)
        options.removeAllViews()
        val ids = Regex("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").findAll(response).map { it.groupValues[1] }.distinct().toList()
        val prioritized = ids.sortedWith(compareByDescending<String> { it.contains("nemotron", true) }.thenByDescending { it.startsWith("nvidia/", true) }.thenBy { it })
        if (prioritized.isEmpty()) { status.text = "No selectable models were returned"; return }
        prioritized.take(20).forEach { id ->
            val button = Button(this).apply {
                text = if (id.contains("nemotron", true)) "${humanModelName(id)}\nNVIDIA  ·  ${if (id.contains("vl", true)) "VISION + TEXT" else "TEXT"}" else "${humanModelName(id)}\nAvailable model"
                setTextColor(Color.WHITE); gravity = Gravity.START or Gravity.CENTER_VERTICAL; minHeight = 72; isAllCaps = false; textSize = 14f; setPadding(18, 8, 18, 8)
                backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.pira_indigo))
                setOnClickListener { selectedModelId = id; modelInput.setText(humanModelName(id)); getSharedPreferences("pira_config", MODE_PRIVATE).edit().putString("model", id).apply(); status.text = "Brain selected: ${humanModelName(id)}"; options.visibility = View.GONE }
            }
            options.addView(button, LinearLayout.LayoutParams(-1, 64).apply { topMargin = 6 })
        }
        options.visibility = View.VISIBLE
    }
    private fun humanModelName(id: String): String = when {
        id.contains("nemotron", true) && id.contains("vl", true) -> "Nemotron Super VL"
        id.contains("nemotron", true) -> "Nemotron"
        else -> id.substringAfterLast('/').replace('-', ' ').replace('_', ' ')
    }
    private fun runNebiusTest() {
        if (!nebius.hasKey()) { status.text = "Connect a Nebius key first"; return }
        val model = selectedModelId ?: ""; val prompt = promptInput.text.toString().trim()
        if (model.isEmpty() || prompt.isEmpty()) { status.text = "Enter a model ID and prompt"; return }
        sendChatMessage(prompt)
    }
    private fun sendChatMessage(prompt: String) {
        val model = selectedModelId ?: ""
        if (model.isEmpty() || prompt.isBlank()) return
        status.text = "Calling Nebius…"
        Thread {
            try { val response = nebius.chat(model, prompt); runOnUiThread { chatTranscript.visibility = View.VISIBLE; val previous = chatTranscript.text.toString().trim(); val turn = "You\n$prompt\n\nPIRA\n${humanizeModelResponse(response)}"; chatTranscript.text = if (previous.isBlank()) turn else "$previous\n\n────────\n\n$turn"; promptInput.text?.clear(); status.text = "PIRA replied"; logger.record("Nebius text request completed"); audit.text = logger.last() } }
            catch (error: Exception) { runOnUiThread { status.text = "Nebius error: ${error.message ?: error.javaClass.simpleName}"; logger.record("Nebius request denied/failed"); audit.text = logger.last() } }
        }.start()
    }
    private fun startVoiceNote(requestCode: Int = voiceRequest) {
        if (!nebius.hasKey() || selectedModelId.isNullOrBlank()) { status.text = "Connect your brain and choose a model first"; return }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to PIRA")
        }
        try { startActivityForResult(intent, requestCode) } catch (_: Exception) { status.text = "Voice input is not available on this device" }
    }
    private fun requestCameraAction() {
        val parameters = mapOf("prompt" to "user initiated")
        val grant = Authorization(Capability.CAMERA_LOOK, System.currentTimeMillis() + 60_000, "user-once", parameters)
        val decision = policy.evaluate(ActionProposal(Capability.CAMERA_LOOK, parameters = parameters), grant)
        if (!decision.allowed) { status.text = "DENIED: ${decision.reason}"; return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraRequest); return
        }
        logger.record("Authorized camera capture started; image is not retained"); audit.text = logger.last()
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), cameraRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest && resultCode == RESULT_OK) {
            latestPhoto = data?.extras?.get("data") as? Bitmap
            visionTranscript.visibility = View.VISIBLE
            visionTranscript.text = "PIRA\nPhoto captured. Now ask me what to look for."
            status.text = "Photo ready — ask your question"
            if (eyesQuestion.text.isNotBlank()) analyzePhoto()
            logger.record("Authorized camera capture completed; image is not retained")
            audit.text = logger.last()
        }
        if (requestCode == voiceRequest && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) { promptInput.setText(spoken); sendChatMessage(spoken) }
        }
        if (requestCode == voiceRequest + 1 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) { eyesQuestion.setText(spoken); requestCameraAction() }
        }
    }

    private fun analyzePhoto() {
        val image = latestPhoto ?: run { status.text = "Take a photo first"; return }
        val model = selectedModelId ?: run { status.text = "Choose a vision model in Connections"; return }
        val question = eyesQuestion.text.toString().trim().ifBlank { "What do you see?" }
        status.text = "PIRA is looking…"
        Thread { try { val response = nebius.chatVision(model, question, image); runOnUiThread { visionTranscript.text = "You\n$question\n\nPIRA\n${humanizeModelResponse(response)}"; status.text = "PIRA answered about your photo"; logger.record("Authorized vision request completed; image not retained"); audit.text = logger.last() } } catch (error: Exception) { runOnUiThread { status.text = "Vision error: ${error.message ?: error.javaClass.simpleName}"; logger.record("Vision request failed"); audit.text = logger.last() } } }.start()
    }

    private fun humanizeModelResponse(raw: String): String {
        fun clean(value: String): String = value.replace("\\\\n", "\\n").replace("\\\\r", "").replace("\\\\t", " ").replace("\\\\\"", "\\\"").trim()
        fun read(value: Any?): String {
            when (value) {
                is String -> {
                    val text = clean(value)
                    if (text.startsWith("{") || text.startsWith("[")) return try { read(JSONObject(text)) } catch (_: Exception) { text }
                    return text
                }
                is JSONObject -> {
                    val direct = listOf("content", "text", "output", "output_text", "generated_text", "answer", "response", "message").firstNotNullOfOrNull { key -> if (value.has(key)) read(value.get(key)) else null }
                    if (!direct.isNullOrBlank()) return direct
                    listOf("choices", "message", "delta", "data", "response", "results").forEach { key -> if (value.has(key)) { val nested = read(value.get(key)); if (nested.isNotBlank()) return nested } }
                    val ignored = setOf("id", "model", "object", "created", "usage", "finish_reason", "index", "role", "type")
                    value.keys().asSequence().filter { it !in ignored }.map { key -> read(value.get(key)) }.filter { it.length > 20 }.maxByOrNull { it.length }?.let { return it }
                }
                is JSONArray -> {
                    val parts = (0 until value.length()).map { read(value.get(it)) }.filter { it.isNotBlank() }
                    if (parts.isNotEmpty()) return parts.joinToString("\\n\\n")
                }
            }
            return ""
        }
        try {
            val parsed = read(JSONObject(raw))
            if (parsed.isNotBlank()) return parsed
        } catch (_: Exception) { /* handled by plain-text fallback */ }
        val content = Regex("\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").find(raw)?.groupValues?.get(1)
        if (!content.isNullOrBlank()) return clean(content)
        val typedText = Regex("\\\"text\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").findAll(raw).map { clean(it.groupValues[1]) }.filter { it.isNotBlank() }.toList()
        if (typedText.isNotEmpty()) return typedText.joinToString("\\n\\n")
        val fallback = raw.trim()
        if (fallback.startsWith("{") || fallback.startsWith("[")) return "I received a response, but it did not include readable text."
        return fallback.ifBlank { "I couldn't find a readable response." }
    }
}
