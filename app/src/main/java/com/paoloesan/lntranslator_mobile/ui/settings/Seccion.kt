package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface Seccion {
    val icono: ImageVector
    val titulo: String
    val descripcion: String
    val contexto: Context

    @Composable
    fun ContenidoModal()

    fun guardarCambios(cerrarModal: () -> Unit)
}