package com.example.copilotui.ui.screens

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.copilotui.ui.theme.*
import com.example.copilotui.ui.viewmodel.ConstructionViewModel
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session

@Composable
fun ARConstructionScreen(
    viewModel: ConstructionViewModel,
    onClose: () -> Unit,
    onVerify: () -> Unit,
    onHelp: () -> Unit,
) {
    val stepIndex = viewModel.currentStepIndex
    val stepTitle = viewModel.buildSteps.getOrElse(stepIndex) { "Unknown Step" }
    val totalSteps = viewModel.buildSteps.size
    val progress = (stepIndex + 1).toFloat() / totalSteps

    var arState by remember { mutableStateOf(ArScanState()) }
    var arSession by remember { mutableStateOf<Session?>(null) }
    var arFrame by remember { mutableStateOf<Frame?>(null) }
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    val dimensions = arState.primaryPlane?.let {
        "${"%.1f".format(it.widthMeters)} m × ${"%.1f".format(it.heightMeters)} m"
    } ?: "3.6 m × 2.4 m"

    Box(modifier = Modifier.fillMaxSize()) {

        ArCameraView(
            modifier = Modifier.fillMaxSize(),
            onStateChange = { arState = it },
            onFrameAvailable = { session, frame ->
                arSession = session
                arFrame = frame
            },
            onSurfaceReady = { glSurfaceView ->
                glSurfaceView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && anchor == null) {
                        arFrame?.let { frame ->
                            val hits = frame.hitTest(event.x, event.y)
                            val hit = hits.firstOrNull {
                                it.trackable is Plane &&
                                (it.trackable as Plane).isPoseInPolygon(it.hitPose)
                            }
                            hit?.let { anchor = it.createAnchor() }
                        }
                    }
                    true
                }
            },
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

        if (anchor == null) {
            // Tap prompt
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(0.6f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    "👆 Tap floor to place structure",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W500,
                    color = Color.White,
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val currentAnchor = anchor
                val currentFrame = arFrame
                val screenW = constraints.maxWidth.toFloat()
                val screenH = constraints.maxHeight.toFloat()
                val anchorScreen = if (currentAnchor != null && currentFrame != null) {
                    projectAnchorToScreen(currentAnchor, currentFrame, screenW, screenH)
                } else {
                    Offset(screenW / 2f, screenH / 2f)
                }
                StepGhostOverlay(stepIndex = stepIndex, centerOffset = anchorScreen)
                Box(
                    modifier = Modifier
                        .absoluteOffset {
                            IntOffset(
                                (anchorScreen.x - 80.dp.toPx()).toInt(),
                                (anchorScreen.y + 100.dp.toPx()).toInt(),
                            )
                        }
                        .width(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        dimensions,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(0.6f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.W500,
                        color = Color.White,
                    )
                }
            }
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

@Composable
private fun StepGhostOverlay(stepIndex: Int, centerOffset: Offset) {
    val infiniteTransition = rememberInfiniteTransition(label = "ghost")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    AnimatedContent(
        targetState = stepIndex,
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
        label = "stepOverlay",
    ) { step ->
        Box(
            modifier = Modifier
                .absoluteOffset {
                    IntOffset(
                        (centerOffset.x - 170.dp.toPx()).toInt(),
                        (centerOffset.y - 90.dp.toPx()).toInt(),
                    )
                }
                .size(width = 340.dp, height = 180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val teal = Color(0xFF00BCD4)
                val orange = Color(0xFFFF6B35)
                val green = Color(0xFF4CAF50)
                val cx = size.width / 2f
                val cy = size.height / 2f
                val sw = 260.dp.toPx()
                val sh = 80.dp.toPx()
                val sk = 40.dp.toPx()
                val wallLift = 45.dp.toPx()
                val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))

                // Isometric parallelogram: flat slab centered at (cx, cy), shifted up by yOff
                fun slabPath(yOff: Float = 0f) = Path().apply {
                    moveTo(cx - sw / 2 + sk, cy - sh / 2 + yOff)
                    lineTo(cx + sw / 2 + sk, cy - sh / 2 + yOff)
                    lineTo(cx + sw / 2 - sk, cy + sh / 2 + yOff)
                    lineTo(cx - sw / 2 - sk, cy + sh / 2 + yOff)
                    close()
                }

                // Parallelogram corner points at a given y-offset
                fun corners(yOff: Float = 0f) = listOf(
                    Offset(cx - sw / 2 + sk, cy - sh / 2 + yOff),
                    Offset(cx + sw / 2 + sk, cy - sh / 2 + yOff),
                    Offset(cx + sw / 2 - sk, cy + sh / 2 + yOff),
                    Offset(cx - sw / 2 - sk, cy + sh / 2 + yOff),
                )

                when (step) {
                    0 -> { // Mark Foundation
                        drawPath(slabPath(), teal.copy(alpha = 0.15f))
                        drawPath(slabPath(), teal.copy(alpha = 0.7f),
                            style = Stroke(width = 2.dp.toPx()))
                    }
                    1 -> { // Place Corner Posts
                        drawPath(slabPath(), teal.copy(alpha = 0.10f))
                        drawPath(slabPath(), teal.copy(alpha = 0.5f),
                            style = Stroke(width = 1.5.dp.toPx()))
                        corners().forEach { pt ->
                            drawCircle(teal.copy(alpha = pulse), radius = 10.dp.toPx(), center = pt)
                        }
                    }
                    2 -> { // Raise Walls
                        drawPath(slabPath(), teal.copy(alpha = 0.10f))
                        drawPath(slabPath(), teal.copy(alpha = 0.4f),
                            style = Stroke(width = 1.5.dp.toPx()))
                        drawPath(slabPath(-wallLift), teal.copy(alpha = 0.7f),
                            style = Stroke(width = 2.dp.toPx(), pathEffect = dash))
                        corners().zip(corners(-wallLift)).forEach { (base, top) ->
                            drawLine(teal.copy(0.5f), base, top,
                                strokeWidth = 1.5.dp.toPx(), pathEffect = dash)
                        }
                    }
                    3 -> { // Install Roof Frame
                        drawPath(slabPath(), teal.copy(alpha = 0.08f))
                        drawPath(slabPath(-wallLift), teal.copy(alpha = 0.3f),
                            style = Stroke(width = 1.dp.toPx()))
                        val roofCorners = corners(-wallLift)
                        drawLine(orange.copy(0.85f), roofCorners[0], roofCorners[2],
                            strokeWidth = 2.5.dp.toPx(), pathEffect = dash)
                        drawLine(orange.copy(0.85f), roofCorners[1], roofCorners[3],
                            strokeWidth = 2.5.dp.toPx(), pathEffect = dash)
                    }
                    4 -> { // Secure & Finish
                        drawPath(slabPath(), green.copy(alpha = 0.15f))
                        drawPath(slabPath(), green.copy(alpha = 0.7f),
                            style = Stroke(width = 2.dp.toPx()))
                        val checkPath = Path().apply {
                            moveTo(cx - 30.dp.toPx(), cy)
                            lineTo(cx - 5.dp.toPx(), cy + 20.dp.toPx())
                            lineTo(cx + 30.dp.toPx(), cy - 20.dp.toPx())
                        }
                        drawPath(checkPath, green.copy(alpha = 0.9f),
                            style = Stroke(width = 4.dp.toPx(),
                                cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            if (step == 0) {
                Text(
                    "FOUNDATION",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W600,
                    color = Color(0xFF00BCD4).copy(alpha = 0.9f),
                )
            }
        }
    }
}

private fun projectAnchorToScreen(
    anchor: Anchor,
    frame: Frame,
    screenWidth: Float,
    screenHeight: Float,
): Offset {
    val projMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    frame.camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f)
    frame.camera.getViewMatrix(viewMatrix, 0)

    val worldPos = anchor.pose.translation
    val viewPos = FloatArray(4)
    val clipSpace = FloatArray(4)

    android.opengl.Matrix.multiplyMV(
        viewPos, 0, viewMatrix, 0,
        floatArrayOf(worldPos[0], worldPos[1], worldPos[2], 1f), 0,
    )
    android.opengl.Matrix.multiplyMV(
        clipSpace, 0, projMatrix, 0, viewPos, 0,
    )

    val ndcX = clipSpace[0] / clipSpace[3]
    val ndcY = clipSpace[1] / clipSpace[3]

    return Offset(
        (ndcX + 1f) / 2f * screenWidth,
        (1f - ndcY) / 2f * screenHeight,
    )
}
