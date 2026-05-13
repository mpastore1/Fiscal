package com.mjproductions.fiscal

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fiscal.composeapp.generated.resources.Res
import fiscal.composeapp.generated.resources.fiscallogo_nobg
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

data class Chip(val label: String, val offsetX: Float, val offsetY: Float, val rotation: Float)
data class Message(val text: String, val isUser: Boolean)


const val SERVER_URL = "http://127.0.0.1:8000/chat"

@Composable
fun MainPage() {
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>() }
    var isResponding by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val suggestions = listOf(
        Chip("📈 How do I invest?",           -100f,  0f,   -8f),
        Chip("💰 Budget tips",                 100f,  30f,   6f),
        Chip("🏦 Best savings account",        -90f,  70f,  -5f),
        Chip("📊 Track expenses",              105f,  100f,  4f),
        Chip("💳 Pay off debt faster",         -80f,  140f, -6f),
        Chip("🪙 Compound interest?",           95f,  160f,  5f),
    )
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    // Show chips only when no messages yet
    val showChips = messages.isEmpty()

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isResponding) return
        inputText = ""
        isResponding = true

        // Add user message
        messages.add(Message(text, isUser = true))
// Adding empty AI message
        messages.add(Message("", isUser = false))

        coroutineScope.launch {
            try {
                val client = HttpClient()
                client.preparePost(SERVER_URL) {
                    contentType(ContentType.Application.Json)
                    setBody("""{"message": "${text.replace("\"", "\\\"")}"}""")
                }.execute { response ->
                    val channel = response.bodyAsChannel()
                    val buffer = StringBuilder()
                    while (!channel.isClosedForRead) {
                        val byte = channel.readByte()
                        val char = byte.toInt().toChar()
                        buffer.append(char)
                        val last = messages.last()
                        messages[messages.size - 1] = Message(last.text + char, isUser = false)

                    }
                    client.close()
                }
            } catch (e: Exception) {
                if (e::class.simpleName != "ClosedReceiveChannelException") {
                    println("FiscalChat ERROR: ${e::class.simpleName}: ${e.message}")
                    if (messages.isNotEmpty()) {
                        messages[messages.size - 1] = Message(
                            "Sorry, I couldn't connect to the server.",
                            isUser = false
                        )
                    }
                }
                isResponding = false
            }
            isResponding = false
        }
    }

    // Glow animations
    val glowAlpha by rememberInfiniteTransition(label = "glow")
        .animateFloat(
            initialValue = 0.4f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "glowAlpha"
        )

    val glowScale by rememberInfiniteTransition(label = "glowScale")
        .animateFloat(
            initialValue = 0.9f, targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "glowScale"
        )

    val logoScale by rememberInfiniteTransition(label = "logoScale")
        .animateFloat(
            initialValue = 0.96f, targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "logoScale"
        )

    val chipFloat by rememberInfiniteTransition(label = "chipFloat")
        .animateFloat(
            initialValue = -6f, targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "chipFloat"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF064C89), Color(0xFF080E1A)),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Logo ---
            Spacer(modifier = Modifier.height(80.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                    modifier = Modifier.size(160.dp).graphicsLayer {
                        alpha = glowAlpha * 0.15f
                        scaleX = glowScale * 1.6f; scaleY = glowScale * 1.6f
                    }
                )
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF7DD3FC)),
                    modifier = Modifier.size(160.dp).graphicsLayer {
                        alpha = glowAlpha * 0.3f
                        scaleX = glowScale * 1.3f; scaleY = glowScale * 1.3f
                    }
                )
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFBAE6FD)),
                    modifier = Modifier.size(160.dp).graphicsLayer {
                        alpha = glowAlpha * 0.5f
                        scaleX = glowScale * 1.1f; scaleY = glowScale * 1.1f
                    }
                )
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = "Fiscal Logo",
                    modifier = Modifier.size(120.dp).scale(logoScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Chips or chat messages ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (showChips) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        suggestions.forEachIndexed { index, chip ->
                            val floatOffset = if (index % 2 == 0) chipFloat else -chipFloat
                            SuggestionChip(
                                label = chip.label,
                                offsetX = chip.offsetX,
                                offsetY = chip.offsetY + floatOffset,
                                rotation = chip.rotation,
                                onClick = {
                                    inputText = chip.label.drop(2)
                                    sendMessage()
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(message)
                        }
                    }
                }
            }
        }

        // --- Input bar pinned to bottom ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 36.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    if (inputText.isEmpty()) {
                        Text("Ask Fiscal anything...", color = Color(0xFF4B5563), fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = Color(0xFFE0F2FE),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isListening) Color(0xFF38BDF8) else Color(0xFF1E293B),
                            CircleShape
                        )
                        .clickable { isListening = !isListening },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎤", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (inputText.isNotEmpty() && !isResponding) Color(0xFF38BDF8)
                            else Color(0xFF1E293B),
                            CircleShape
                        )
                        .clickable { sendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isResponding) "…" else "➤",
                        color = if (inputText.isNotEmpty() && !isResponding) Color(0xFF080E1A)
                        else Color(0xFF4B5563),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (message.isUser) Color(0xFF1E40AF) else Color(0xFF111827),
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = Color(0xFFE0F2FE),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SuggestionChip(
    label: String,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .graphicsLayer { rotationZ = rotation }
            .background(Color(0xFF111827).copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color(0xFFE0F2FE), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}