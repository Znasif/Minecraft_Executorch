package com.example.copilotui.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.copilotui.verification.VlmEngine
import com.example.copilotui.verification.VlmResult
import kotlinx.coroutines.launch

class ConstructionViewModel(app: Application) : AndroidViewModel(app) {

    val buildSteps = listOf(
        "Place Corner Posts",
        "Raise Walls",
        "Install Roof Frame",
        "Secure & Finish",
    )

    var currentStepIndex by mutableIntStateOf(0)
        private set

    private val vlmEngine = VlmEngine(app)

    private var verificationCount = 0
    private val inferenceTimes = mutableListOf<Long>()

    val avgInferenceMs: Long
        get() = if (inferenceTimes.isEmpty()) 0L else inferenceTimes.average().toLong()

    val totalVerifications: Int
        get() = verificationCount

    var isVerifying by mutableStateOf(false)
        private set

    var lastVerificationResult by mutableStateOf<VlmResult?>(null)
        private set

    var verificationError by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch { vlmEngine.load() }
    }

    fun verifyCurrentStep(bitmap: Bitmap) {
        viewModelScope.launch {
            isVerifying = true
            verificationError = null
            try {
                val result = vlmEngine.verify(
                    bitmap = bitmap,
                    stepTitle = buildSteps[currentStepIndex],
                )
                lastVerificationResult = result
                verificationCount++
                inferenceTimes.add(result.inferenceTimeMs)
            } catch (e: Exception) {
                verificationError = "Verification failed: ${e.message}"
            } finally {
                isVerifying = false
            }
        }
    }

    fun advanceStep() {
        if (currentStepIndex < buildSteps.size - 1) {
            currentStepIndex++
            lastVerificationResult = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        vlmEngine.close()
    }
}
