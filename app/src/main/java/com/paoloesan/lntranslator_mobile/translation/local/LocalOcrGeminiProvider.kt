package com.paoloesan.lntranslator_mobile.translation.local

import android.content.Context
import android.util.Log
import com.paoloesan.lntranslator_mobile.translation.ImageTranslationRequest
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.models.GeminiTextModel
import com.paoloesan.lntranslator_mobile.translation.models.TextTranslationModel
import com.paoloesan.lntranslator_mobile.translation.models.TextTranslationResult
import com.paoloesan.lntranslator_mobile.translation.ocr.JapaneseOcrExtractor
import com.paoloesan.lntranslator_mobile.translation.ocr.OcrResult

class LocalOcrGeminiProvider(context: Context) : TranslationProvider {

    override val providerId: String = "local_ocr_gemini"
    override val displayName: String = "OCR Local + Gemini"

    private val ocrExtractor = JapaneseOcrExtractor()
    private val textModel: TextTranslationModel = GeminiTextModel(context)

    companion object {
        private const val TAG = "LocalOcrGeminiProvider"
    }

    override fun isConfigured(): Boolean {
        return textModel.isConfigured()
    }

    override suspend fun translateImage(request: ImageTranslationRequest): TranslationResult {
        if (!isConfigured()) {
            return TranslationResult.Error(
                message = "Configura tu API Key de ${textModel.displayName} en ajustes",
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }

        Log.d(TAG, "=== INICIANDO OCR LOCAL + ${textModel.displayName.uppercase()} ===")

        when (val ocrResult = ocrExtractor.extractText(request.bitmap)) {
            is OcrResult.Error -> {
                return TranslationResult.Error(
                    message = ocrResult.message,
                    errorType = TranslationResult.ErrorType.EMPTY_RESPONSE
                )
            }

            is OcrResult.Success -> {
                val japaneseText = ocrResult.extractedText
                Log.d(
                    TAG,
                    "Texto extraído (${japaneseText.length} chars): ${japaneseText.take(100)}..."
                )

                val translationResult = textModel.translateText(japaneseText, request.prompt)

                return when (translationResult) {
                    is TextTranslationResult.Success -> {
                        TranslationResult.Success(translationResult.translatedText)
                    }

                    is TextTranslationResult.Error -> {
                        TranslationResult.Error(
                            message = translationResult.message,
                            errorType = if (translationResult.isRetryable)
                                TranslationResult.ErrorType.RATE_LIMITED
                            else
                                TranslationResult.ErrorType.UNKNOWN,
                            isRetryable = translationResult.isRetryable
                        )
                    }
                }
            }
        }
    }
}
