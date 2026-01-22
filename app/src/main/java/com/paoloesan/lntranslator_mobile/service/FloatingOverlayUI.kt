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

@Composable
fun FloatingOverlayUI(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onExpand: (Boolean) -> Unit,
    onTranslate: () -> Unit,
    uiState: TranslationUiState = TranslationUiState(),
    onAnterior: () -> Unit = {},
    onSiguiente: () -> Unit = {}
) {
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
                contentDescription = "Abrir Traductor",
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
                        text = if (uiState.total > 0) "${uiState.indiceActual + 1}/${uiState.total}" else "Traductor",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = onAnterior,
                        modifier = Modifier.size(24.dp),
                        enabled = uiState.puedeIrAnterior
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Anterior",
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
                            contentDescription = "Traducir",
                            tint = if (!uiState.isLoading) Color.White else Color.Gray
                        )
                    }/*
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(24.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Rounded.SkipNext,
                            contentDescription = "Precargar",
                            tint = if (!uiState.isLoading) Color.White else Color.Gray
                        )
                    }*/
                    IconButton(
                        onClick = onSiguiente,
                        modifier = Modifier.size(24.dp),
                        enabled = uiState.puedeIrSiguiente
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Siguiente",
                            tint = if (uiState.puedeIrSiguiente) Color.White else Color.Gray
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.Cancel,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        menuOpen = false
                        onExpand(false)
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.CloseFullscreen,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when {
                        uiState.isLoading -> {
                            Text(
                                text = "Traduciendo...",
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
                                text = "Presiona el botón de traducir para capturar la pantalla...",
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

