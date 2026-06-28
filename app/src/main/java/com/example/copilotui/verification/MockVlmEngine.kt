package com.example.copilotui.verification

import android.graphics.Bitmap
import kotlinx.coroutines.delay

class MockVlmEngine {

    // Simulates VLM inference latency on Hexagon NPU
    // Replace this entire class with real ExecuTorch engine when .pte arrives
    // Input contract: Bitmap + step title
    // Output contract: VlmResult
    // Prompt used will be:
    // "Look at this image. Current step: {stepTitle}. Is this step complete? Answer YES or NO only:"

    suspend fun verify(bitmap: Bitmap, stepTitle: String): VlmResult {
        delay(340L) // Simulated NPU inference time
        return VlmResult(
            verdict = true,
            confidence = 0.89f,
            rawResponse = "YES",
            inferenceTimeMs = 340L,
            modelInfo = "SmolVLM | Hexagon NPU | ExecuTorch"
        )
    }
}

data class VlmResult(
    val verdict: Boolean,
    val confidence: Float,
    val rawResponse: String,
    val inferenceTimeMs: Long,
    val modelInfo: String
)
