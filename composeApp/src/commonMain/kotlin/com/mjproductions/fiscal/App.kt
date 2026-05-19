package com.mjproductions.fiscal

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.russhwolf.settings.Settings

@Composable
@Preview
fun App() {
    val settings = remember { Settings() }
    var isUnlocked by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("intro") }
    var tutorialComplete by remember {
        mutableStateOf(settings.getString("tutorial_complete_v2", "") == "done")
    }

    if (!isUnlocked) {
        PinScreen(
            settings = settings,
            onUnlocked = { isUnlocked = true }
        )
    } else {
        when (currentScreen) {
            "intro"   -> IntroScreen(onFinished = { currentScreen = "main" })
            "main"    -> MainPage(
                onNavigateToOptions = { currentScreen = "options" },
                onTutorialComplete = {
                    settings.putString("tutorial_complete_v2", "done")
                    tutorialComplete = true
                },
                showTutorial = !tutorialComplete
            )
            "options" -> options(onBack = { currentScreen = "main" })
        }
    }
}