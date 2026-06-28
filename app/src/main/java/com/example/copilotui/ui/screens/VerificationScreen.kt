package com.example.copilotui.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*
import com.example.copilotui.ui.viewmodel.ConstructionViewModel

@Composable
fun VerificationScreen(
    viewModel: ConstructionViewModel,
    onBack: () -> Unit,
    onFix: () -> Unit,
    onProceed: () -> Unit,
) {
    val result = viewModel.lastVerificationResult
    val isVerifying = viewModel.isVerifying
    val proceedEnabled = result?.verdict == true

    val animatedConfidence by animateFloatAsState(
        targetValue = result?.confidence ?: 0f,
        animationSpec = tween(durationMillis = 800),
        label = "confidence",
    )

    Column(modifier = Modifier.fillMaxSize().background(LightBg)) {
        StatusBar()
        Text(
            "Step Verification",
            fontSize = 17.sp,
            fontWeight = FontWeight.W600,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // AR verification thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .drawBehind {
                        drawRect(Color(0xFF2A2B30))
                        val stripe = 7.dp.toPx()
                        var off = -size.height; var i = 0
                        while (off < size.width + size.height) {
                            if (i % 2 == 0) {
                                val p = Path().apply {
                                    moveTo(off, 0f); lineTo(off + stripe, 0f)
                                    lineTo(off + stripe - size.height, size.height)
                                    lineTo(off - size.height, size.height); close()
                                }
                                drawPath(p, Color(0xFF222328))
                            }
                            i++; off += stripe
                        }
                        val rx = (size.width - 74.dp.toPx()) / 2
                        val ry = (size.height - 50.dp.toPx()) / 2
                        drawRect(
                            Color(0xFFE3E3E0),
                            topLeft = androidx.compose.ui.geometry.Offset(rx, ry),
                            size = androidx.compose.ui.geometry.Size(74.dp.toPx(), 50.dp.toPx()),
                            style = Stroke(2.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Rounded.Warning, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Text("misaligned 2.1°", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W600, color = Color.White)
                }
            }

            // VLM result panel
            when {
                isVerifying -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(LightCard)
                            .border(1.5.dp, LightCardBorder, RoundedCornerShape(11.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00BCD4),
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                            )
                            Text(
                                "Running on Hexagon NPU…",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00BCD4),
                            )
                        }
                    }
                }

                result != null -> {
                    // Status chip
                    val chipColor = if (result.verdict) Color(0xFF1B7A4A) else Color(0xFF9B2335)
                    val chipLabel = if (result.verdict) "STEP COMPLETE" else "INCOMPLETE"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipColor)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(chipLabel, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W700, color = Color.White)
                    }

                    // Confidence bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(LightCard)
                            .border(1.5.dp, LightCardBorder, RoundedCornerShape(11.dp))
                            .padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("AI CONFIDENCE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
                            Text("${(result.confidence * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.W700, color = TextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE8E8E4))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedConfidence)
                                    .fillMaxHeight()
                                    .background(Color(0xFF00BCD4))
                            )
                        }
                    }

                    // Inference stats
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(LightCard)
                            .border(1.5.dp, LightCardBorder, RoundedCornerShape(11.dp))
                            .padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            "${result.inferenceTimeMs}ms ⚡ Hexagon NPU",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.W600,
                            color = TextPrimary,
                        )
                        Text(
                            result.modelInfo,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                        )
                    }
                }

                else -> {
                    // Idle: static score cards
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        ScoreCard("AI CONFIDENCE", "–", null, Modifier.weight(1f))
                        ScoreCard("BUILD SCORE", "–", null, Modifier.weight(1f))
                    }

                    // Detected mistakes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(LightCard)
                            .border(1.5.dp, LightCardBorder, RoundedCornerShape(11.dp))
                            .padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("DETECTED MISTAKES · 1", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Warning, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                            Text("North wall tilted 2.1° from vertical", fontSize = 12.sp, fontWeight = FontWeight.W500, color = TextPrimary)
                        }
                    }

                    // AI suggestion
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(SuggestionBg)
                            .border(1.5.dp, SuggestionBorder, RoundedCornerShape(11.dp))
                            .padding(11.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = SuggestionIcon, modifier = Modifier.size(18.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("SUGGESTION", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = Color(0xFF7A8077))
                            Text(
                                "Loosen the top brace and shim the base plate ~6 mm on the east side.",
                                fontSize = 12.sp, fontWeight = FontWeight.W500, color = TextPrimary, lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp, 10.dp, 16.dp, 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(CtaDark)
                    .clickable(onClick = onFix)
                    .padding(13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Text("Fix Before Continuing", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color.White)
                }
            }
            val proceedAlpha = if (proceedEnabled) 1f else 0.4f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.5.dp, LightInputBorder, RoundedCornerShape(13.dp))
                    .then(
                        if (proceedEnabled) Modifier.clickable {
                            viewModel.advanceStep()
                            onProceed()
                        } else Modifier
                    )
                    .padding(11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Proceed Anyway", fontSize = 13.sp, fontWeight = FontWeight.W600, color = Color(0xFF33332F).copy(alpha = proceedAlpha))
                    Icon(Icons.Rounded.ArrowForward, null, tint = Color(0xFF33332F).copy(alpha = proceedAlpha), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, suffix: String?, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(LightCard)
            .border(1.5.dp, LightCardBorder, RoundedCornerShape(11.dp))
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.W700, color = TextPrimary)
            if (suffix != null) Text(suffix, fontSize = 11.sp, color = TextTertiary)
        }
    }
}
