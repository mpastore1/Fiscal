package com.mjproductions.fiscal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSR
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizer {
    private var recognizer: AndroidSR? = null

    override fun start(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Microphone permission not granted")
            return
        }

        recognizer = AndroidSR.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onError(error: Int) {
                    onError("Recognition error: $error")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(AndroidSR.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotEmpty()) onResult(text) else onError("No speech detected")
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    override fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}

@Composable
actual fun rememberSpeechRecognizer(): SpeechRecognizer {
    val context = LocalContext.current
    return remember { AndroidSpeechRecognizer(context) }
}