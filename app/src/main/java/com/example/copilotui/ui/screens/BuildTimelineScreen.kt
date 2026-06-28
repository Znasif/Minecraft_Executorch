package com.example.copilotui.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*

private enum class StepState { DONE, ACTIVE, PENDING }
private data class Step(val title: String, val meta: String, val state: StepState)

@Composable
fun BuildTimelineScreen(onBack: () -> Unit, onResume: () -> Unit) {
    val steps = listOf(
        Step("Foundation", "2 days · 100%", StepState.DONE),
        Step("Frame", "3 days · 100%", StepState.DONE),
        Step("Walls", "~4 days · 35%", StepState.ACTIVE),
        Step("Roof", "~3 days · 0%", StepState.PENDING),
        Step("Finish", "~2 days · 0%", StepState.PENDING),
    )
    val pulseAlpha by rememberInfiniteTransition(label = "p").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "pa"
    )

    Column(modifier = Modifier.fillMaxSize().background(LightBg)) {
        StatusBar()

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text("Build Timeline", fontSize = 17.sp, fontWeight = FontWeight.W600, color = TextPrimary)
            Text("Backyard Cabin · 40% complete", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = TextSecondary)
        }

        // Timeline
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Vertical line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = 14.dp)
                    .background(TimelineLineBg)
            )
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                steps.forEach { step ->
                    TimelineStep(step = step, pulseAlpha = pulseAlpha)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 10.dp, 16.dp, 18.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(CtaDark)
                .clickable(onClick = onResume)
                .padding(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resume Build (Walls)", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color.White)
                Icon(Icons.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TimelineStep(step: Step, pulseAlpha: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        // Node
        when (step.state) {
            StepState.DONE -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(TimelineDone),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            StepState.ACTIVE -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(LightCard)
                        .border(2.dp, CtaDark, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(CtaDark.copy(alpha = pulseAlpha))
                    )
                }
            }
            StepState.PENDING -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(LightCard)
                        .border(2.dp, Color(0xFFD2D2C9), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFC4C4BA), CircleShape)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val textColor = if (step.state == StepState.PENDING) TextTertiary else TextPrimary
                Text(step.title, fontSize = 14.sp, fontWeight = FontWeight.W600, color = textColor)
                if (step.state == StepState.ACTIVE) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(ProgressBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("IN PROGRESS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W600, color = InProgressText)
                    }
                }
            }
            val metaColor = if (step.state == StepState.PENDING) TextMuted else TextTertiary
            Text(step.meta, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = metaColor)
        }
    }
}
