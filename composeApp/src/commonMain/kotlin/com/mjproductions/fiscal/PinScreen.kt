package com.mjproductions.fiscal

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russhwolf.settings.Settings
import fiscal.composeapp.generated.resources.Res
import fiscal.composeapp.generated.resources.fiscallogo_nobg
import org.jetbrains.compose.resources.painterResource

@Composable
fun PinScreen(
    settings: Settings,
    onUnlocked: () -> Unit
) {
    val savedPin = remember { settings.getString("user_pin", "") }
    val isSettingPin = savedPin.isEmpty()

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset by animateFloatAsState(
        targetValue = if (errorMessage.isNotEmpty()) 10f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        finishedListener = { },
        label = "shake"
    )

    fun onDigit(digit: String) {
        errorMessage = ""
        val current = if (isConfirming) confirmPin else pin

        if (current.length >= 4) return

        val updated = current + digit

        if (isSettingPin) {
            if (!isConfirming) {
                pin = updated
                if (pin.length == 4) isConfirming = true
            } else {
                confirmPin = updated
                if (confirmPin.length == 4) {
                    if (confirmPin == pin) {
                        settings.putString("user_pin", pin)
                        onUnlocked()
                    } else {
                        errorMessage = "PINs don't match, try again"
                        pin = ""
                        confirmPin = ""
                        isConfirming = false
                    }
                }
            }
        } else {
            pin = updated
            if (pin.length == 4) {
                if (pin == savedPin) {
                    onUnlocked()
                } else {
                    errorMessage = "Incorrect PIN"
                    pin = ""
                }
            }
        }
    }

    fun onBackspace() {
        errorMessage = ""
        if (isConfirming) {
            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
        } else {
            if (pin.isNotEmpty()) pin = pin.dropLast(1)
        }
    }

    val currentLength = if (isConfirming) confirmPin.length else pin.length
    val title = when {
        isSettingPin && !isConfirming -> "Create a PIN"
        isSettingPin && isConfirming  -> "Confirm your PIN"
        else                          -> "Enter your PIN"
    }
    val subtitle = when {
        isSettingPin && !isConfirming -> "Choose a 4-digit PIN to secure your account"
        isSettingPin && isConfirming  -> "Enter your PIN again to confirm"
        else                          -> "Verify your identity to continue"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF064C89), Color(0xFF080E1A)),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.fiscallogo_nobg),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                modifier = Modifier.size(72.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    color = Color(0xFFE0F2FE),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = Color(0xFF4B5563),
                    fontSize = 13.sp
                )
            }

            // PIN dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(x = shakeOffset.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (index < currentLength) Color(0xFF38BDF8)
                                else Color(0xFF1E3A5F),
                                CircleShape
                            )
                    )
                }
            }

            // Error message
            Box(modifier = Modifier.height(20.dp)) {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp
                    )
                }
            }

            // Number pad
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        if (digit.isEmpty()) Color.Transparent
                                        else Color(0xFF111827),
                                        CircleShape
                                    )
                                    .clickable(enabled = digit.isNotEmpty()) {
                                        if (digit == "⌫") onBackspace()
                                        else onDigit(digit)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (digit.isNotEmpty()) {
                                    Text(
                                        digit,
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}