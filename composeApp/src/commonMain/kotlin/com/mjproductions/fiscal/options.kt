package com.mjproductions.fiscal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fiscal.composeapp.generated.resources.Res
import fiscal.composeapp.generated.resources.account_balance
import fiscal.composeapp.generated.resources.arrow_back
import fiscal.composeapp.generated.resources.faq_a1
import fiscal.composeapp.generated.resources.faq_a2
import fiscal.composeapp.generated.resources.faq_a3
import fiscal.composeapp.generated.resources.faq_q1
import fiscal.composeapp.generated.resources.faq_q2
import fiscal.composeapp.generated.resources.faq_q3
import fiscal.composeapp.generated.resources.question_mark
import fiscal.composeapp.generated.resources.support_agent
import fiscal.composeapp.generated.resources.wrench
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


data class OptionItem(val icon: DrawableResource, val label: String, val subtitle: String)

@Composable
fun options(onBack: () -> Unit) {
    var activeSheet by remember { mutableStateOf<String?>(null) }

    val optionsList = listOf(
        OptionItem(Res.drawable.account_balance,"Link Bank Account", "Connect your bank account securely"),
        OptionItem(Res.drawable.question_mark,     "FAQ",               "Common questions answered"),
        OptionItem(Res.drawable.wrench,    "Modify Fiscal",     "Personalise your assistant"),
        OptionItem(Res.drawable.support_agent,    "Contact",           "Get in touch with us about anything"),
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
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 40f && activeSheet == null) onBack()
                }
            }
    ) {
        // --- Main content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF111827), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.arrow_back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Options",
                    color = Color(0xFFE0F2FE),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            optionsList.forEach { item ->
                OptionRow(item, onClick = { activeSheet = item.label })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // --- Dim overlay ---
        AnimatedVisibility(
            visible = activeSheet != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(alpha = 0.6f))
                    .clickable { activeSheet = null }
            )
        }

        // --- Bottom Sheet ---
        AnimatedVisibility(
            visible = activeSheet != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = EaseOutQuart)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = EaseInQuart)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFF0D1B2E))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 40f) activeSheet = null
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFF2D3F55), RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = activeSheet ?: "",
                        color = Color(0xFFE0F2FE),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1E3A5F))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sheet content per option
                    when (activeSheet) {
                        "Link Bank Account" -> SheetLinkBank()
                        "FAQ"               -> SheetFAQ()
                        "Modify Fiscal"     -> SheetModifyFiscal()
                        "Contact"           -> SheetContact()
                    }
                }
            }
        }
    }
}

// --- Sheet content composables (fill these in) ---

@Composable
fun SheetLinkBank() {
    Text(
        "Connect your bank account to track spending automatically.",
        color = Color(0xFF94A3B8), fontSize = 14.sp, lineHeight = 22.sp
    )
}

@Composable
fun SheetFAQ() {
    val faqs = listOf(
        stringResource(Res.string.faq_q1) to stringResource(Res.string.faq_a1),
        stringResource(Res.string.faq_q2) to stringResource(Res.string.faq_a2),
        stringResource(Res.string.faq_q3) to stringResource(Res.string.faq_a3),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        faqs.forEach { (question, answer) ->
            FAQItem(question = question, answer = answer)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Composable
fun SheetModifyFiscal() {
    Text(
        "Personalise how Fiscal responds and what it focuses on.",
        color = Color(0xFF94A3B8), fontSize = 14.sp, lineHeight = 22.sp
    )
}

@Composable
fun SheetContact() {
    Text(
        "Reach out to us at support@mjproductions.com",
        color = Color(0xFF94A3B8), fontSize = 14.sp, lineHeight = 22.sp
    )
}

@Composable
fun OptionRow(item: OptionItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111827), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF0F2A4A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(item.icon),
                contentDescription = item.label,
                colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                color = Color(0xFFE0F2FE),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.subtitle,
                color = Color(0xFF4B5563),
                fontSize = 12.sp
            )
        }
    }
}



@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111827), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                color = Color(0xFFE0F2FE),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(Res.drawable.arrow_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFF38BDF8)),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = rotationAngle }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF1E3A5F))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = answer,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}