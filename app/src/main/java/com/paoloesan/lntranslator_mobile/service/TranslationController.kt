package com.paoloesan.lntranslator_mobile.service

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import com.paoloesan.lntranslator_mobile.translation.TranslationResult
import com.paoloesan.lntranslator_mobile.translation.TranslationService
import com.paoloesan.lntranslator_mobile.ui.novels.components.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import com.paoloesan.lntranslator_mobile.data.DataStoreManager

data class TranslationUiState(
    val traducciones: List<String> = emptyList(),
    val indiceActual: Int = -1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNovel: String? = null,
    val isSavingIllustration: Boolean = false,
    val showIllustrationSavedCheck: Boolean = false
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

    private val controllerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        controllerScope.launch {
            DataStoreManager.getStringFlow(context, "selected_novel").collect { selected ->
                _uiState.value = _uiState.value.copy(selectedNovel = selected)
            }
        }
    }

    fun release() {
        controllerScope.cancel()
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

    fun setSavingIllustration(saving: Boolean) {
        _uiState.value = _uiState.value.copy(isSavingIllustration = saving)
    }

    fun guardarIlustracion(bitmap: Bitmap, scope: LifecycleCoroutineScope, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val novelName = _uiState.value.selectedNovel
        if (novelName == null) {
            onError("Error: selectedNovel is null")
            return
        }
        scope.launch {
            try {
                NovelRepository.saveTranslation(context, novelName, "", bitmap = bitmap)
                onSuccess()
                _uiState.value = _uiState.value.copy(showIllustrationSavedCheck = true)
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(showIllustrationSavedCheck = false)
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Error")
            }
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
