package com.mjproductions.fiscal

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fiscal.composeapp.generated.resources.Res
import fiscal.composeapp.generated.resources.fiscallogo_nobg
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val fullWord = "FISCAL"
    var typedText by remember { mutableStateOf("") }
    var typingDone by remember { mutableStateOf(false) }

    var glowTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        glowTriggered = true
    }

    // Typing effect
    LaunchedEffect(Unit) {
        delay(600)
        for (i in fullWord.indices) {
            typedText = fullWord.substring(0, i + 1)
            delay(120)
        }
        typingDone = true
        delay(3000)
        onFinished()
    }

    // Glow pulse
    val glowPulse by animateFloatAsState(
        targetValue = if (glowTriggered) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1800, easing = EaseOutCubic),
        label = "glowPulse"
    )

    // Cursor blink
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(typingDone) {
        if (typingDone) {
            repeat(6) {
                delay(400)
                cursorVisible = !cursorVisible
            }
            cursorVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080E1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                    modifier = Modifier
                        .size(250.dp)
                        .graphicsLayer {
                            alpha = glowPulse * 0.12f
                            scaleX = 1.6f + glowPulse * 0.2f
                            scaleY = 1.6f + glowPulse * 0.2f
                        }
                )

                // Layer 2
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF004D74)),
                    modifier = Modifier
                        .size(250.dp)
                        .graphicsLayer {
                            alpha = glowPulse * 0.2f
                            scaleX = 1.4f + glowPulse * 0.15f
                            scaleY = 1.4f + glowPulse * 0.15f
                        }
                )

                // Layer 3
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF003F58)),
                    modifier = Modifier
                        .size(250.dp)
                        .graphicsLayer {
                            alpha = glowPulse * 0.3f
                            scaleX = 1.2f + glowPulse * 0.1f
                            scaleY = 1.2f + glowPulse * 0.1f
                        }
                )

                // Layer 4 — closest glow, brightest
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFBAE6FD)),
                    modifier = Modifier
                        .size(250.dp)
                        .graphicsLayer {
                            alpha = glowPulse * 0.45f
                            scaleX = 1.08f + glowPulse * 0.05f
                            scaleY = 1.08f + glowPulse * 0.05f
                        }
                )

                // Actual logo on top
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = "Fiscal Logo",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Typed text with blinking cursor
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = typedText,
                    color = Color(0xFFE0F2FE),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center
                )
                if (!typingDone || cursorVisible) {
                    Text(
                        text = "|",
                        color = Color(0xFF38BDF8),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}