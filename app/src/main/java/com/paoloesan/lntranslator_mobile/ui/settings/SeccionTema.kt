package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

class SeccionTema(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    private val prefs = contexto.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private var seleccionActual by mutableStateOf(
        prefs.getString(
            "tema_app",
            "Predeterminado del sistema"
        )
    )

    @Composable
    override fun ContenidoModal() {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            seleccionActual = prefs.getString("tema_app", "Predeterminado del sistema")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            val opciones = listOf("Predeterminado del sistema", "Claro", "Oscuro")
            opciones.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        {
                            seleccionActual = opcion
                        },

                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (opcion == seleccionActual),
                        onClick = { seleccionActual = opcion }
                    )
                    Text(
                        text = opcion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                }
            }
        }
    }

    override fun guardarCambios(cerrarModal: () -> Unit) {
        prefs.edit { putString("tema_app", seleccionActual) }

        val modo = when (seleccionActual) {
            "Claro" -> AppCompatDelegate.MODE_NIGHT_NO
            "Oscuro" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(modo)
        cerrarModal()
    }
}