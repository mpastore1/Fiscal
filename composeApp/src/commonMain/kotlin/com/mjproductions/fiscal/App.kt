package com.mjproductions.fiscal

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var showIntro by remember { mutableStateOf(true) }

    if (showIntro) {
        IntroScreen(onFinished = { showIntro = false })
    } else {
        MainPage() // your current MainPage
    }
}