package com.example.copilotui.ar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class PlaneInfo(val widthM: Float, val heightM: Float)

class ArScanState {
    var isTracking by mutableStateOf(false)
    var scanProgress by mutableFloatStateOf(0f)
    val planes = mutableStateListOf<PlaneInfo>()
    val primaryPlane: PlaneInfo?
        get() = planes.maxByOrNull { it.widthM * it.heightM }

    fun addPlanes(newPlanes: List<PlaneInfo>) {
        planes.clear()
        planes.addAll(newPlanes)
        if (newPlanes.isNotEmpty()) {
            scanProgress = (scanProgress + 0.05f).coerceAtMost(1f)
        }
    }
}

@Composable
fun rememberArScanState() = remember { ArScanState() }
