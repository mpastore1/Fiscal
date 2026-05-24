package com.mjproductions.fiscal


import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fiscal.composeapp.generated.resources.Res
import fiscal.composeapp.generated.resources.fiscallogo_nobg
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    onLoginSuccess: (token: String, email: String) -> Unit
) {
    var isSignUp     by remember { mutableStateOf(false) }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Logo glow — same as MainPage
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

    fun submit() {
        errorMessage = ""

        // Basic validation
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }
        if (!email.contains("@")) {
            errorMessage = "Please enter a valid email"
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        if (isSignUp && password != confirmPass) {
            errorMessage = "Passwords don't match"
            return
        }

        isLoading = true
        coroutineScope.launch {
            val result = if (isSignUp) {
                AuthRepository.signUp(email.trim(), password).map { "" }
                    .recoverCatching {
                        throw it  // re-throw to hit the catch below
                    }.also {
                        // After sign up, sign in to get the token
                        if (it.isSuccess) {
                            AuthRepository.signIn(email.trim(), password)
                                .onSuccess { token ->
                                    isLoading = false
                                    onLoginSuccess(token, email.trim())
                                }
                                .onFailure { e ->
                                    isLoading = false
                                    errorMessage = "Account created! Please check your email to confirm, then sign in."
                                }
                            return@launch
                        }
                    }
            } else {
                AuthRepository.signIn(email.trim(), password)
                    .onSuccess { token ->
                        isLoading = false
                        onLoginSuccess(token, email.trim())
                    }
                    .onFailure { e ->
                        isLoading = false
                        errorMessage = when {
                            e.message?.contains("Invalid login") == true -> "Incorrect email or password"
                            e.message?.contains("Email not confirmed") == true -> "Please confirm your email first"
                            else -> "Something went wrong, please try again"
                        }
                    }
            }
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            // --- Logo (same layered glow as MainPage) ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                    modifier = Modifier.size(120.dp).graphicsLayer {
                        alpha = glowAlpha * 0.15f
                        scaleX = glowScale * 1.6f; scaleY = glowScale * 1.6f
                    }
                )
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF7DD3FC)),
                    modifier = Modifier.size(120.dp).graphicsLayer {
                        alpha = glowAlpha * 0.3f
                        scaleX = glowScale * 1.3f; scaleY = glowScale * 1.3f
                    }
                )
                Image(
                    painter = painterResource(Res.drawable.fiscallogo_nobg),
                    contentDescription = "Fiscal Logo",
                    modifier = Modifier.size(90.dp).graphicsLayer {
                        scaleX = logoScale; scaleY = logoScale
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Title ---
            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                color = Color(0xFFE0F2FE),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSignUp) "Start your financial journey" else "Sign in to FISCAL",
                color = Color(0xFF4B5563),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // --- Email field ---
            AuthField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Password field ---
            AuthField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                placeholder = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
                isPassword = true,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword },
                onDone = { if (!isSignUp) submit() }
            )

            // --- Confirm password (sign up only) ---
            if (isSignUp) {
                Spacer(modifier = Modifier.height(12.dp))
                AuthField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it; errorMessage = "" },
                    placeholder = "Confirm Password",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword },
                    onDone = { submit() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Error message ---
            Box(modifier = Modifier.fillMaxWidth().height(32.dp)) {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }

            // --- Submit button ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isLoading) Color(0xFF1E3A5F) else Color(0xFF38BDF8),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = !isLoading) { submit() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isLoading && isSignUp  -> "Creating account..."
                        isLoading && !isSignUp -> "Signing in..."
                        isSignUp               -> "Create Account"
                        else                   -> "Sign In"
                    },
                    color = if (isLoading) Color(0xFF4B5563) else Color(0xFF080E1A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Toggle sign in / sign up ---
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = Color(0xFF4B5563),
                    fontSize = 13.sp
                )
                Text(
                    text = if (isSignUp) "Sign In" else "Sign Up",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        isSignUp = !isSignUp
                        errorMessage = ""
                        password = ""
                        confirmPass = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ---------- Reusable text field ----------

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111827), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color(0xFF4B5563), fontSize = 15.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFFE0F2FE),
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                visualTransformation = if (isPassword && !showPassword)
                    PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone?.invoke() }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show/hide password toggle
        if (isPassword && onTogglePassword != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showPassword) "🙈" else "👁",
                fontSize = 16.sp,
                modifier = Modifier.clickable { onTogglePassword() }
            )
        }
    }
}