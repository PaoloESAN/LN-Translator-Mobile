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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.translation.ProviderFactory

class SeccionProveedor(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    companion object {
        private const val PREF_ACTIVE_PROVIDER = "active_translation_provider"
        private const val DEFAULT_PROVIDER = "gemini"
    }

    private val prefs = contexto.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val providerFactory = ProviderFactory(contexto)

    private var seleccionActual by mutableStateOf(
        prefs.getString(PREF_ACTIVE_PROVIDER, DEFAULT_PROVIDER) ?: DEFAULT_PROVIDER
    )

    private data class ProviderOption(
        val id: String,
        val displayName: String
    )

    private val opcionesProveedores: List<ProviderOption> = providerFactory.getAllProviders().map {
        ProviderOption(it.providerId, it.displayName)
    }

    @Composable
    override fun ContenidoModal() {
        LaunchedEffect(Unit) {
            seleccionActual =
                prefs.getString(PREF_ACTIVE_PROVIDER, DEFAULT_PROVIDER) ?: DEFAULT_PROVIDER
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            opcionesProveedores.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            seleccionActual = opcion.id
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (opcion.id == seleccionActual),
                        onClick = { seleccionActual = opcion.id }
                    )
                    Text(
                        text = opcion.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }

    override fun guardarCambios(cerrarModal: () -> Unit) {
        prefs.edit { putString(PREF_ACTIVE_PROVIDER, seleccionActual) }
        cerrarModal()
    }
}