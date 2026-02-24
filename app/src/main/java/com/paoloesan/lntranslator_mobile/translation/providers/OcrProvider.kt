package com.paoloesan.lntranslator_mobile.translation.providers

import android.util.Log
import com.paoloesan.lntranslator_mobile.translation.ImageTranslationRequest
import com.paoloesan.lntranslator_mobile.translation.ModelClient
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.ocr.JapaneseOcrExtractor
import com.paoloesan.lntranslator_mobile.translation.ocr.OcrResult

/**
 * Proveedor OCR: captura pantalla → OCR local → envía texto al modelo.
 */
class OcrProvider(
    private val client: ModelClient
) : TranslationProvider {

    override val providerId: String = "ocr_${client.modelId}"
    override val displayName: String = "OCR + ${client.displayName}"

    private val ocrExtractor = JapaneseOcrExtractor()

    override fun isConfigured(): Boolean = client.isConfigured()

    override suspend fun translateImage(request: ImageTranslationRequest): TranslationResult {
        if (!isConfigured()) {
            return TranslationResult.Error(
                message = "Configura tu API Key de ${client.displayName} en ajustes",
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }

        Log.d(TAG, "=== INICIANDO OCR LOCAL + ${client.displayName.uppercase()} ===")

        when (val ocrResult = ocrExtractor.extractText(request.bitmap)) {
            is OcrResult.Error -> {
                return TranslationResult.Error(
                    message = ocrResult.message,
                    errorType = TranslationResult.ErrorType.EMPTY_RESPONSE
                )
            }

            is OcrResult.Success -> {
                val extractedText = ocrResult.extractedText
                Log.d(
                    TAG,
                    "Texto extraído (${extractedText.length} chars): ${extractedText.take(100)}..."
                )
                return client.translateText(extractedText, request.prompt)
            }
        }
    }

    companion object {
        private const val TAG = "OcrProvider"
    }
}
