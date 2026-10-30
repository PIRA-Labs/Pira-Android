package com.taak.watch.signedtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MiauBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MIAU) return

        val did = intent.getStringExtra(EXTRA_DID).orEmpty()
        val source = intent.getStringExtra(EXTRA_SOURCE).orEmpty()
        val transport = intent.getStringExtra(EXTRA_TRANSPORT).orEmpty()
        val event = intent.getStringExtra(EXTRA_EVENT).orEmpty()
        val summary = listOf(
            "did=$did",
            "source=$source",
            "event=$event",
            "transport=$transport"
        ).joinToString(" ")

        Log.i("TAAK_MIAU", "TAAK_MIAU received $summary")
        MiauEventStore.save(context, summary)
    }

    companion object {
        const val ACTION_MIAU = "com.taak.watch.MIAU"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_DID = "did"
        const val EXTRA_EVENT = "event"
        const val EXTRA_TRANSPORT = "transport"
    }
}
