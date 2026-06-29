package com.aml_sakr.fitlife.feature.session.ui.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

private const val TAG = "SessionTtsManager"

/**
 * Manages Text-to-Speech for workout guidance.
 * AC 6 compliance: Provides workout instructions and cues via TTS.
 */
class SessionTtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
                flushPendingMessages()
            } else {
                Log.e(TAG, "Initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.d(TAG, "Buffering message: $text")
            pendingMessages.add(text)
        }
    }

    private fun flushPendingMessages() {
        if (pendingMessages.isNotEmpty()) {
            val combined = pendingMessages.joinToString(". ")
            tts?.speak(combined, TextToSpeech.QUEUE_FLUSH, null, null)
            pendingMessages.clear()
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
