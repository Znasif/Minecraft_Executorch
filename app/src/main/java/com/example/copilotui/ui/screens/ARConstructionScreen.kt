package com.example.copilotui.ui.screens

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
                MinecraftAROverlay(stepIndex = stepIndex, centerOffset = anchorScreen)
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
private fun MinecraftAROverlay(stepIndex: Int, centerOffset: Offset) {
    val density = LocalDensity.current
    val bs   = with(density) { 56.dp.toPx() }
    val isoW = bs
    val isoH = bs * 0.5f
    val wallH = bs * 0.8f
    // Center the 3x2 footprint (avg col=1, avg row=0.5) at centerOffset
    val cx = centerOffset.x - 0.5f * isoW
    val cy = centerOffset.y - 1.5f * isoH

    fun blockPos(col: Int, row: Int, layer: Int = 0) = Offset(
        cx + (col - row) * isoW,
        cy + (col + row) * isoH - layer * wallH,
    )

    // Painter order: ascending (col+row) so back blocks draw first
    val ijFront = listOf(0 to 0, 1 to 0, 0 to 1, 2 to 0, 1 to 1, 2 to 1)

    val pillarProgress by animateFloatAsState(
        targetValue = if (stepIndex >= 0) 3f else 0f,
        animationSpec = tween(600),
        label = "pillar",
    )
    val sparkle by rememberInfiniteTransition(label = "sparkle").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "sp",
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 1 — Foundation, always visible
        Canvas(modifier = Modifier.fillMaxSize()) {
            for ((col, row) in ijFront) {
                val p = blockPos(col, row, 0)
                drawBlock(p.x, p.y, bs, isoH, wallH, Color(0xFFC8C8C8), Color(0xFF888888), Color(0xFF707070))
            }
        }

        // Layer 2 — Corner posts, rise up one block at a time
        AnimatedVisibility(visible = stepIndex >= 0, enter = fadeIn(tween(400))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxK = pillarProgress.toInt().coerceIn(0, 3)
                for ((ci, cj) in listOf(0 to 0, 2 to 0, 0 to 1, 2 to 1)) {
                    for (k in 1..maxK) {
                        val p = blockPos(ci, cj, k)
                        drawBlock(p.x, p.y, bs, isoH, wallH, Color(0xFFB8924A), Color(0xFF8B6914), Color(0xFF6B4F10))
                    }
                }
            }
        }

        // Layer 3 — Walls: back-middle only (front is the doorway)
        AnimatedVisibility(visible = stepIndex >= 1, enter = fadeIn(tween(400))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (k in 1..2) {
                    val p = blockPos(1, 1, k)
                    drawBlock(p.x, p.y, bs, isoH, wallH, Color(0xFFAAAAAA), Color(0xFF777777), Color(0xFF606060))
                }
            }
        }

        // Layer 4 — Roof, drops in from above
        AnimatedVisibility(
            visible = stepIndex >= 2,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { -it / 3 },
                animationSpec = tween(600),
            ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for ((col, row) in ijFront) {
                    val p = blockPos(col, row, 3)
                    drawBlock(p.x, p.y, bs, isoH, wallH, Color(0xFF6B4F10), Color(0xFF4A3520), Color(0xFF3A2510))
                }
            }
        }

        // Layer 5 — Door, windows, sparkles, label
        AnimatedVisibility(visible = stepIndex >= 3, enter = fadeIn(tween(400))) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dp = blockPos(1, 0, 0)
                    val dw = bs * 0.55f; val dh = wallH * 1.4f
                    drawRect(Color(0xFF3E2010),
                        topLeft = Offset(dp.x - dw * 0.5f, dp.y - dh),
                        size = Size(dw, dh))
                    for (k in 1..2) {
                        val wp = blockPos(1, 1, k)
                        drawRect(Color(0xFF87CEEB).copy(alpha = 0.8f),
                            topLeft = Offset(wp.x - bs * 0.28f, wp.y - wallH * 0.55f),
                            size = Size(bs * 0.45f, wallH * 0.4f))
                    }
                    listOf(
                        Offset(-80f, -130f), Offset(90f, -110f), Offset(0f, -170f),
                        Offset(-60f, -90f), Offset(70f, -80f),
                    ).forEachIndexed { idx, off ->
                        val alpha = ((sparkle + idx * 0.2f) % 1f)
                        drawCircle(Color(0xFFFFD700).copy(alpha = alpha), 5f, centerOffset + off)
                    }
                }
                Text(
                    "COMPLETE ✓",
                    modifier = Modifier.absoluteOffset(
                        x = with(density) { (centerOffset.x - 60.dp.toPx()).toDp() },
                        y = with(density) { (centerOffset.y - 180.dp.toPx()).toDp() },
                    ),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W700,
                    color = Color(0xFF4CAF50),
                )
            }
        }
    }
}

private fun DrawScope.drawBlock(
    x: Float, y: Float, size: Float,
    isoH: Float, wallH: Float,
    topColor: Color, frontColor: Color, sideColor: Color,
) {
    val s = size
    // x,y = front-top vertex (where top face meets front/right face tops)
    val top = Path().apply {
        moveTo(x, y - isoH)
        lineTo(x + s, y - isoH * 0.5f)
        lineTo(x, y)
        lineTo(x - s, y - isoH * 0.5f)
        close()
    }
    val front = Path().apply {
        moveTo(x - s, y - isoH * 0.5f)
        lineTo(x, y)
        lineTo(x, y + wallH)
        lineTo(x - s, y + wallH - isoH * 0.5f)
        close()
    }
    val right = Path().apply {
        moveTo(x, y)
        lineTo(x + s, y - isoH * 0.5f)
        lineTo(x + s, y + wallH - isoH * 0.5f)
        lineTo(x, y + wallH)
        close()
    }
    drawPath(top, topColor)
    drawPath(front, frontColor)
    drawPath(right, sideColor)
    val edge = Color.Black.copy(alpha = 0.3f)
    val stroke = Stroke(width = 1.5f)
    drawPath(top, edge, style = stroke)
    drawPath(front, edge, style = stroke)
    drawPath(right, edge, style = stroke)
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
