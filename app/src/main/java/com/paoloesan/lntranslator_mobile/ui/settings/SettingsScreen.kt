package com.paoloesan.lntranslator_mobile.ui.settings


import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.BrightnessMedium
//import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class SeccionData(
    val icono: ImageVector,
    val titulo: String,
    val descripcion: String
)

val secciones = listOf(
    SeccionData(
        icono = Icons.Outlined.Key,
        titulo = "API key",
        descripcion = "El API key que usará gemini."
    ),
    /*SeccionData(
        icono = Icons.Outlined.GeneratingTokens,
        titulo = "Modelo de IA",
        descripcion = "Modelo Seleccionado."
    ),*/
    SeccionData(
        icono = Icons.Outlined.BrightnessMedium,
        titulo = "Tema",
        descripcion = "Predeterminado del sistema/claro/oscuro"
    ),
    SeccionData(
        icono = Icons.Outlined.Translate,
        titulo = "Idioma",
        descripcion = "Idiomas: Español, Ingles."
    ),
)

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        secciones.forEach { seccionData ->
            Seccion(seccionData.icono, seccionData.titulo, seccionData.descripcion)
        }
    }
}

@Composable
fun Seccion(icono: ImageVector, titulo: String, descripcion: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
