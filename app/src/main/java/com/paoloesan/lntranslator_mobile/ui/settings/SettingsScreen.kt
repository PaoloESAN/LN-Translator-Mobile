package com.paoloesan.lntranslator_mobile.ui.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    val secciones = listOf(
        SeccionProveedor(
            Icons.Outlined.Api,
            strings.settingsProviderTitle,
            strings.settingsProviderDescription,
            context
        ),
        SeccionKey(
            Icons.Outlined.Key,
            strings.settingsApikeyTitle,
            strings.settingsApikeyDescription,
            context
        ),
        SeccionTema(
            Icons.Outlined.BrightnessMedium,
            strings.settingsThemeTitle,
            strings.settingsThemeDescription,
            context
        ),
        SeccionIdioma(
            Icons.Outlined.Translate,
            strings.settingsLanguageTitle,
            strings.settingsLanguageDescription,
            context
        ),
    )

    var seccionSeleccionada by remember { mutableStateOf<Seccion?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        secciones.forEach { seccionData ->
            SeccionRow(
                seccionData.icono,
                seccionData.titulo,
                seccionData.descripcion,
                { seccionSeleccionada = seccionData }
            )
        }
    }

    if (seccionSeleccionada != null) {
        AlertDialog(
            title = {
                Text(seccionSeleccionada!!.titulo, style = MaterialTheme.typography.headlineSmall)
            },
            onDismissRequest = { seccionSeleccionada = null },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { seccionSeleccionada = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(strings.buttonClose)
                    }
                    Button(
                        onClick = {
                            seccionSeleccionada?.guardarCambios({
                                seccionSeleccionada = null
                            })
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(strings.buttonSave)
                    }
                }
            },
            text = {
                seccionSeleccionada?.ContenidoModal()
            }
        )
    }
}

@Composable
fun SeccionRow(icono: ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
