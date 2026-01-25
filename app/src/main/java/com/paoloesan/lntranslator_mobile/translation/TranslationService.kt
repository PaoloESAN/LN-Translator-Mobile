package com.paoloesan.lntranslator_mobile.translation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.edit

class TranslationService(private val context: Context) {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val providerFactory = ProviderFactory(context)

    companion object {
        private const val TAG = "TranslationService"
        private const val PREF_ACTIVE_PROVIDER = "active_translation_provider"
        private const val DEFAULT_PROVIDER = "gemini"
    }

    fun getActiveProvider(): TranslationProvider? {
        val providerId = prefs.getString(PREF_ACTIVE_PROVIDER, DEFAULT_PROVIDER) ?: DEFAULT_PROVIDER
        return providerFactory.getProvider(providerId)
    }

    fun setActiveProvider(providerId: String): Boolean {
        val provider = providerFactory.getProvider(providerId)
        return if (provider != null) {
            prefs.edit { putString(PREF_ACTIVE_PROVIDER, providerId) }
            Log.d(TAG, "Proveedor cambiado a: ${provider.displayName}")
            true
        } else {
            Log.e(TAG, "Proveedor no encontrado: $providerId")
            false
        }
    }

    fun getAvailableProviders(): List<TranslationProvider> {
        return providerFactory.getAllProviders()
    }

    suspend fun translate(bitmap: Bitmap): TranslationResult {

        val validationError = validateBitmap(bitmap)
        if (validationError != null) {
            return validationError
        }

        val provider = getActiveProvider()
        if (provider == null) {
            return TranslationResult.Error(
                message = "No hay proveedor de traducción configurado",
                errorType = TranslationResult.ErrorType.UNKNOWN
            )
        }

        if (!provider.isConfigured()) {
            return TranslationResult.Error(
                message = "Configura tu API Key en ajustes para ${provider.displayName}",
                errorType = TranslationResult.ErrorType.NO_API_KEY
            )
        }

        val userPrompt = prefs.getString("prompt_app", "") ?: ""

        val request = ImageTranslationRequest(
            bitmap = bitmap,
            prompt = userPrompt
        )

        Log.d(TAG, "Enviando traducción a ${provider.displayName}")
        return try {
            val result = provider.translateImage(request)
            when (result) {
                is TranslationResult.Success -> {
                    Log.d(TAG, "Traducción exitosa: ${result.translatedText.take(50)}...")
                }

                is TranslationResult.Error -> {
                    Log.e(TAG, "Error en traducción: ${result.message}")
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante traducción: ${e.localizedMessage}")
            TranslationResult.Error(
                message = "Error inesperado: ${e.localizedMessage}",
                errorType = TranslationResult.ErrorType.UNKNOWN
            )
        }
    }

    private fun validateBitmap(bitmap: Bitmap): TranslationResult.Error? {
        Log.d(TAG, "=== VALIDANDO BITMAP ===")
        Log.d(TAG, "Bitmap width: ${bitmap.width}, height: ${bitmap.height}")
        Log.d(TAG, "Bitmap config: ${bitmap.config}")
        Log.d(TAG, "Bitmap byteCount: ${bitmap.byteCount}")
        Log.d(TAG, "Bitmap isRecycled: ${bitmap.isRecycled}")

        return when {
            bitmap.isRecycled -> TranslationResult.Error(
                message = "Error: Bitmap fue reciclado antes de procesar",
                errorType = TranslationResult.ErrorType.INVALID_IMAGE
            )

            bitmap.width == 0 || bitmap.height == 0 -> TranslationResult.Error(
                message = "Error: Imagen capturada está vacía",
                errorType = TranslationResult.ErrorType.INVALID_IMAGE
            )

            else -> null
        }
    }
}
