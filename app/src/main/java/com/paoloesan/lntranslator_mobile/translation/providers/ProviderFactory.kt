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
        val gemini3FlashClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3-flash-preview",
            modelId = "gemini_3_flash",
            displayName = "Gemini 3 flash"
        )

        val gemini31FlashLiteClient = GeminiClient(
            context = context,
            modelVersion = "gemini-3.1-flash-lite-preview",
            modelId = "gemini_31_flash",
            displayName = "Gemini 3.1 flash"
        )

        // Vision Register
        register(VisionProvider(gemini3FlashClient))
        register(VisionProvider(gemini31FlashLiteClient))

        // OCR + Model Register
        register(OcrProvider(gemini3FlashClient))
        register(OcrProvider(gemini31FlashLiteClient))
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
