package com.paoloesan.lntranslator_mobile.translation.models

interface TextTranslationModel {
    val modelId: String
    val displayName: String

    fun isConfigured(): Boolean

    suspend fun translateText(
        japaneseText: String,
        contextPrompt: String
    ): TextTranslationResult
}

sealed class TextTranslationResult {
    data class Success(val translatedText: String) : TextTranslationResult()
    data class Error(val message: String, val isRetryable: Boolean = false) : TextTranslationResult()
}
