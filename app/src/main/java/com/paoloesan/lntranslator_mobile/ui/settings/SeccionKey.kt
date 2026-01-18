package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

class SeccionKey(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    private val prefs = contexto.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private var apikey by mutableStateOf(
        prefs.getString(
            "apikey_app",
            ""
        )
    )

    @Composable
    override fun ContenidoModal() {

        androidx.compose.runtime.LaunchedEffect(Unit) {
            apikey = prefs.getString("apikey_app", "")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Aquí puedes introducir tu clave privada para el traductor:",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = apikey ?: "",
                onValueChange = { apikey = it },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Consigue tu clave aqui: https://aistudio.google.com/app/apikey",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    override fun guardarCambios(cerrarModal: () -> Unit) {
        prefs.edit { putString("apikey_app", apikey) }
        cerrarModal()
    }
}