package com.paoloesan.lntranslator_mobile.service

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.TranslationService
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

class TranslationController(private val translationService: TranslationService) {
    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState = _uiState.asStateFlow()

    fun traducirCaptura(bitmap: Bitmap, scope: LifecycleCoroutineScope) {
        if (_uiState.value.isLoading) return

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resultado = translationService.translate(bitmap)

                when (resultado) {
                    is TranslationResult.Success -> {
                        val nuevaLista = _uiState.value.traducciones + resultado.translatedText
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
                    }

                    is TranslationResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resultado.message
                        )
                    }
                }
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
