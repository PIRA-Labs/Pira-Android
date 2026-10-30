package com.taak.watch.signedtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private val policy by lazy { PolicyEngine { getSharedPreferences("pira_security", MODE_PRIVATE).getBoolean("emergency_stop", false) } }
    private val logger by lazy { AuditLogger(this) }
    private val nebius by lazy { NebiusClient(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        status = findViewById(R.id.status); audit = findViewById(R.id.audit); keyInput = findViewById(R.id.nebiusKey)
        modelInput = findViewById(R.id.model); promptInput = findViewById(R.id.prompt)
        audit.text = logger.last()
        findViewById<Button>(R.id.saveKey).setOnClickListener {
            if (keyInput.text.isNullOrBlank()) { status.text = "Enter a key to continue"; return@setOnClickListener }
            nebius.saveKey(keyInput.text.toString()); keyInput.text?.clear()
            logger.record("Nebius credential provisioned (value never logged)")
            status.text = "Model connection configured — hardware access remains separate"; audit.text = logger.last()
        }
        findViewById<Button>(R.id.testNebius).setOnClickListener { runNebiusTest() }
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
