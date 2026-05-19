package com.mjproductions.fiscal

import androidx.compose.runtime.Composable

interface SpeechRecognizer {
    fun start(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stop()
}

@Composable
expect fun rememberSpeechRecognizer(): SpeechRecognizer