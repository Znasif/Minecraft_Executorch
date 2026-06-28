package com.example.copilotui.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.copilotui.ar.helpers.CameraPermissionHelper
import com.example.copilotui.ar.helpers.DisplayRotationHelper
import com.example.copilotui.ar.samplerender.SampleRender
import com.example.copilotui.ar.samplerender.arcore.BackgroundRenderer
import com.example.copilotui.ar.samplerender.arcore.PlaneRenderer
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.SessionPausedException
import java.io.IOException

private const val TAG = "ArCameraView"
private const val Z_NEAR = 0.1f
private const val Z_FAR = 100f

@Composable
fun ArCameraView(
    modifier: Modifier = Modifier,
    onStateChange: (ArScanState) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context.findActivity()
    val sessionHelper = remember(activity) {
        activity?.let {
            ARCoreSessionLifecycleHelper(it).apply {
                beforeSessionResume = ::configureSession
                exceptionCallback = { exception ->
                    Log.e(TAG, "ARCore session error", exception)
                }
            }
        }
    }
    val renderer = remember(sessionHelper) {
        sessionHelper?.let { ArPlaneRenderer(context, it, onStateChange) }
    }
    val glSurfaceView = remember { GLSurfaceView(context) }

    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView.onPause()
            sessionHelper?.session?.pause()
        }
    }

    DisposableEffect(lifecycleOwner, sessionHelper, renderer) {
        if (sessionHelper != null && renderer != null) {
            lifecycleOwner.lifecycle.addObserver(sessionHelper)
            lifecycleOwner.lifecycle.addObserver(renderer)
        }
        onDispose {
            if (sessionHelper != null && renderer != null) {
                lifecycleOwner.lifecycle.removeObserver(renderer)
                lifecycleOwner.lifecycle.removeObserver(sessionHelper)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            glSurfaceView.also { surfaceView ->
                renderer?.let { SampleRender(surfaceView, it, context.assets) }
            }
        },
    )
}

private fun configureSession(session: Session) {
    session.configure(
        session.config.apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            lightEstimationMode = Config.LightEstimationMode.DISABLED
            depthMode = Config.DepthMode.DISABLED
        }
    )
}

private class ArPlaneRenderer(
    context: Context,
    private val sessionHelper: ARCoreSessionLifecycleHelper,
    private val onStateChange: (ArScanState) -> Unit,
) : SampleRender.Renderer, DefaultLifecycleObserver {
    private val displayRotationHelper = DisplayRotationHelper(context)
    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var planeRenderer: PlaneRenderer
    private val projectionMatrix = FloatArray(16)
    private var hasSetTextureNames = false

    override fun onResume(owner: LifecycleOwner) {
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        displayRotationHelper.onPause()
    }

    override fun onSurfaceCreated(render: SampleRender) {
        try {
            backgroundRenderer = BackgroundRenderer(render)
            backgroundRenderer.setUseDepthVisualization(render, false)
            backgroundRenderer.setUseOcclusion(render, false)
            planeRenderer = PlaneRenderer(render)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to initialize AR renderers", e)
        }
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        displayRotationHelper.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(render: SampleRender) {
        val session = sessionHelper.session ?: return

        if (!hasSetTextureNames) {
            session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
            hasSetTextureNames = true
        }

        displayRotationHelper.updateSessionIfNeeded(session)

        val frame = try {
            session.update()
        } catch (e: SessionPausedException) {
            return
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available during AR frame", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "Frame error: ${e.message}")
            return
        }
        val camera = frame.camera

        backgroundRenderer.updateDisplayGeometry(frame)
        if (frame.timestamp != 0L) {
            backgroundRenderer.drawBackground(render)
        }

        val planes = session.getAllTrackables(Plane::class.java)
            .filter {
                it.trackingState == TrackingState.TRACKING &&
                    it.subsumedBy == null &&
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
            }
        val detectedPlanes = planes.map { it.toDetectedPlane() }
        val primary = detectedPlanes.maxByOrNull { it.widthMeters * it.heightMeters }
        val progress = (detectedPlanes.size.coerceAtMost(5) / 5f).coerceIn(0f, 1f)

        onStateChange(
            ArScanState(
                isTracking = camera.trackingState == TrackingState.TRACKING,
                scanProgress = progress,
                planes = detectedPlanes,
                primaryPlane = primary,
            )
        )

        if (camera.trackingState != TrackingState.TRACKING) {
            return
        }

        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)
        planeRenderer.drawPlanes(
            render,
            session.getAllTrackables(Plane::class.java),
            camera.displayOrientedPose,
            projectionMatrix,
        )
    }
}

private class ARCoreSessionLifecycleHelper(
    private val activity: Activity,
    private val features: Set<Session.Feature> = emptySet(),
) : DefaultLifecycleObserver {
    var installRequested = false
    var session: Session? = null
        private set
    var exceptionCallback: ((Exception) -> Unit)? = null
    var beforeSessionResume: ((Session) -> Unit)? = null

    private fun tryCreateSession(): Session? {
        if (!CameraPermissionHelper.hasCameraPermission(activity)) {
            CameraPermissionHelper.requestCameraPermission(activity)
            return null
        }

        return try {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    installRequested = true
                    return null
                }
                ArCoreApk.InstallStatus.INSTALLED -> Unit
            }
            Session(activity, features)
        } catch (e: Exception) {
            exceptionCallback?.invoke(e)
            null
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        val activeSession = session ?: tryCreateSession() ?: return
        try {
            beforeSessionResume?.invoke(activeSession)
            activeSession.resume()
            session = activeSession
        } catch (e: CameraNotAvailableException) {
            exceptionCallback?.invoke(e)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        session?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        session?.close()
        session = null
    }

    fun onRequestPermissionsResult() {
        if (!CameraPermissionHelper.hasCameraPermission(activity)) {
            Toast.makeText(activity, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(activity)) {
                CameraPermissionHelper.launchPermissionSettings(activity)
            }
            activity.finish()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
