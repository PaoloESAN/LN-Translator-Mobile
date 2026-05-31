package com.paoloesan.lntranslator_mobile.ui.novels.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropDialog(
    novelName: String,
    imagePath: String,
    onDismiss: () -> Unit,
    onConfirm: (croppedPath: String) -> Unit
) {
    val context = LocalContext.current

    // Cropper States
    var cropLeft by remember { mutableFloatStateOf(0f) }
    var cropTop by remember { mutableFloatStateOf(0f) }
    var cropRight by remember { mutableFloatStateOf(1f) }
    var cropBottom by remember { mutableFloatStateOf(1f) }
    var containerWidth by remember { mutableFloatStateOf(1f) }
    var containerHeight by remember { mutableFloatStateOf(1f) }
    var activeHandle by remember { mutableStateOf<DragHandle?>(null) }

    fun cropImage(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): String? {
        return try {
            val original = BitmapFactory.decodeFile(imagePath) ?: return null
            val x = (left * original.width).toInt().coerceIn(0, original.width - 1)
            val y = (top * original.height).toInt().coerceIn(0, original.height - 1)
            val w = ((right - left) * original.width).toInt().coerceIn(1, original.width - x)
            val h = ((bottom - top) * original.height).toInt().coerceIn(1, original.height - y)

            val cropped = Bitmap.createBitmap(original, x, y, w, h)
            val imagesDir = File(context.filesDir, "images_$novelName")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val fileName = "img_${System.currentTimeMillis()}_cropped.jpg"
            val file = File(imagesDir, fileName)
            FileOutputStream(file).use { out ->
                cropped.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            original.recycle()
            cropped.recycle()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recortar imagen",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = Color.White
                        )
                    }
                }

                // Image Cropping Canvas area
                val bitmap = remember(imagePath) {
                    try {
                        BitmapFactory.decodeFile(imagePath)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (bitmap != null) {
                    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .systemGestureExclusion()
                            .onGloballyPositioned { coordinates ->
                                containerWidth = coordinates.size.width.toFloat()
                                containerHeight = coordinates.size.height.toFloat()
                            }
                            .pointerInput(containerWidth, containerHeight, aspectRatio) {
                                detectDragGestures(
                                    onDragStart = { startPosition ->
                                        val density = this.density
                                        val handleTouchThreshold = 56f * density

                                        if (containerWidth > 1f && containerHeight > 1f) {
                                            val containerRatio = containerWidth / containerHeight
                                            val imageWidth =
                                                if (containerRatio > aspectRatio) containerHeight * aspectRatio else containerWidth
                                            val imageHeight =
                                                if (containerRatio > aspectRatio) containerHeight else containerWidth / aspectRatio
                                            val imageLeft = (containerWidth - imageWidth) / 2f
                                            val imageTop = (containerHeight - imageHeight) / 2f

                                            val localX = startPosition.x - imageLeft
                                            val localY = startPosition.y - imageTop

                                            val tlX = cropLeft * imageWidth
                                            val tlY = cropTop * imageHeight
                                            val trX = cropRight * imageWidth
                                            val trY = cropTop * imageHeight
                                            val blX = cropLeft * imageWidth
                                            val blY = cropBottom * imageHeight
                                            val brX = cropRight * imageWidth
                                            val brY = cropBottom * imageHeight

                                            val distTL =
                                                kotlin.math.hypot(localX - tlX, localY - tlY)
                                            val distTR =
                                                kotlin.math.hypot(localX - trX, localY - trY)
                                            val distBL =
                                                kotlin.math.hypot(localX - blX, localY - blY)
                                            val distBR =
                                                kotlin.math.hypot(localX - brX, localY - brY)

                                            activeHandle = when {
                                                distTL < handleTouchThreshold && distTL <= distTR && distTL <= distBL && distTL <= distBR -> DragHandle.TOP_LEFT
                                                distTR < handleTouchThreshold && distTR <= distTL && distTR <= distBL && distTR <= distBR -> DragHandle.TOP_RIGHT
                                                distBL < handleTouchThreshold && distBL <= distTL && distBL <= distTR && distBL <= distBR -> DragHandle.BOTTOM_LEFT
                                                distBR < handleTouchThreshold && distBR <= distTL && distBR <= distTR && distBR <= distBL -> DragHandle.BOTTOM_RIGHT
                                                else -> {
                                                    val touchX = localX
                                                    val touchY = localY
                                                    val l = cropLeft * imageWidth
                                                    val r = cropRight * imageWidth
                                                    val t = cropTop * imageHeight
                                                    val b = cropBottom * imageHeight

                                                    val isInside = touchX in l..r && touchY in t..b
                                                    val currentThreshold = if (isInside) 40f * density else 72f * density

                                                    val nearLeft = (touchX < 0f || kotlin.math.abs(touchX - l) < currentThreshold) && touchY in -currentThreshold..(imageHeight + currentThreshold)
                                                    val nearRight = (touchX > imageWidth || kotlin.math.abs(touchX - r) < currentThreshold) && touchY in -currentThreshold..(imageHeight + currentThreshold)
                                                    val nearTop = (touchY < 0f || kotlin.math.abs(touchY - t) < currentThreshold) && touchX in -currentThreshold..(imageWidth + currentThreshold)
                                                    val nearBottom = (touchY > imageHeight || kotlin.math.abs(touchY - b) < currentThreshold) && touchX in -currentThreshold..(imageWidth + currentThreshold)

                                                    val distances = mutableListOf<Pair<DragHandle, Float>>()
                                                    if (nearLeft) distances.add(DragHandle.LEFT to kotlin.math.abs(touchX - l))
                                                    if (nearRight) distances.add(DragHandle.RIGHT to kotlin.math.abs(touchX - r))
                                                    if (nearTop) distances.add(DragHandle.TOP to kotlin.math.abs(touchY - t))
                                                    if (nearBottom) distances.add(DragHandle.BOTTOM to kotlin.math.abs(touchY - b))

                                                    if (distances.isNotEmpty()) {
                                                        distances.minByOrNull { it.second }?.first
                                                    } else if (isInside) {
                                                        DragHandle.BODY
                                                    } else {
                                                        null
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = { activeHandle = null },
                                    onDragCancel = { activeHandle = null },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (containerWidth > 1f && containerHeight > 1f) {
                                            val containerRatio = containerWidth / containerHeight
                                            val imageWidth =
                                                if (containerRatio > aspectRatio) containerHeight * aspectRatio else containerWidth
                                            val imageHeight =
                                                if (containerRatio > aspectRatio) containerHeight else containerWidth / aspectRatio

                                            val deltaX = dragAmount.x / imageWidth
                                            val deltaY = dragAmount.y / imageHeight
                                            val minGap = 0.1f

                                            when (activeHandle) {
                                                DragHandle.TOP_LEFT -> {
                                                    cropLeft = (cropLeft + deltaX).coerceIn(
                                                        0f,
                                                        cropRight - minGap
                                                    )
                                                    cropTop = (cropTop + deltaY).coerceIn(
                                                        0f,
                                                        cropBottom - minGap
                                                    )
                                                }

                                                DragHandle.TOP_RIGHT -> {
                                                    cropRight = (cropRight + deltaX).coerceIn(
                                                        cropLeft + minGap,
                                                        1f
                                                    )
                                                    cropTop = (cropTop + deltaY).coerceIn(
                                                        0f,
                                                        cropBottom - minGap
                                                    )
                                                }

                                                DragHandle.BOTTOM_LEFT -> {
                                                    cropLeft = (cropLeft + deltaX).coerceIn(
                                                        0f,
                                                        cropRight - minGap
                                                    )
                                                    cropBottom = (cropBottom + deltaY).coerceIn(
                                                        cropTop + minGap,
                                                        1f
                                                    )
                                                }

                                                DragHandle.BOTTOM_RIGHT -> {
                                                    cropRight = (cropRight + deltaX).coerceIn(
                                                        cropLeft + minGap,
                                                        1f
                                                    )
                                                    cropBottom = (cropBottom + deltaY).coerceIn(
                                                        cropTop + minGap,
                                                        1f
                                                    )
                                                }

                                                DragHandle.LEFT -> {
                                                    cropLeft = (cropLeft + deltaX).coerceIn(
                                                        0f,
                                                        cropRight - minGap
                                                    )
                                                }

                                                DragHandle.RIGHT -> {
                                                    cropRight = (cropRight + deltaX).coerceIn(
                                                        cropLeft + minGap,
                                                        1f
                                                    )
                                                }

                                                DragHandle.TOP -> {
                                                    cropTop = (cropTop + deltaY).coerceIn(
                                                        0f,
                                                        cropBottom - minGap
                                                    )
                                                }

                                                DragHandle.BOTTOM -> {
                                                    cropBottom = (cropBottom + deltaY).coerceIn(
                                                        cropTop + minGap,
                                                        1f
                                                    )
                                                }

                                                DragHandle.BODY -> {
                                                    val width = cropRight - cropLeft
                                                    val height = cropBottom - cropTop
                                                    val newLeft =
                                                        (cropLeft + deltaX).coerceIn(0f, 1f - width)
                                                    val newTop =
                                                        (cropTop + deltaY).coerceIn(0f, 1f - height)
                                                    cropLeft = newLeft
                                                    cropRight = newLeft + width
                                                    cropTop = newTop
                                                    cropBottom = newTop + height
                                                }

                                                null -> {}
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (containerWidth > 1f && containerHeight > 1f) {
                            val containerRatio = containerWidth / containerHeight
                            val imageWidth =
                                if (containerRatio > aspectRatio) containerHeight * aspectRatio else containerWidth
                            val imageHeight =
                                if (containerRatio > aspectRatio) containerHeight else containerWidth / aspectRatio
                            val imageLeft = (containerWidth - imageWidth) / 2f
                            val imageTop = (containerHeight - imageHeight) / 2f

                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .absoluteOffset(
                                            x = with(LocalDensity.current) { imageLeft.toDp() },
                                            y = with(LocalDensity.current) { imageTop.toDp() }
                                        )
                                        .size(
                                            width = with(LocalDensity.current) { imageWidth.toDp() },
                                            height = with(LocalDensity.current) { imageHeight.toDp() }
                                        )
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val rectLeft = imageLeft + cropLeft * imageWidth
                                    val rectTop = imageTop + cropTop * imageHeight
                                    val rectRight = imageLeft + cropRight * imageWidth
                                    val rectBottom = imageTop + cropBottom * imageHeight

                                    // Draw background shadow
                                    drawRect(
                                        Color.Black.copy(alpha = 0.6f),
                                        topLeft = Offset(0f, 0f),
                                        size = Size(size.width, rectTop)
                                    )
                                    drawRect(
                                        Color.Black.copy(alpha = 0.6f),
                                        topLeft = Offset(0f, rectBottom),
                                        size = Size(size.width, size.height - rectBottom)
                                    )
                                    drawRect(
                                        Color.Black.copy(alpha = 0.6f),
                                        topLeft = Offset(0f, rectTop),
                                        size = Size(rectLeft, rectBottom - rectTop)
                                    )
                                    drawRect(
                                        Color.Black.copy(alpha = 0.6f),
                                        topLeft = Offset(rectRight, rectTop),
                                        size = Size(size.width - rectRight, rectBottom - rectTop)
                                    )

                                    // Draw white border
                                    drawRect(
                                        color = Color.White,
                                        topLeft = Offset(rectLeft, rectTop),
                                        size = Size(rectRight - rectLeft, rectBottom - rectTop),
                                        style = Stroke(width = 2.dp.toPx())
                                    )

                                    // Draw corner handle circles
                                    val radius = 10.dp.toPx()
                                    drawCircle(
                                        Color.White,
                                        center = Offset(rectLeft, rectTop),
                                        radius = radius
                                    )
                                    drawCircle(
                                        Color.White,
                                        center = Offset(rectRight, rectTop),
                                        radius = radius
                                    )
                                    drawCircle(
                                        Color.White,
                                        center = Offset(rectLeft, rectBottom),
                                        radius = radius
                                    )
                                    drawCircle(
                                        Color.White,
                                        center = Offset(rectRight, rectBottom),
                                        radius = radius
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Error al cargar la imagen", color = Color.White)
                    }
                }

                // Bottom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val croppedPath = cropImage(
                                cropLeft,
                                cropTop,
                                cropRight,
                                cropBottom
                            )
                            if (croppedPath != null) {
                                onConfirm(croppedPath)
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

enum class DragHandle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT, TOP, BOTTOM, BODY
}
