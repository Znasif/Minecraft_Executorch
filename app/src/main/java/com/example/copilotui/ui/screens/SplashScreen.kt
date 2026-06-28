package com.example.copilotui.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2400)
        onDone()
    }

    val ring1Alpha by rememberInfiniteTransition(label = "r1").animateFloat(
        initialValue = 0.2f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
            tween(2400, easing = LinearEasing), RepeatMode.Reverse
        ), label = "r1a"
    )
    val ring2Alpha by rememberInfiniteTransition(label = "r2").animateFloat(
        initialValue = 0.15f, targetValue = 0.5f, animationSpec = infiniteRepeatable(
            tween(2400, delayMillis = 300, easing = LinearEasing), RepeatMode.Reverse
        ), label = "r2a"
    )
    val scanY by rememberInfiniteTransition(label = "scan").animateFloat(
        initialValue = -46f, targetValue = 46f, animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing), RepeatMode.Reverse
        ), label = "sy"
    )
    val dotAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
        initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(
            tween(1400, easing = LinearEasing), RepeatMode.Reverse
        ), label = "da"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        StatusBar(dark = true, modifier = Modifier.align(Alignment.TopStart))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Pulsing rings + icon
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF2F313A).copy(alpha = ring1Alpha),
                                radius = size.minDimension / 2,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()),
                            )
                        }
                )
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF3F4250).copy(alpha = ring2Alpha),
                                radius = size.minDimension / 2,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()),
                            )
                        }
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Home, null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                // Scanline
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .offset(y = scanY.dp)
                        .drawBehind {
                            val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0xFFCFD2D8), Color.Transparent)
                            )
                            drawRect(gradient)
                        }
                )
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "AI Construction\nCopilot",
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = TextDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.3).sp,
                lineHeight = 28.sp,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7D7E85).copy(alpha = dotAlpha))
                )
                Text(
                    "scanning environment…",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF7D7E85),
                )
            }
        }

        Text(
            "POWERED BY QUALCOMM AI",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.W500,
            color = Color(0xFF6F7077),
            letterSpacing = 0.5.sp,
        )
    }
}
