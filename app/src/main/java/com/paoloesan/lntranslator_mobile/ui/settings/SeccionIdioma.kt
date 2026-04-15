package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
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
import com.paoloesan.lntranslator_mobile.translation.prompts.TranslationPrompts
import java.util.Locale

class SeccionIdioma(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    private val prefs = contexto.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private var seleccionActual by mutableStateOf(
        prefs.getString("idioma_app", null) ?: getSystemLanguage()
    )

    private fun getSystemLanguage(): String {
        val systemLang = Locale.getDefault().language
        return if (systemLang.startsWith("es", ignoreCase = true)) "Español" else "English"
    }

    private fun seleccionarIdioma(idioma: String): Boolean {
        if (seleccionActual == idioma) return false
        seleccionActual = idioma
        prefs.edit { putString("idioma_app", idioma) }
        return true
    }

    @Composable
    override fun ContenidoModal(solicitarCierre: (() -> Unit)?) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            seleccionActual = prefs.getString("idioma_app", null) ?: getSystemLanguage()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            val opciones = TranslationPrompts.getAvailableLanguages()
            opciones.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        {
                            seleccionarIdioma(opcion)
                            solicitarCierre?.invoke()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (opcion == seleccionActual),
                        onClick = {
                            seleccionarIdioma(opcion)
                            solicitarCierre?.invoke()
                        }
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
        cerrarModal()
    }
}