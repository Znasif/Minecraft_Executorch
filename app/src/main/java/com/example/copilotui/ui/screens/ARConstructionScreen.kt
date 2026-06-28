package com.example.copilotui.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*
import com.example.copilotui.ui.viewmodel.ConstructionViewModel

@Composable
fun ARConstructionScreen(
    viewModel: ConstructionViewModel,
    onClose: () -> Unit,
    onVerify: () -> Unit,
    onHelp: () -> Unit,
) {
    val arrowAlpha by rememberInfiniteTransition(label = "arrow").animateFloat(
        0.5f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "aa"
    )

    val stepIndex = viewModel.currentStepIndex
    val stepTitle = viewModel.buildSteps.getOrElse(stepIndex) { "Unknown Step" }
    val totalSteps = viewModel.buildSteps.size
    val progress = (stepIndex + 1).toFloat() / totalSteps

    var arState by remember { mutableStateOf(ArScanState()) }
    val dimensions = arState.primaryPlane?.let {
        "${"%.1f".format(it.widthMeters)} m × ${"%.1f".format(it.heightMeters)} m"
    } ?: "3.6 m × 2.4 m"

    Box(modifier = Modifier.fillMaxSize()) {

        // Live camera feed replaces hatch background
        ArCameraView(
            modifier = Modifier.fillMaxSize(),
            onStateChange = { arState = it },
        )

        StatusBar(dark = true, modifier = Modifier.align(Alignment.TopStart))

        // Step header + close
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 42.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(0.55f))
                    .padding(horizontal = 13.dp, vertical = 7.dp),
            ) {
                Text(
                    "STEP ${stepIndex + 1} OF $totalSteps",
                    fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = Color(0xFFBFC0BB),
                )
                Text(stepTitle, fontSize = 13.sp, fontWeight = FontWeight.W600, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.55f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
        }

        // Placement guide
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 150.dp, height = 160.dp)
                .drawBehind {
                    drawRect(
                        Color.White.copy(0.85f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.ArrowUpward, null,
                tint = Color.White.copy(arrowAlpha),
                modifier = Modifier.size(44.dp),
            )
            Text(
                "place wall here",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.6f))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W500,
                color = Color.White,
            )
            Text(
                dimensions,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.6f))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W500,
                color = Color.White,
            )
        }

        // AI help FAB
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, top = 80.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onHelp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF1B1C20), modifier = Modifier.size(24.dp))
        }

        // Voice instruction
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(0.55f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(Icons.Rounded.VolumeUp, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(
                "\"Align the frame with the guide, then anchor the base plate.\"",
                fontSize = 11.sp,
                fontWeight = FontWeight.W500,
                color = Color.White,
                lineHeight = 15.sp,
            )
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF101114).copy(0.92f))
                .border(width = 1.dp, color = Color.White.copy(0.08f), shape = RoundedCornerShape(0.dp))
                .padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress bar driven by currentStepIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable {
                            val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            viewModel.verifyCurrentStep(bitmap)
                            onVerify()
                        }
                        .padding(13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Icon(Icons.Rounded.Check, null, tint = Color(0xFF1B1C20), modifier = Modifier.size(18.dp))
                        Text("Verify Step", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1B1C20))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onHelp)
                        .padding(13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Help, null, tint = Color(0xFFF0F0EE), modifier = Modifier.size(16.dp))
                        Text("Help", fontSize = 12.sp, fontWeight = FontWeight.W600, color = Color(0xFFF0F0EE))
                    }
                }
            }
        }
    }
}
