package com.example.copilotui.ar

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.util.concurrent.Executors

@Composable
fun ArCameraView(
    modifier: Modifier = Modifier,
    onUpdate: (isTracking: Boolean, planes: List<PlaneInfo>) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val arSession = remember {
        runCatching {
            Session(context).apply {
                configure(
                    Config(this).apply {
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                    }
                )
            }
        }.getOrNull()
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )

    LaunchedEffect(context, lifecycleOwner, previewView, arSession) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            onUpdate(false, emptyList())
            return@LaunchedEffect
        }

        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(executor) { imageProxy ->
                    val session = arSession
                    if (session == null) {
                        onUpdate(false, emptyList())
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    runCatching {
                        val frame = synchronized(session) { session.update() }
                        val isTracking = frame.camera.trackingState == TrackingState.TRACKING
                        val planes = session
                            .getAllTrackables(Plane::class.java)
                            .filter {
                                it.trackingState == TrackingState.TRACKING &&
                                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                            }
                            .map { PlaneInfo(it.extentX, it.extentZ) }
                        onUpdate(isTracking, planes)
                    }.onFailure {
                        onUpdate(false, emptyList())
                    }

                    imageProxy.close()
                }
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis,
        )
    }

    DisposableEffect(arSession) {
        onDispose {
            executor.shutdown()
            arSession?.close()
        }
    }
}
