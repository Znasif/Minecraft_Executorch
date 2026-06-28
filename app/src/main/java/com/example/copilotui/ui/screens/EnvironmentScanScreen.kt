package com.example.copilotui.ui.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.copilotui.ui.theme.*

@Composable
fun EnvironmentScanScreen(onFinish: () -> Unit) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    var arState by remember { mutableStateOf(ArScanState()) }

    val dotAlpha by rememberInfiniteTransition(label = "d").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "da"
    )

    val progressPct = (arState.scanProgress * 100).toInt().coerceIn(0, 100)
    val primary = arState.primaryPlane
    val rulerLabel = if (primary != null) "%.1f m".format(primary.widthMeters) else "– m"

    Box(modifier = Modifier.fillMaxSize()) {

        ArCameraView(
            modifier = Modifier.fillMaxSize(),
            onStateChange = { arState = it },
        )

        Text(
            if (primary != null) "floor plane · ${"%.1f".format(primary.widthMeters)} × ${"%.1f".format(primary.heightMeters)} m" else "floor plane · scanning…",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 66.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(0.55f))
                .padding(horizontal = 7.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.W500,
            color = Color.White,
        )

        StatusBar(dark = true, modifier = Modifier.align(Alignment.TopStart))

        // Scanning badge
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 42.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(0.5f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(dotAlpha))
            )
            Text(
                if (arState.isTracking) "TRACKING" else "SCANNING…",
                fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W500, color = Color.White,
            )
        }

        // Progress circle (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 16.dp)
                .size(46.dp)
                .drawBehind {
                    drawCircle(Color.White.copy(0.25f), radius = size.minDimension / 2 - 2, style = Stroke(3.dp.toPx()))
                    drawArc(Color.White, -90f, progressPct / 100f * 360f, false, style = Stroke(3.dp.toPx()))
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("$progressPct%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.W600, color = Color.White)
        }

        // Ruler badge
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 30.dp)
                .offset(y = 30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(0.55f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(Icons.Rounded.Straighten, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(rulerLabel, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.White)
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp, 0.dp, 16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Move slowly to map the area",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W500,
                color = Color(0xFFC9CAC5),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White)
                    .clickable(onClick = onFinish)
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Check, null, tint = Color(0xFF1B1C20), modifier = Modifier.size(18.dp))
                    Text("Finish Scan", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1B1C20))
                }
            }
        }
    }
}

private class PermissionState(
    val status: PermissionStatus,
    private val requestPermission: () -> Unit,
) {
    fun launchPermissionRequest() = requestPermission()
}

private data class PermissionStatus(val isGranted: Boolean)

@Composable
private fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    var isGranted by remember(permission) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isGranted = granted
    }

    return PermissionState(
        status = PermissionStatus(isGranted),
        requestPermission = { launcher.launch(permission) },
    )
}
