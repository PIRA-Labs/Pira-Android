package com.taak.watch.signedtest

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Thin native capability adapter. It never accepts raw model output: callers must
 * first obtain a matching, short-lived Authorization through PolicyEngine.
 */
class NativeTextToSpeech(context: Context, private val onReady: (Boolean) -> Unit) : TextToSpeech.OnInitListener {
    private val engine = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) engine.language = Locale.getDefault()
        onReady(ready)
    }

    fun speak(text: String): Boolean {
        if (!ready) return false
        return engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pira-${System.currentTimeMillis()}") == TextToSpeech.SUCCESS
    }

    fun close() = engine.shutdown()
}
