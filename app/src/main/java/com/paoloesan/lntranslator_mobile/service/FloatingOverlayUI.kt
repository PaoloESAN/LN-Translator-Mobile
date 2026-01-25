package com.paoloesan.lntranslator_mobile.service

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.LocalStrings

@Composable
fun FloatingOverlayUI(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onExpand: (Boolean) -> Unit,
    onTranslate: () -> Unit,
    onPreload: () -> Unit = {},
    uiState: TranslationUiState = TranslationUiState(),
    onAnterior: () -> Unit = {},
    onSiguiente: () -> Unit = {}
) {
    val strings = LocalStrings.current
    var menuOpen by remember { mutableStateOf(false) }

    if (!menuOpen) {
        IconButton(
            onClick = {
                menuOpen = true
                onExpand(true)
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                },
        ) {
            Icon(
                Icons.Rounded.Translate,
                contentDescription = strings.overlayOpen,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE8151528)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33FFFFFF))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when {
                            uiState.isLoading && uiState.total > 0 -> "${uiState.indiceActual + 1}/${uiState.total} ..."
                            uiState.total > 0 -> "${uiState.indiceActual + 1}/${uiState.total}"
                            else -> strings.overlayTitle
                        },
                        color = if (uiState.isLoading) Color.Cyan else Color.White,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = onAnterior,
                        modifier = Modifier.size(24.dp),
                        enabled = uiState.puedeIrAnterior
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = strings.overlayPrevious,
                            tint = if (uiState.puedeIrAnterior) Color.White else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onTranslate,
                        modifier = Modifier.size(24.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Rounded.Translate,
                            contentDescription = strings.overlayTranslate,
                            tint = if (!uiState.isLoading) Color.White else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onSiguiente,
                        modifier = Modifier.size(24.dp),
                        enabled = uiState.puedeIrSiguiente
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = strings.overlayNext,
                            tint = if (uiState.puedeIrSiguiente) Color.White else Color.Gray
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.Cancel,
                            contentDescription = strings.overlayClose,
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        menuOpen = false
                        onExpand(false)
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.CloseFullscreen,
                            contentDescription = strings.overlayClose,
                            tint = Color.White
                        )
                    }
                }

                val scrollState = key(uiState.indiceActual) { rememberScrollState() }

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(scrollState)
                ) {
                    when {
                        uiState.isLoading && uiState.total == 0 -> {
                            Text(
                                text = strings.overlayLoading,
                                color = Color.Cyan,
                                fontSize = 13.sp
                            )
                        }

                        uiState.error != null -> {
                            Text(
                                text = uiState.error,
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                        }

                        uiState.textoActual != null -> {
                            Text(
                                text = uiState.textoActual!!,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }

                        else -> {
                            Text(
                                text = strings.overlayHelp,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
