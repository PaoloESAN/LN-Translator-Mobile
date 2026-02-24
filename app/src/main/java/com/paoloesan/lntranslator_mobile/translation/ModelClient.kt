package com.paoloesan.lntranslator_mobile.translation

import android.graphics.Bitmap

/**
 * Interfaz unificada para clientes de modelos de IA.
 *
 * Cada proveedor de API (Gemini, ChatGPT, Groq, etc.) implementa esta interfaz UNA sola vez.
 * - Modelos con visión (Gemini, ChatGPT): supportsVision = true, implementan ambos métodos.
 * - Modelos solo texto (Groq): supportsVision = false, solo implementan translateText().
 */
interface ModelClient {
    val modelId: String
    val displayName: String
    val supportsVision: Boolean get() = false

    fun isConfigured(): Boolean
    
    suspend fun translateText(extractedText: String, prompt: String): TranslationResult

    suspend fun translateWithImage(bitmap: Bitmap, prompt: String): TranslationResult {
        return TranslationResult.Error(
            message = "$displayName no soporta traducción por imagen",
            errorType = TranslationResult.ErrorType.UNKNOWN
        )
    }
}
