package com.paoloesan.lntranslator_mobile.ui.prompts

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PromptScreen(
    context: Context,
    onPromptSelected: (String) -> Unit
) {
    val strings = LocalStrings.current
    val promptsList = remember { mutableStateListOf<PromptData>() }
    var borrarDialog by remember { mutableStateOf(false) }
    var editarDialog by remember { mutableStateOf(false) }
    var indexSeleccionado by remember { mutableIntStateOf(-1) }
    var indexEditando by remember { mutableIntStateOf(-1) }
    var tituloEditado by remember { mutableStateOf("") }
    var descripcionEditada by remember { mutableStateOf("") }
    var cargandoPrompts by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val prompts = withContext(Dispatchers.IO) {
            Prompt.obtenerPrompts(context)
        }
        promptsList.clear()
        promptsList.addAll(prompts)
        cargandoPrompts = false
    }

    if (borrarDialog) {
        AlertDialog(
            onDismissRequest = { borrarDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { borrarDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(strings.buttonCancel)
                    }
                    Button(
                        onClick = {
                            if (indexSeleccionado in promptsList.indices) {
                                Prompt.eliminarPrompt(indexSeleccionado, context)
                                promptsList.removeAt(indexSeleccionado)
                            }
                            borrarDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(strings.buttonDelete)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        strings.deletePromptTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        )
    }

    if (editarDialog) {
        val tituloNormalizado = tituloEditado.trim()
        val descripcionNormalizada = descripcionEditada.trim()
        val tituloValido = tituloNormalizado.isNotBlank()
        val descripcionValida = descripcionNormalizada.isNotBlank()
        val puedeGuardar = tituloValido && descripcionValida

        AlertDialog(
            title = {
                Text(strings.editPromptTitle, style = MaterialTheme.typography.headlineSmall)
            },
            onDismissRequest = { editarDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { editarDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(strings.buttonCancel)
                    }
                    Button(
                        onClick = {
                            if (puedeGuardar && indexEditando in promptsList.indices) {
                                val actualizado = PromptData(
                                    tituloNormalizado,
                                    descripcionNormalizada
                                )
                                Prompt.actualizarPrompt(indexEditando, actualizado, context)
                                promptsList[indexEditando] = actualizado
                                editarDialog = false
                            }
                        },
                        enabled = puedeGuardar,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        label = { Text(strings.promptTitleLabel) },
                        value = tituloEditado,
                        onValueChange = { tituloEditado = it },
                        isError = !tituloValido,
                        supportingText = {
                            if (!tituloValido) {
                                Text(strings.promptTitleRequired)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        label = { Text(strings.promptDescriptionLabel) },
                        value = descripcionEditada,
                        onValueChange = { descripcionEditada = it },
                        isError = !descripcionValida,
                        supportingText = {
                            if (!descripcionValida) {
                                Text(strings.promptContextRequired)
                            }
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (!cargandoPrompts && promptsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalIconButton(
                        onClick = {},
                        enabled = false,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    text = strings.promptsEmptyTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = strings.promptsEmptySubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            itemsIndexed(promptsList) { index, prompt ->
                val shape = listItemShape(index, promptsList.size)
                Card(
                    onClick = { onPromptSelected(prompt.descripcion) },
                    shape = shape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                prompt.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                prompt.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = {
                                    indexEditando = index
                                    tituloEditado = prompt.titulo
                                    descripcionEditada = prompt.descripcion
                                    editarDialog = true
                                },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = strings.editPromptContentDescription,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            FilledIconButton(
                                onClick = {
                                    indexSeleccionado = index
                                    borrarDialog = true
                                },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = strings.deletePromptContentDescription,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun listItemShape(index: Int, total: Int): Shape {
    val radius = 20.dp
    val noRadius = 4.dp
    return when {
        total <= 1 -> RoundedCornerShape(radius)
        index == 0 -> RoundedCornerShape(
            topStart = radius,
            topEnd = radius,
            bottomStart = noRadius,
            bottomEnd = noRadius
        )

        index == total - 1 -> RoundedCornerShape(
            bottomStart = radius,
            bottomEnd = radius,
            topStart = noRadius,
            topEnd = noRadius
        )

        else -> RoundedCornerShape(noRadius)
    }
}
