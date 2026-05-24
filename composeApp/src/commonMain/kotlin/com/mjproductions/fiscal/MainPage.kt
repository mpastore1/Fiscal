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
import fiscal.composeapp.generated.resources.wrench
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import io.ktor.utils.io.core.EOFException
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class Chip(val label: String, val offsetX: Float, val offsetY: Float, val rotation: Float)
data class Message(val text: String, val isUser: Boolean)


const val SERVER_URL = "https://mj-prod-fiscal.hf.space/chat"

@Composable
fun MainPage(onNavigateToOptions: () -> Unit,onTutorialComplete: () -> Unit,
             showTutorial: Boolean = false,  authToken: String = "") {
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>() }
    var isResponding by remember { mutableStateOf(false) }
    val speechRecognizer = rememberSpeechRecognizer()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var tutorialActive by remember { mutableStateOf(showTutorial) }



    var tutorialStep by remember { mutableStateOf(0) }
    val tutorialSteps = listOf(
        TutorialStep("logo",    "Welcome to Fiscal! Your personal AI financial assistant.", Alignment.Center),
        TutorialStep("chips",   "Tap any suggestion to get started quickly.", Alignment.TopCenter),
        TutorialStep("input",   "Or type your own question here.", Alignment.BottomCenter),
        TutorialStep("mic",     "Use your voice to ask Fiscal anything hands-free.", Alignment.BottomCenter),
        TutorialStep("options", "Access settings and preferences here to link your bank account.", Alignment.TopEnd),
    )

    val suggestions = listOf(
        Chip("📈 How do I invest?",           -100f,  0f,   -8f),
        Chip("💰 Budget tips",                 100f,  30f,   6f),
        Chip("🏦 Best savings account",        -90f,  70f,  -5f),
        Chip("📊 Track expenses",              105f,  100f,  4f),
        Chip("💳 Pay off debt faster",         -80f,  140f, -6f),
        Chip("🪙 Compound interest?",           95f,  160f,  5f),
    )


    LaunchedEffect(Unit) {
        // Reset conversation memory on the server when MainPage loads
        coroutineScope.launch {
            val client = HttpClient()
            try {
                client.post("https://mj-prod-fiscal.hf.space/reset") {
                    header("Authorization", "Bearer $authToken")
                    contentType(ContentType.Application.Json)
                }
            } catch (e: Exception) {
                println("Reset error: ${e.message}")
            } finally {
                client.close()
            }
        }
    }


    val showChips = messages.isEmpty()

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isResponding) return
        inputText = ""
        isResponding = true

        messages.add(Message(text, isUser = true))
        messages.add(Message("", isUser = false))

        coroutineScope.launch {
            val client = HttpClient()
            try {
                client.preparePost("https://mj-prod-fiscal.hf.space/chat/stream") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $authToken")
                    setBody("""{"message":"${text.replace("\"", "\\\"")}"}""")
                }.execute { response ->
                    val channel = response.bodyAsChannel()
                    val buf = ByteArray(4096)

                    while (true) {
                        val n = channel.readAvailable(buf, 0, buf.size)
                        if (n == -1) break
                        if (n == 0) continue

                        val raw = buf.decodeToString(0, n)

                        // Parse SSE format: "data: {"chunk": "..."}\n\n"
                        raw.split("\n").forEach { line ->
                            if (line.startsWith("data: ")) {
                                val data = line.removePrefix("data: ").trim()
                                if (data == "[DONE]") return@forEach

                                try {
                                    val json = kotlinx.serialization.json.Json
                                        .parseToJsonElement(data)
                                    val chunk = json.jsonObject["chunk"]
                                        ?.jsonPrimitive?.content ?: ""

                                    if (chunk.isNotEmpty()) {
                                        val last = messages.last()
                                        messages[messages.size - 1] =
                                            Message(last.text + chunk, isUser = false)
                                    }
                                } catch (e: Exception) {
                                    // skip malformed chunks
                                }
                            }
                        }
                    }
                }
            } catch (e: EOFException) {
                // normal stream end
            } catch (e: Exception) {
                println("FiscalChat ERROR: ${e::class.simpleName}: ${e.message}")
                if (messages.isNotEmpty() && messages.last().text.isEmpty()) {
                    messages[messages.size - 1] =
                        Message("Sorry, I couldn't connect to the server.", isUser = false)
                }
            } finally {
                client.close()
                isResponding = false
            }
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

                val ringTransition = rememberInfiniteTransition(label = "micRing")
                val ringScale by ringTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ringScale"
                )
                val ringAlpha by ringTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ringAlpha"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer {
                                    scaleX = ringScale
                                    scaleY = ringScale
                                    alpha = ringAlpha
                                }
                                .background(Color(0xFF38BDF8), CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isListening) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                CircleShape
                            )
                            .clickable {
                                if (isListening) {
                                    speechRecognizer.stop()
                                    isListening = false
                                } else {
                                    isListening = true
                                    speechRecognizer.start(
                                        onResult = { text ->
                                            inputText = text
                                            isListening = false
                                            sendMessage()
                                        },
                                        onError = { err ->
                                            println("Speech error: $err")
                                            isListening = false
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            WaveBars(color = Color(0xFF080E1A))
                        } else {
                            Text("🎤", fontSize = 16.sp)
                        }
                    }
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


        // --- Options button (top right) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF111827), CircleShape)
                    .clickable { onNavigateToOptions() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.wrench),
                    contentDescription = "Options",
                    colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- Tutorial Overlay ---
        if (tutorialActive && tutorialStep < tutorialSteps.size) {
            val step = tutorialSteps[tutorialStep]

            // Dim background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(alpha = 0.75f))
                    .clickable {
                        if (tutorialStep < tutorialSteps.size - 1) tutorialStep++
                        else tutorialActive = false
                    }
            )

            // Highlight card
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when (step.targetDescription) {
                    "logo"    -> Alignment.TopCenter
                    "chips"   -> Alignment.TopCenter
                    "input"   -> Alignment.BottomCenter
                    "mic"     -> Alignment.BottomCenter
                    "options" -> Alignment.TopEnd
                    else      -> Alignment.Center
                }
            ) {
                val cardPadding = when (step.targetDescription) {
                    "logo"    -> PaddingValues(top = 260.dp, start = 24.dp, end = 24.dp)
                    "chips"   -> PaddingValues(top = 300.dp, start = 24.dp, end = 24.dp)
                    "input"   -> PaddingValues(bottom = 120.dp, start = 24.dp, end = 24.dp)
                    "mic"     -> PaddingValues(bottom = 120.dp, start = 24.dp, end = 24.dp)
                    "options" -> PaddingValues(top = 110.dp, end = 24.dp, start = 24.dp)
                    else      -> PaddingValues(24.dp)
                }

                Box(modifier = Modifier.padding(cardPadding)) {
                    TutorialCard(
                        step = tutorialStep,
                        total = tutorialSteps.size,
                        message = step.message,
                        onNext = {
                            if (tutorialStep < tutorialSteps.size - 1) tutorialStep++
                            else {
                                onTutorialComplete()  // ← calls App.kt's lambda instead
                                tutorialActive = false
                            }
                        },
                        onSkip = {
                            onTutorialComplete()
                            tutorialActive = false
                        }
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
fun WaveBars(color: Color) {
    val transition = rememberInfiniteTransition(label = "bars")
    val delays = listOf(0, 120, 240, 360, 180)  // staggered so bars don't sync

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        delays.forEach { delay ->
            val height by transition.animateFloat(
                initialValue = 4f,
                targetValue = 18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, delayMillis = delay, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$delay"
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(height.dp)
                    .background(color, RoundedCornerShape(2.dp))
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


@Composable
fun TutorialCard(
    step: Int,
    total: Int,
    message: String,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1B2E), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        // Step indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == step) 20.dp else 6.dp, 6.dp)
                        .background(
                            if (index == step) Color(0xFF38BDF8)
                            else Color(0xFF1E3A5F),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Text(
            text = message,
            color = Color(0xFFE0F2FE),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Skip",
                color = Color(0xFF4B5563),
                fontSize = 13.sp,
                modifier = Modifier.clickable { onSkip() }
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    .clickable { onNext() }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (step == total - 1) "Done" else "Next →",
                    color = Color(0xFF080E1A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class TutorialStep(
    val targetDescription: String,
    val message: String,
    val alignment: Alignment
)