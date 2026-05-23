package com.paoloesan.lntranslator_mobile.ui.novels

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TelephotoSwipeDismissImage(
    imageUrl: Any,
    onDismiss: () -> Unit,
    onClick: () -> Unit = {},
    onZoomedChanged: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val zoomableState = rememberZoomableImageState()

    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    val isFractionalZoomOne = (zoomableState.zoomableState.zoomFraction ?: 0f) < 0.1f
    val isZoomed = !isFractionalZoomOne

    LaunchedEffect(isZoomed) {
        onZoomedChanged(isZoomed)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .drawBehind {
                val swipeFraction = if (containerHeight > 0f) {
                    (abs(offsetY) / containerHeight).coerceIn(0f, 1f)
                } else {
                    0f
                }
                val backgroundAlpha = (1f - swipeFraction) * 0.95f
                drawRect(color = Color.Black.copy(alpha = backgroundAlpha))
            }
            .onSizeChanged { containerHeight = it.height.toFloat() },
        contentAlignment = Alignment.Center
    ) {
        ZoomableAsyncImage(
            model = imageUrl,
            contentDescription = "Imagen con Zoom y Swipe",
            state = zoomableState,
            contentScale = ContentScale.Fit,
            onClick = { onClick() },
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        // Modificamos el offset de manera síncrona para evitar lag
                        offsetY += delta
                    },
                    orientation = Orientation.Vertical,
                    // ¡CRUCIAL!: Solo permitimos el arrastre si la imagen NO tiene zoom
                    enabled = isFractionalZoomOne,
                    onDragStarted = {
                        // Cancelamos cualquier animación en curso si el usuario vuelve a arrastrar
                        animationJob?.cancel()
                    },
                    onDragStopped = { velocity ->
                        // Al soltar, verificamos si se deslizó lo suficiente para cerrar
                        val threshold = containerHeight * 0.25f // 25% de la pantalla
                        animationJob = scope.launch {
                            if (abs(offsetY) > threshold || abs(velocity) > 2000f) {
                                // Determinamos si sale hacia arriba o hacia abajo
                                val targetValue =
                                    if (offsetY > 0) containerHeight else -containerHeight
                                animate(
                                    initialValue = offsetY,
                                    targetValue = targetValue,
                                    initialVelocity = velocity,
                                    animationSpec = tween(durationMillis = 200)
                                ) { value, _ ->
                                    offsetY = value
                                }
                                onDismiss()
                            } else {
                                // Si no superó el umbral, regresa al centro de forma suave
                                animate(
                                    initialValue = offsetY,
                                    targetValue = 0f,
                                    initialVelocity = velocity,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) { value, _ ->
                                    offsetY = value
                                }
                            }
                        }
                    }
                )
        )
    }
}