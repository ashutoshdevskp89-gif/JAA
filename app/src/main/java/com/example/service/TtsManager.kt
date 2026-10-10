package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onSpeechStarted: () -> Unit,
    private val onSpeechFinished: () -> Unit,
    private val onSpeechError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentPitch = 1.35f // Cute sweet female pitch
    private var currentRate = 1.02f

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            configureVoice(Locale("hi", "IN"))
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStarted()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechFinished()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechError("TTS Playback error")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onSpeechError("TTS Error code: $errorCode")
                }
            })
        } else {
            Log.e("TtsManager", "TextToSpeech init failed")
        }
    }

    fun configureVoice(locale: Locale) {
        tts?.let { engine ->
            val result = engine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English (India) or default
                engine.setLanguage(Locale("en", "IN"))
            }
            engine.setPitch(currentPitch)
            engine.setSpeechRate(currentRate)

            // Try selecting a female voice if available
            try {
                val femaleVoice = engine.voices?.firstOrNull {
                    it.name.contains("female", ignoreCase = true) ||
                    it.name.contains("woman", ignoreCase = true) ||
                    it.name.contains("c", ignoreCase = true)
                }
                if (femaleVoice != null) {
                    engine.voice = femaleVoice
                }
            } catch (e: Exception) {
                Log.w("TtsManager", "Could not set specific voice: ${e.message}")
            }
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch
        tts?.setPitch(pitch)
    }

    fun setRate(rate: Float) {
        currentRate = rate
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String, utteranceId: String = "ja_speech_${System.currentTimeMillis()}") {
        if (!isInitialized) return
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
