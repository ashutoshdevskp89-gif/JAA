package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerManager(
    private val context: Context,
    private val onReady: () -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onResultReceived: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            isListening = true
                            onReady()
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = (rmsdB + 2f).coerceIn(0f, 10f) / 10f
                            onRmsChanged(normalized)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            isListening = false
                        }

                        override fun onError(error: Int) {
                            isListening = false
                            val message = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "कुछ सुनाई नहीं दिया, कृपया फिर से बोलें"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "समय समाप्त हुआ"
                                SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग में समस्या"
                                SpeechRecognizer.ERROR_NETWORK -> "नेटवर्क समस्या"
                                else -> "माइक त्रुटि (कोड: $error)"
                            }
                            Log.w("SpeechRecognizerManager", "Recognizer error: $error ($message)")
                            onError(message)
                        }

                        override fun onResults(results: Bundle?) {
                            isListening = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                onResultReceived(text)
                            } else {
                                onError("कोई शब्द नहीं पहचाना गया")
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizerManager", "Failed to init speech recognizer", e)
            }
        }
    }

    fun startListening(languageCode: String = "hi-IN") {
        if (speechRecognizer == null) {
            initRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            onError("माइक शुरू करने में असमर्थ: ${e.message}")
        }
    }

    fun stopListening() {
        isListening = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w("SpeechRecognizerManager", "Stop listening error: ${e.message}")
        }
    }

    fun destroy() {
        isListening = false
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("SpeechRecognizerManager", "Destroy error: ${e.message}")
        }
    }
}
