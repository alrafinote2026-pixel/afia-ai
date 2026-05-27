package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceSystem(
    private val context: Context,
    private val onTextRecognized: (String) -> Unit,
    private val onListeningStateChanged: (ListeningState) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    var isTtsEnabled = true
    private var isContinuousListening = false

    init {
        // Initialize Android TextToSpeech
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceSystem", "TTS creation failures", e)
        }

        // Initialize Android SpeechRecognizer
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            onListeningStateChanged(ListeningState.LISTENING)
                        }

                        override fun onBeginningOfSpeech() {
                            onListeningStateChanged(ListeningState.RECORDING)
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Can be used to drive a glowing orb waveform viz
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            onListeningStateChanged(ListeningState.PROCESSING)
                        }

                        override fun onError(error: Int) {
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions required for microphone access"
                                SpeechRecognizer.ERROR_NETWORK -> "Network failure detected"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network speed timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No audible signal recognized"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic channels are busy"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Audible signal timeout"
                                else -> "Microphone sector code error ($error)"
                            }
                            Log.w("VoiceSystem", "SpeechRecognizer error: $msg")
                            onError(msg)
                            onListeningStateChanged(ListeningState.IDLE)
                            
                            // Retry listening if continuous mode is enabled
                            if (isContinuousListening) {
                                startListening()
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val bestMatch = matches?.firstOrNull()
                            if (bestMatch != null) {
                                onTextRecognized(bestMatch)
                            } else {
                                onError("Data transmission mismatch")
                            }
                            onListeningStateChanged(ListeningState.IDLE)
                            
                            if (isContinuousListening) {
                                startListening()
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                Log.e("VoiceSystem", "Failed to initialize SpeechRecognizer", e)
            }
        } else {
            Log.w("VoiceSystem", "Speech recognition not available on this device")
        }
    }

    // --- Speech Recognizer Control ---
    fun startListening() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            onError("Speech API offline. Please use prompt terminal typing fallback.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            onError("Mic trigger anomaly: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onListeningStateChanged(ListeningState.IDLE)
    }

    fun setContinuousMode(enabled: Boolean) {
        isContinuousListening = enabled
        if (enabled) {
            startListening()
        } else {
            stopListening()
        }
    }

    fun toggleContinuousMode(): Boolean {
        setContinuousMode(!isContinuousListening)
        return isContinuousListening
    }

    fun getContinuousMode(): Boolean = isContinuousListening

    // --- TTS Control ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceSystem", "Selected linguistic pattern is not supported")
            } else {
                // Pitch vocalizer tuning for futuristic voice
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(1.05f)
            }
        } else {
            Log.e("VoiceSystem", "Linguistic synthesizer calibration failed")
        }
    }

    fun speak(text: String) {
        if (!isTtsEnabled) return
        val speakingTts = tts
        if (speakingTts != null) {
            speakingTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AfiaSpeechUID")
        } else {
            Log.w("VoiceSystem", "Speech synthesis offline. Unable to talk.")
        }
    }

    fun shutdown() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceSystem", "Failed shutting down speech channels", e)
        }
    }
}

enum class ListeningState {
    IDLE,
    LISTENING,
    RECORDING,
    PROCESSING
}
