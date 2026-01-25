package com.paoloesan.lntranslator_mobile.translation

import android.graphics.Bitmap

interface TranslationProvider {
    val providerId: String
    val displayName: String
    suspend fun translateImage(request: ImageTranslationRequest): TranslationResult
    fun isConfigured(): Boolean
}

data class ImageTranslationRequest(
    val bitmap: Bitmap,
    val prompt: String,
    val sourceLanguage: String = "ja",
    val targetLanguage: String = "es"
)

sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()

    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.UNKNOWN,
        val isRetryable: Boolean = false
    ) : TranslationResult()

    enum class ErrorType {
        NO_API_KEY,
        INVALID_API_KEY,
        RATE_LIMITED,
        NETWORK_ERROR,
        INVALID_IMAGE,
        MODEL_OVERLOADED,
        EMPTY_RESPONSE,
        UNKNOWN
    }
}
