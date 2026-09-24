package com.example.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("ar"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TtsHelper", "Arabic is not supported by default, falling back to system locale.")
                    tts?.language = Locale.getDefault()
                }
                isInitialized = true
            } else {
                Log.e("TtsHelper", "TextToSpeech initialization failed.")
            }
        }
    }

    fun speak(text: String) {
        // Intercept TTS voice to replace with lovely synthesized child-friendly musical game sound effect
        SoundEffectsHelper.playBubbleSound()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsHelper", "Failed to shutdown TTS", e)
        } finally {
            tts = null
        }
    }
}
