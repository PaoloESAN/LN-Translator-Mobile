package com.paoloesan.lntranslator_mobile.ui.overlay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.R
import com.paoloesan.lntranslator_mobile.service.TranslationUiState
import com.paoloesan.lntranslator_mobile.ui.utils.applyExtraBoldToMarkdown
import com.paoloesan.lntranslator_mobile.ui.utils.escapeAngleBrackets
import dev.jeziellago.compose.markdowntext.MarkdownText


@Composable
fun FloatingOverlayUI(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onExpand: (Boolean) -> Unit,
    onBottomPassThroughChange: (Boolean) -> Unit = {},
    onSideMarginDpChange: (Int) -> Unit = {},
    onTranslate: () -> Unit,
    onPreload: () -> Unit = {},
    uiState: TranslationUiState = TranslationUiState(),
    onAnterior: () -> Unit = {},
    onSiguiente: () -> Unit = {}
) {
    val logTag = "OverlayDiag"
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
    var currentFontFamily by remember {
        mutableStateOf(
            OverlayFontOption.fromPref(
                prefs.getString("overlay_font_family", OverlayFontOption.ROBOTO.prefValue)
            )
        )
    }
    var menuOpen by remember { mutableStateOf(false) }
    var configOpen by remember { mutableStateOf(false) }
    var invertGestures by remember {
        mutableStateOf(
            prefs.getBoolean("overlay_invert_gestures", false)
        )
    }
    var bottomPassThroughEnabled by remember {
        mutableStateOf(
            prefs.getBoolean("overlay_bottom_pass_through", false)
        )
    }
    var currentSideMarginDp by remember {
        mutableIntStateOf(
            prefs.getInt("overlay_side_margin_dp", 12)
        )
    }
    var showErrorDetails by remember { mutableStateOf(false) }
    val scrollState = key(uiState.indiceActual) { rememberScrollState() }

    LaunchedEffect(uiState.error, uiState.isLoading) {
        if (uiState.error == null || uiState.isLoading) {
            showErrorDetails = false
        }
    }

    if (!menuOpen) {
        IconButton(
            onClick = {
                menuOpen = true
                onExpand(true)
                onBottomPassThroughChange(bottomPassThroughEnabled)
                onSideMarginDpChange(currentSideMarginDp)
                Log.d(logTag, "UI open overlay menuOpen=$menuOpen bottomPassThrough=$bottomPassThroughEnabled")
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
                .padding(
                    horizontal = if (bottomPassThroughEnabled) 0.dp else 8.dp,
                    vertical = 8.dp
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.95f
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val leftAction = if (invertGestures) onSiguiente else onAnterior
                    val leftEnabled = if (invertGestures) uiState.puedeIrSiguiente else uiState.puedeIrAnterior
                    val leftDescription = if (invertGestures) strings.overlayNext else strings.overlayPrevious
                    val rightAction = if (invertGestures) onAnterior else onSiguiente
                    val rightEnabled = if (invertGestures) uiState.puedeIrAnterior else uiState.puedeIrSiguiente
                    val rightDescription = if (invertGestures) strings.overlayPrevious else strings.overlayNext

                    Text(
                        text = when {
                            uiState.error != null -> strings.overlayErrorTitle
                            uiState.isLoading && uiState.total > 0 -> "${uiState.indiceActual + 1}/${uiState.total} ..."
                            uiState.total > 0 -> "${uiState.indiceActual + 1}/${uiState.total}"
                            else -> strings.overlayTitle
                        },
                        color = when {
                            uiState.error != null -> MaterialTheme.colorScheme.error
                            uiState.isLoading -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable(enabled = uiState.error != null) {
                                showErrorDetails = !showErrorDetails
                            }
                    )
                    IconButton(
                        onClick = leftAction,
                        modifier = Modifier.size(24.dp),
                        enabled = leftEnabled
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = leftDescription,
                            tint = if (leftEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    }
                    IconButton(
                        onClick = {
                            showErrorDetails = false
                            onTranslate()
                        },
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
                        onClick = rightAction,
                        modifier = Modifier.size(24.dp),
                        enabled = rightEnabled
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = rightDescription,
                            tint = if (rightEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
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
                        Log.d(logTag, "UI close overlay menuOpen=$menuOpen")
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
                        invertGestures = invertGestures,
                        bottomPassThroughEnabled = bottomPassThroughEnabled,
                        currentSideMarginDp = currentSideMarginDp,
                        currentFontFamily = currentFontFamily,
                        onFontSizeChange = { newSize ->
                            currentFontSize = newSize
                            prefs.edit { putInt("overlay_font_size", newSize) }
                        },
                        onLineSpacingChange = { newSpacing ->
                            currentLineSpacing = newSpacing
                            prefs.edit { putInt("overlay_line_spacing", newSpacing) }
                        },
                        onInvertGesturesChange = { inverted ->
                            invertGestures = inverted
                            prefs.edit { putBoolean("overlay_invert_gestures", inverted) }
                        },
                        onBottomPassThroughChange = { enabled ->
                            Log.d(
                                logTag,
                                "UI toggle switch old=$bottomPassThroughEnabled new=$enabled configOpen=$configOpen"
                            )
                            bottomPassThroughEnabled = enabled
                            prefs.edit { putBoolean("overlay_bottom_pass_through", enabled) }
                            onBottomPassThroughChange(enabled)
                        },
                        onSideMarginDpChange = { newMarginDp ->
                            val clamped = newMarginDp.coerceIn(0, 32)
                            currentSideMarginDp = clamped
                            prefs.edit { putInt("overlay_side_margin_dp", clamped) }
                            onSideMarginDpChange(clamped)
                        },
                        onFontFamilyChange = { newFontFamily ->
                            currentFontFamily = newFontFamily
                            prefs.edit { putString("overlay_font_family", newFontFamily.prefValue) }
                        },
                        onClose = onClose,
                        onBack = { configOpen = false }
                    )
                } else {
                    var swipeAccumulator by remember { mutableFloatStateOf(0f) }
                    val swipeThreshold = 100f

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .pointerInput(uiState.puedeIrAnterior, uiState.puedeIrSiguiente) {
                                detectHorizontalDragGestures(
                                    onDragStart = { swipeAccumulator = 0f },
                                    onDragEnd = {
                                        when {
                                            swipeAccumulator < -swipeThreshold -> {
                                                val action = if (invertGestures) onAnterior else onSiguiente
                                                val canDo = if (invertGestures) uiState.puedeIrAnterior else uiState.puedeIrSiguiente
                                                if (canDo) action()
                                            }
                                            swipeAccumulator > swipeThreshold -> {
                                                val action = if (invertGestures) onSiguiente else onAnterior
                                                val canDo = if (invertGestures) uiState.puedeIrSiguiente else uiState.puedeIrAnterior
                                                if (canDo) action()
                                            }
                                        }
                                        swipeAccumulator = 0f
                                    },
                                    onDragCancel = { swipeAccumulator = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        swipeAccumulator += dragAmount
                                    }
                                )
                            }
                            .padding(12.dp)
                    ) {
                        when {
                            uiState.isLoading && uiState.total == 0 -> {
                                Text(
                                    text = strings.overlayLoading,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp,
                                    fontFamily = currentFontFamily.toComposeFontFamily()
                                )
                            }

                            uiState.error != null && showErrorDetails -> {
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp,
                                    fontFamily = currentFontFamily.toComposeFontFamily()
                                )
                            }

                            uiState.textoActual != null -> {
                                MarkdownText(
                                    markdown = escapeAngleBrackets(uiState.textoActual!!),
                                    fontResource = R.font.roboto_regular,
                                    afterSetMarkdown = { textView ->
                                        textView.typeface = currentFontFamily.toAndroidTypeface(context)
                                        if (currentFontFamily == OverlayFontOption.ROBOTO) {
                                            applyExtraBoldToMarkdown(
                                                textView,
                                                context
                                            )
                                        }
                                    },
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = currentFontSize.sp,
                                        lineHeight = (currentFontSize + currentLineSpacing).sp,
                                        fontFamily = currentFontFamily.toComposeFontFamily()
                                    )
                                )
                            }

                            else -> {
                                Text(
                                    text = strings.overlayHelp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = currentFontSize.sp,
                                    lineHeight = (currentFontSize + currentLineSpacing).sp,
                                    fontFamily = currentFontFamily.toComposeFontFamily()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
