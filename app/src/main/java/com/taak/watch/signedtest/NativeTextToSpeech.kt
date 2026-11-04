package com.taak.watch.signedtest

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Thin native capability adapter. It never accepts raw model output: callers must
 * first obtain a matching, short-lived Authorization through PolicyEngine.
 */
enum class TtsExecutionEvent { STARTED, COMPLETED, FAILED, CANCELLED }

class NativeTextToSpeech(context: Context, private val onReady: (Boolean) -> Unit, private val onEvent: (TtsExecutionEvent, String) -> Unit = { _, _ -> }) : TextToSpeech.OnInitListener {
    private val engine = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            engine.language = Locale.getDefault()
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String) = onEvent(TtsExecutionEvent.STARTED, id)
                override fun onDone(id: String) = onEvent(TtsExecutionEvent.COMPLETED, id)
                @Deprecated("Deprecated in Java") override fun onError(id: String) = onEvent(TtsExecutionEvent.FAILED, id)
                override fun onError(id: String, errorCode: Int) = onEvent(TtsExecutionEvent.FAILED, id)
                override fun onStop(id: String, interrupted: Boolean) = onEvent(TtsExecutionEvent.CANCELLED, id)
            })
        }
        onReady(ready)
    }

    fun speak(text: String): Boolean {
        if (!ready) return false
        return engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pira-${System.currentTimeMillis()}") == TextToSpeech.SUCCESS
    }

    fun stop() = engine.stop()
    fun close() = engine.shutdown()
}
