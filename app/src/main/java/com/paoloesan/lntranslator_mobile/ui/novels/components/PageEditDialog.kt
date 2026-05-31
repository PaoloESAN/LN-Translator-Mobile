package com.paoloesan.lntranslator_mobile.ui.novels.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.paoloesan.lntranslator_mobile.LocalStrings
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditDialog(
    novelName: String,
    dialogTitle: String,
    initialTranslatedText: String,
    initialOriginalText: String,
    initialImagePath: String?,
    initialOnlyImage: Boolean,
    onDismiss: () -> Unit,
    onSave: (translatedText: String, originalText: String, imagePath: String?, onlyImage: Boolean) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    var dialogTranslatedText by remember { mutableStateOf(initialTranslatedText) }
    var dialogOriginalText by remember { mutableStateOf(initialOriginalText) }
    var dialogImagePath by remember { mutableStateOf(initialImagePath) }
    var dialogOnlyImage by remember { mutableStateOf(initialOnlyImage) }

    // Cropper States
    var showCropDialog by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }

    fun saveImageUri(uri: Uri): String? {
        val contentResolver = context.contentResolver
        val imagesDir = File(context.filesDir, "images_$novelName")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file = File(imagesDir, fileName)
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imagePath = saveImageUri(uri)
            if (imagePath != null) {
                dialogImagePath = imagePath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dialogOnlyImage = !dialogOnlyImage }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.pageManagementOnlyImage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = dialogOnlyImage,
                        onCheckedChange = { dialogOnlyImage = it }
                    )
                }

                if (!dialogOnlyImage) {
                    OutlinedTextField(
                        value = dialogTranslatedText,
                        onValueChange = { dialogTranslatedText = it },
                        label = { Text(strings.pageManagementTranslatedTextLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = dialogOriginalText,
                        onValueChange = { dialogOriginalText = it },
                        label = { Text(strings.pageManagementOriginalTextLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = strings.pageManagementPageImageLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (dialogImagePath != null) {
                        val bitmap = remember(dialogImagePath) {
                            try {
                                BitmapFactory.decodeFile(dialogImagePath)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        bitmap?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showFullImage = true },
                                    contentScale = ContentScale.Crop
                                )
                                // Crop button (arriba a la izquierda)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable {
                                            showCropDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Crop,
                                        contentDescription = "Recortar imagen",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Remove button (arriba a la derecha)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable { dialogImagePath = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = strings.pageManagementRemoveImage,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { pickImageLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Crop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.pageManagementSelectImage)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        if (dialogOnlyImage) "" else dialogTranslatedText,
                        if (dialogOnlyImage) "" else dialogOriginalText,
                        dialogImagePath,
                        dialogOnlyImage
                    )
                },
                enabled = if (dialogOnlyImage) dialogImagePath != null else (dialogTranslatedText.isNotBlank() || dialogImagePath != null)
            ) {
                Text(strings.buttonSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.buttonCancel)
            }
        }
    )

    // Fullscreen Crop Dialog
    if (showCropDialog && dialogImagePath != null) {
        ImageCropDialog(
            novelName = novelName,
            imagePath = dialogImagePath!!,
            onDismiss = { showCropDialog = false },
            onConfirm = { croppedPath ->
                dialogImagePath = croppedPath
                showCropDialog = false
            }
        )
    }

    // Fullscreen Image Viewer
    if (showFullImage && dialogImagePath != null) {
        var isUiVisible by remember { mutableStateOf(true) }
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SwipeDismissZoomImage(
                        imageUrl = java.io.File(dialogImagePath!!),
                        onDismiss = { showFullImage = false },
                        onClick = { isUiVisible = !isUiVisible },
                        onZoomedChanged = { zoomed ->
                            isUiVisible = !zoomed
                        }
                    )

                    AnimatedVisibility(
                        visible = isUiVisible,
                        enter = fadeIn(animationSpec = tween(150)),
                        exit = fadeOut(animationSpec = tween(150)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .statusBarsPadding()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(onClick = { showFullImage = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
