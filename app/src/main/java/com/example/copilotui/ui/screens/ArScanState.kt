package com.example.copilotui.ui.screens

import com.google.ar.core.Plane

data class DetectedPlane(
    val widthMeters: Float,
    val heightMeters: Float,
)

data class ArScanState(
    val isTracking: Boolean = false,
    val scanProgress: Float = 0f,
    val planes: List<DetectedPlane> = emptyList(),
    val primaryPlane: DetectedPlane? = null,
)

internal fun Plane.toDetectedPlane() = DetectedPlane(
    widthMeters = extentX,
    heightMeters = extentZ,
)
