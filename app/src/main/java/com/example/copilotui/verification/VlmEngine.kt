package com.example.copilotui.verification

import android.content.Context
import android.graphics.Bitmap
import com.facebook.soloader.SoLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule
import java.io.File
import java.nio.ByteBuffer

private const val BASE_PATH = "/data/local/tmp/llama"
private const val MODEL_PATH = "$BASE_PATH/hybrid_llama_qnn.pte"
private const val VISION_ENCODER_PATH = "$BASE_PATH/vision_encoder_qnn.pte"
private const val TOK_EMBEDDING_PATH = "$BASE_PATH/tok_embedding_qnn.pte"
private const val TOKENIZER_PATH = "$BASE_PATH/tokenizer.json"
private const val SEQ_LEN = 128
private const val IMAGE_SIZE = 336

class VlmEngine(private val context: Context) {

    private var module: LlmModule? = null
    private var useMock = false

    suspend fun load() = withContext(Dispatchers.IO) {
        try { SoLoader.init(context, false) } catch (_: Exception) {}

        val allPresent = listOf(MODEL_PATH, VISION_ENCODER_PATH, TOK_EMBEDDING_PATH, TOKENIZER_PATH)
            .all { File(it).exists() }
        if (!allPresent) {
            useMock = true
            return@withContext
        }
        try {
            module = LlmModule(
                LlmModule.MODEL_TYPE_TEXT_VISION,
                MODEL_PATH,
                TOKENIZER_PATH,
                0.0f,
                listOf(VISION_ENCODER_PATH, TOK_EMBEDDING_PATH),
            ).also { it.load() }
        } catch (_: Exception) {
            useMock = true
        }
    }

    suspend fun verify(bitmap: Bitmap, stepTitle: String): VlmResult {
        if (useMock || module == null) return mockResult()

        return withContext(Dispatchers.Default) {
            val mod = module!!
            val startMs = System.currentTimeMillis()

            val buf = bitmapToRgbBuffer(bitmap)
            mod.prefillImages(buf, IMAGE_SIZE, IMAGE_SIZE, 3)

            val prompt = "Is the construction step \"$stepTitle\" complete in this image? Reply YES or NO only."
            val response = StringBuilder()
            mod.generate(prompt, SEQ_LEN, object : LlmCallback {
                override fun onResult(token: String) { response.append(token) }
            }, false)

            mod.resetContext()

            val inferenceMs = System.currentTimeMillis() - startMs
            val raw = response.toString().trim()
            val verdict = raw.contains("YES", ignoreCase = true) &&
                    !raw.contains("NO", ignoreCase = true)

            VlmResult(
                verdict = verdict,
                confidence = if (verdict) 0.92f else 0.15f,
                rawResponse = raw.ifEmpty { "(no response)" },
                inferenceTimeMs = inferenceMs,
                modelInfo = "SmolVLM | Hexagon NPU | ExecuTorch",
            )
        }
    }

    fun close() {
        module?.close()
        module = null
    }

    private fun bitmapToRgbBuffer(bitmap: Bitmap): ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
        val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        scaled.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE)
        val buf = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3)
        for (px in pixels) {
            buf.put(((px shr 16) and 0xFF).toByte())
            buf.put(((px shr 8) and 0xFF).toByte())
            buf.put((px and 0xFF).toByte())
        }
        buf.rewind()
        return buf
    }

    private suspend fun mockResult(): VlmResult {
        delay(340L)
        return VlmResult(
            verdict = true,
            confidence = 0.89f,
            rawResponse = "YES (model files not found at $BASE_PATH — mock fallback)",
            inferenceTimeMs = 340L,
            modelInfo = "SmolVLM | Mock fallback",
        )
    }
}

data class VlmResult(
    val verdict: Boolean,
    val confidence: Float,
    val rawResponse: String,
    val inferenceTimeMs: Long,
    val modelInfo: String,
)
