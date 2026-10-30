package com.taak.watch.signedtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.InputType
import android.view.View
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val cameraRequest = 41
    private lateinit var status: TextView
    private lateinit var audit: TextView
    private lateinit var keyInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var promptInput: EditText
    private lateinit var tavilyKeyInput: EditText
    private lateinit var searchInput: EditText
    private val policy by lazy { PolicyEngine { getSharedPreferences("pira_security", MODE_PRIVATE).getBoolean("emergency_stop", false) } }
    private val logger by lazy { AuditLogger(this) }
    private val nebius by lazy { NebiusClient(this) }
    private val tavily by lazy { TavilyClient(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        status = findViewById(R.id.status); audit = findViewById(R.id.audit); keyInput = findViewById(R.id.nebiusKey)
        modelInput = findViewById(R.id.model); promptInput = findViewById(R.id.prompt)
        tavilyKeyInput = findViewById(R.id.tavilyKey); searchInput = findViewById(R.id.searchQuery)
        findViewById<Button>(R.id.myBrain).setOnClickListener { showPanel(R.id.brainPanel) }
        findViewById<Button>(R.id.myEyes).setOnClickListener { showPanel(R.id.eyesPanel) }
        findViewById<Button>(R.id.mySafety).setOnClickListener { showPanel(R.id.safetyPanel) }
        findViewById<Button>(R.id.myVoice).setOnClickListener { status.text = "My voice is coming soon — nothing is enabled yet" }
        findViewById<android.widget.CheckBox>(R.id.showKey).setOnCheckedChangeListener { _, checked ->
            keyInput.inputType = if (checked) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            keyInput.setSelection(keyInput.text.length)
        }
        audit.text = logger.last()
        if (nebius.hasKey()) {
            findViewById<TextView>(R.id.connectionState).text = "●  KEY SAVED"
            status.text = "Your brain is ready — discover models"
        }
        findViewById<Button>(R.id.saveKey).setOnClickListener {
            if (keyInput.text.isNullOrBlank()) { status.text = "Enter a key to continue"; return@setOnClickListener }
            nebius.saveKey(keyInput.text.toString()); keyInput.text?.clear()
            logger.record("Nebius credential provisioned (value never logged)")
            status.text = "Credential saved — discover models to verify connection"; findViewById<TextView>(R.id.connectionState).text = "●  SAVED"; audit.text = logger.last()
        }
        findViewById<Button>(R.id.testNebius).setOnClickListener { runNebiusTest() }
        findViewById<Button>(R.id.saveTavily).setOnClickListener { saveTavilyKey() }
        findViewById<Button>(R.id.searchWeb).setOnClickListener { runTavilySearch() }
        findViewById<Button>(R.id.discoverModels).setOnClickListener { discoverModels() }
        findViewById<Button>(R.id.look).setOnClickListener { requestCameraAction() }
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
    private fun showPanel(panelId: Int) {
        listOf(R.id.brainPanel, R.id.eyesPanel, R.id.safetyPanel).forEach { findViewById<View>(it).visibility = if (it == panelId) View.VISIBLE else View.GONE }
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
                text = if (id.contains("nemotron", true)) "NVIDIA NEMOTRON\n$id" else id
                setTextColor(Color.WHITE); gravity = Gravity.START or Gravity.CENTER_VERTICAL; minHeight = 56; isAllCaps = false
                backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.pira_indigo))
                setOnClickListener { modelInput.setText(id); status.text = "Selected ${id.substringAfterLast('/')}"; options.visibility = View.GONE }
            }
            options.addView(button, LinearLayout.LayoutParams(-1, 64).apply { topMargin = 6 })
        }
        options.visibility = View.VISIBLE
    }
    private fun runNebiusTest() {
        if (!nebius.hasKey()) { status.text = "Connect a Nebius key first"; return }
        val model = modelInput.text.toString().trim(); val prompt = promptInput.text.toString().trim()
        if (model.isEmpty() || prompt.isEmpty()) { status.text = "Enter a model ID and prompt"; return }
        status.text = "Calling Nebius…"
        Thread {
            try { val response = nebius.chat(model, prompt); runOnUiThread { status.text = "Nebius response:\n${response.take(1200)}"; logger.record("Nebius text request completed"); audit.text = logger.last() } }
            catch (error: Exception) { runOnUiThread { status.text = "Nebius error: ${error.message ?: error.javaClass.simpleName}"; logger.record("Nebius request denied/failed"); audit.text = logger.last() } }
        }.start()
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
}
