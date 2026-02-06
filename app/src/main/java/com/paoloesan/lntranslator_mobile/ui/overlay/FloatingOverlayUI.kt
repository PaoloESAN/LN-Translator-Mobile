package com.paoloesan.lntranslator_mobile.ui.overlay

import android.content.Context
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
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.R
import com.paoloesan.lntranslator_mobile.service.TranslationUiState
import com.paoloesan.lntranslator_mobile.ui.utils.applyExtraBoldToMarkdown
import dev.jeziellago.compose.markdowntext.MarkdownText


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
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(
            "settings_prefs",
            Context.MODE_PRIVATE
        )
    }
    var currentFontSize by remember {
        mutableIntStateOf(
            prefs.getInt(
                "overlay_font_size",
                18
            )
        )
    }
    var currentLineSpacing by remember {
        mutableIntStateOf(
            prefs.getInt(
                "overlay_line_spacing",
                5
            )
        )
    }
    var menuOpen by remember { mutableStateOf(false) }
    var configOpen by remember { mutableStateOf(false) }

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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.95f
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
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
                        color = if (uiState.isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                            tint = if (uiState.puedeIrAnterior) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
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
                            tint = if (!uiState.isLoading) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
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
                            tint = if (uiState.puedeIrSiguiente) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    }
                    IconButton(
                        onClick = { configOpen = !configOpen },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = strings.overlayConfig,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        menuOpen = false
                        onExpand(false)
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.CloseFullscreen,
                            contentDescription = strings.overlayClose,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (configOpen) {
                    ConfigOverlayContent(
                        currentFontSize = currentFontSize,
                        currentLineSpacing = currentLineSpacing,
                        onFontSizeChange = { newSize ->
                            currentFontSize = newSize
                            prefs.edit { putInt("overlay_font_size", newSize) }
                        },
                        onLineSpacingChange = { newSpacing ->
                            currentLineSpacing = newSpacing
                            prefs.edit { putInt("overlay_line_spacing", newSpacing) }
                        },
                        onClose = onClose,
                        onBack = { configOpen = false }
                    )
                } else {
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
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp
                                )
                            }

                            uiState.error != null -> {
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp
                                )
                            }

                            uiState.textoActual != null -> {
                                MarkdownText(
                                    markdown = uiState.textoActual!!,
                                    fontResource = R.font.roboto_regular,
                                    afterSetMarkdown = { textView ->
                                        applyExtraBoldToMarkdown(
                                            textView,
                                            context
                                        )
                                    },
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = currentFontSize.sp,
                                        lineHeight = (currentFontSize + currentLineSpacing).sp
                                    )
                                )
                            }

                            else -> {
                                Text(
                                    text = strings.overlayHelp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
