package com.taak.watch.signedtest

import android.content.Context

object MiauEventStore {
    private const val prefsName = "taak_miau_events"
    private const val keySummary = "summary"
    private const val keyTimestamp = "timestamp"

    fun save(context: Context, summary: String) {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(keySummary, summary)
            .putLong(keyTimestamp, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): StoredMiauEvent? {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val summary = prefs.getString(keySummary, null) ?: return null
        val timestamp = prefs.getLong(keyTimestamp, 0L)
        return StoredMiauEvent(summary, timestamp)
    }
}

data class StoredMiauEvent(
    val summary: String,
    val timestampMs: Long
)
