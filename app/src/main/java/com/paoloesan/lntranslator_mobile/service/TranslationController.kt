package com.paoloesan.lntranslator_mobile.service

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.TranslationService
import com.paoloesan.lntranslator_mobile.ui.novels.components.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranslationUiState(
    val traducciones: List<String> = emptyList(),
    val indiceActual: Int = -1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNovel: String? = null
) {
    val textoActual: String?
        get() = if (indiceActual >= 0 && indiceActual < traducciones.size) {
            traducciones[indiceActual]
        } else null

    val total: Int get() = traducciones.size

    val puedeIrAnterior: Boolean get() = indiceActual > 0
    val puedeIrSiguiente: Boolean get() = indiceActual < traducciones.size - 1
}

class TranslationController(
    private val translationService: TranslationService,
    private val context: Context
) {
    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState = _uiState.asStateFlow()

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
        if (key == "selected_novel") {
            val selected = p.getString(key, null)
            _uiState.value = _uiState.value.copy(selectedNovel = selected)
        }
    }

    init {
        val selected = prefs.getString("selected_novel", null)
        _uiState.value = _uiState.value.copy(selectedNovel = selected)
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    fun release() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    fun traducirCaptura(bitmap: Bitmap, scope: LifecycleCoroutineScope) {
        if (_uiState.value.isLoading) return

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resultado = translationService.translate(bitmap)

                when (resultado) {
                    is TranslationResult.Success -> {
                        val novelName = _uiState.value.selectedNovel
                        if (novelName != null) {
                            NovelRepository.saveTranslation(
                                context,
                                novelName,
                                resultado.translatedText,
                                bitmap = bitmap
                            )
                        }

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
