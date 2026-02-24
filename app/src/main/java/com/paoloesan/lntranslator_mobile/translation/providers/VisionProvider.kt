package com.paoloesan.lntranslator_mobile.translation.providers

import com.paoloesan.lntranslator_mobile.translation.ImageTranslationRequest
import com.paoloesan.lntranslator_mobile.translation.ModelClient
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.TranslationResult

/**
 * Proveedor de visión: captura pantalla → envía imagen al modelo.
 */
class VisionProvider(
    private val client: ModelClient
) : TranslationProvider {

    override val providerId: String = client.modelId
    override val displayName: String = client.displayName

    override fun isConfigured(): Boolean = client.isConfigured()

    override suspend fun translateImage(request: ImageTranslationRequest): TranslationResult {
        if (!isConfigured()) {
            return TranslationResult.Error(
                message = "Configura tu API Key de ${client.displayName} en ajustes",
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }
        return client.translateWithImage(request.bitmap, request.prompt)
    }
}
