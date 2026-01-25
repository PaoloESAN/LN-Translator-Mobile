package com.paoloesan.lntranslator_mobile.translation

import android.content.Context
import android.util.Log
import com.paoloesan.lntranslator_mobile.translation.gemini.GeminiProvider
import com.paoloesan.lntranslator_mobile.translation.local.LocalOcrGeminiProvider

class ProviderFactory(private val context: Context) {

    private val providers = mutableMapOf<String, TranslationProvider>()

    companion object {
        private const val TAG = "ProviderFactory"
    }

    init {
        registerProviders()
    }

    private fun registerProviders() {
        register(GeminiProvider(context))
        register(LocalOcrGeminiProvider(context))
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
