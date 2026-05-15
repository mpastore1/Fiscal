package com.mjproductions.fiscal

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf("intro") }

    when (currentScreen) {
        "intro"   -> IntroScreen(onFinished = { currentScreen = "main" })
        "main"    -> MainPage(onNavigateToOptions = { currentScreen = "options" })
        "options" -> options(onBack = { currentScreen = "main" })
    }
}