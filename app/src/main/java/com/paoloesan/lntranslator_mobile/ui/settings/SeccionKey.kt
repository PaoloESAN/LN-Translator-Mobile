package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SeccionKey(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    companion object {
        private const val MAX_API_KEYS = 5
        private const val PREFS_KEY = "api_keys_list"
    }

    private val prefs = contexto.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val apiKeys = mutableStateListOf<String>()

    init {
        loadApiKeys()
    }

    private fun loadApiKeys() {
        apiKeys.clear()
        val jsonString = prefs.getString(PREFS_KEY, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val keys: List<String> = Gson().fromJson(jsonString, type)
                apiKeys.addAll(keys)
            } catch (_: Exception) {
            }
        }
        if (apiKeys.isEmpty()) {
            apiKeys.add("")
        }
    }

    @Composable
    override fun ContenidoModal() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Puedes agregar hasta $MAX_API_KEYS claves API para rotar automáticamente:",
                style = MaterialTheme.typography.bodyMedium
            )

            apiKeys.forEachIndexed { index, key ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { newValue -> apiKeys[index] = newValue },
                        modifier = Modifier.weight(1f),
                        label = { Text("API Key ${index + 1}") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    if (apiKeys.size > 1) {
                        IconButton(
                            onClick = { apiKeys.removeAt(index) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar API Key",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (apiKeys.size < MAX_API_KEYS) {
                FilledTonalButton(
                    onClick = { apiKeys.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " Agregar otra API Key",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            val linkColor = MaterialTheme.colorScheme.primary
            val annotatedText = buildAnnotatedString {
                append("Consigue tu clave aquí: ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://aistudio.google.com/app/apikey",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("Google AI Studio")
                }
            }
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Text(
                text = "Las claves rotarán automáticamente si una falla o alcanza su límite.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    override fun guardarCambios(cerrarModal: () -> Unit) {
        val validKeys = apiKeys.filter { it.isNotBlank() }
        prefs.edit {
            putString(PREFS_KEY, Gson().toJson(validKeys))
            putInt("api_key_index", 0)
        }
        cerrarModal()
    }
}