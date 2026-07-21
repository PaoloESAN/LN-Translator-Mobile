package com.paoloesan.lntranslator_mobile.translation.providers

import android.content.Context
import android.util.Log
import com.paoloesan.lntranslator_mobile.translation.TranslationProvider
import com.paoloesan.lntranslator_mobile.translation.gemini.GeminiClient

class ProviderFactory(private val context: Context) {

    private val providers = mutableMapOf<String, TranslationProvider>()

    companion object {
        private const val TAG = "ProviderFactory"
    }

    init {
        registerProviders()
    }

    private fun registerProviders() {
        val gemini31FlashLiteClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.1-flash-lite",
            modelId = "gemini_31_flash",
            displayName = "Gemini 3.1 Flash Lite"
        )

        val gemini35FlashLiteClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.5-flash-lite",
            modelId = "gemini_35_flash_lite",
            displayName = "Gemini 3.5 Flash Lite"
        )

        val gemini36FlashClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.6-flash",
            modelId = "gemini_36_flash",
            displayName = "Gemini 3.6 Flash"
        )

        // Vision Register
        register(VisionProvider(gemini31FlashLiteClient))
        register(VisionProvider(gemini35FlashLiteClient))
        register(VisionProvider(gemini36FlashClient))

        // OCR + Model Register
        register(OcrProvider(gemini31FlashLiteClient))
        register(OcrProvider(gemini35FlashLiteClient))
        register(OcrProvider(gemini36FlashClient))
    }

    private fun register(provider: TranslationProvider) {
        providers[provider.providerId] = provider
        Log.d(TAG, "Proveedor registrado: ${provider.displayName} (${provider.providerId})")
    }

    fun getProvider(providerId: String): TranslationProvider? {
        return providers[providerId]
    }

    fun getAllProviders(): List<TranslationProvider> {
        return providers.values.toList()
    }

    fun getConfiguredProviderIds(): List<String> {
        return providers.filter { it.value.isConfigured() }.keys.toList()
    }
}
