package com.paoloesan.lntranslator_mobile.service

import android.graphics.Bitmap
import com.paoloesan.lntranslator_mobile.api.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranslationController(private val geminiClient: GeminiClient) {
    private val _uiState = MutableStateFlow<String?>(null)
    val uiState = _uiState.asStateFlow()

    fun traducirCaptura(bitmap: Bitmap, scope: androidx.lifecycle.LifecycleCoroutineScope) {
        scope.launch {
            try {
                val resultado = geminiClient.generateFromImage(bitmap)
                _uiState.value = resultado
            } catch (e: Exception) {
                _uiState.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}
