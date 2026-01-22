package com.paoloesan.lntranslator_mobile.service

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import com.paoloesan.lntranslator_mobile.api.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranslationUiState(
    val traducciones: List<String> = emptyList(),
    val indiceActual: Int = -1,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val textoActual: String?
        get() = if (indiceActual >= 0 && indiceActual < traducciones.size) {
            traducciones[indiceActual]
        } else null

    val total: Int get() = traducciones.size

    val puedeIrAnterior: Boolean get() = indiceActual > 0
    val puedeIrSiguiente: Boolean get() = indiceActual < traducciones.size - 1
}

class TranslationController(private val geminiClient: GeminiClient) {
    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState = _uiState.asStateFlow()

    fun traducirCaptura(bitmap: Bitmap, scope: LifecycleCoroutineScope) {
        if (_uiState.value.isLoading) return

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resultado = geminiClient.generateFromImage(bitmap)
                val nuevaLista = _uiState.value.traducciones + resultado
                val nuevoIndice = if (_uiState.value.indiceActual == -1) {
                    0
                } else {
                    _uiState.value.indiceActual
                }
                _uiState.value = _uiState.value.copy(
                    traducciones = nuevaLista,
                    indiceActual = nuevoIndice,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun irAnterior() {
        if (_uiState.value.puedeIrAnterior) {
            _uiState.value = _uiState.value.copy(
                indiceActual = _uiState.value.indiceActual - 1
            )
        }
    }

    fun irSiguiente() {
        if (_uiState.value.puedeIrSiguiente) {
            _uiState.value = _uiState.value.copy(
                indiceActual = _uiState.value.indiceActual + 1
            )
        }
    }

    fun limpiarHistorial() {
        _uiState.value = TranslationUiState()
    }

    fun notificarError(mensaje: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = mensaje
        )
    }
}
