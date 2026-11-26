package com.paoloesan.lntranslator_mobile.translation.providers

import android.content.Context
import android.util.Log
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.gemini.GeminiClient

class ProviderFactory(private val context: Context) {

    private val providers = mutableMapOf<String, TranslationProvider>()

    companion object {
        private const val TAG = "ProviderFactory"
        const val CUSTOM_MODEL_ID = "gemini_custom"
        const val CUSTOM_OCR_MODEL_ID = "ocr_gemini_custom"
        const val PREF_CUSTOM_MODEL_ID = "custom_gemini_model_id"
    }

    init {
        registerProviders()
    }

    private fun registerProviders() {
        val gemini31FlashLiteClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.1-flash-lite",
            modelId = "gemini_31_flash",
            displayName = "Gemini 3.1 Flash Lite",
            thinkingLevel = "minimal"
        )

        val gemini35FlashLiteClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.5-flash-lite",
            modelId = "gemini_35_flash_lite",
            displayName = "Gemini 3.5 Flash Lite",
            thinkingLevel = "minimal"
        )

        val gemini4HydrogenClient = GeminiClient(
            context = context,
            modelVersion = "gemini-4-hydrogen",
            modelId = "gemini_4_hydrogen",
            displayName = "Gemini 4 Hydrogen",
            thinkingLevel = "low"
        )

        // Vision Register
        register(VisionProvider(gemini31FlashLiteClient))
        register(VisionProvider(gemini35FlashLiteClient))
        register(VisionProvider(gemini4HydrogenClient))

        // OCR + Model Register
        register(OcrProvider(gemini31FlashLiteClient))
        register(OcrProvider(gemini35FlashLiteClient))
        register(OcrProvider(gemini4HydrogenClient))
    }

    private fun register(provider: TranslationProvider) {
        providers[provider.providerId] = provider
        Log.d(TAG, "Proveedor registrado: ${provider.displayName} (${provider.providerId})")
    }

    fun getProvider(providerId: String): TranslationProvider? {
        if (providerId == CUSTOM_MODEL_ID || providerId == CUSTOM_OCR_MODEL_ID) {
            val customModelVersion = DataStoreManager
                .getString(context, PREF_CUSTOM_MODEL_ID, "")
                ?.trim()
                .orEmpty()
            if (customModelVersion.isEmpty()) {
                Log.w(TAG, "Modelo personalizado vacío; usando respaldo.")
                return null
            }
            val inferredThinkingLevel = when {
                customModelVersion.contains("flash-lite") -> "minimal"
                customModelVersion.startsWith("gemini-4") ||
                    customModelVersion.startsWith("gemini-3.8") -> "low"
                else -> null
            }
            val customClient = GeminiClient(
                context = context,
                modelVersion = customModelVersion,
                modelId = CUSTOM_MODEL_ID,
                displayName = customModelVersion,
                thinkingLevel = inferredThinkingLevel
            )
            return if (providerId == CUSTOM_OCR_MODEL_ID) {
                OcrProvider(customClient)
            } else {
                VisionProvider(customClient)
            }
        }
        return providers[providerId]
    }

    fun getAllProviders(): List<TranslationProvider> {
        return providers.values.toList()
    }

    fun getConfiguredProviderIds(): List<String> {
        return providers.filter { it.value.isConfigured() }.keys.toList()
    }
}
