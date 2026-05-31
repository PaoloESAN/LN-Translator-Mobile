package com.paoloesan.lntranslator_mobile.ui.novels

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageManagementScreen(
    novelName: String,
    onBack: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current

    var pagesList by remember { mutableStateOf(NovelRepository.getPages(context, novelName)) }
    var isEditMode by remember { mutableStateOf(true) }

    // Gestures and drag state
    var activeDragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { mutableStateMapOf<String, Float>() } // Keyed by page ID

    // Unified Dialog States
    var showPageDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogPageId by remember { mutableStateOf<String?>(null) } // Non-null if editing
    var dialogInsertIndex by remember { mutableIntStateOf(-1) }    // Index if inserting/adding
    var dialogTranslatedText by remember { mutableStateOf("") }
    var dialogOriginalText by remember { mutableStateOf("") }
    var dialogImagePath by remember { mutableStateOf<String?>(null) }
    var dialogOnlyImage by remember { mutableStateOf(false) }
    var pageToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(novelName, pagesList) {
        topBarVisible.value = true
        topBarTitle.value = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val titleStart = "${strings.pageManagementTitle} - "
                Text(
                    text = titleStart + (if (novelName.length > 5) novelName.dropLast(5) else novelName),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (novelName.length > 5) {
                    Text(
                        text = novelName.takeLast(5),
                        maxLines = 1
                    )
                }
            }
        }
        topBarNavIcon.value = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.navBack
                )
            }
        }
        topBarActions.value = {}
        topBarColors.value = null
    }

    // Back gesture handling
    BackHandler {
        onBack()
    }

    // Helper function to save image from Uri
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            if (pagesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            strings.pageManagementNoPages,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            strings.pageManagementPressEditToAdd,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(pagesList.size, key = { index -> pagesList[index].id }) { index ->
                        val page = pagesList[index]
                        val isDragging = activeDragIndex == index

                        val offsetModifier = if (isDragging) {
                            Modifier
                                .zIndex(1f)
                                .graphicsLayer {
                                    translationY = dragOffsetY
                                    shadowElevation = 8f
                                    scaleX = 1.02f
                                    scaleY = 1.02f
                                }
                        } else {
                            Modifier
                        }

                        var expandedItemMenu by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    itemHeights[page.id] = coordinates.size.height.toFloat()
                                }
                                .then(offsetModifier)
                                .animateItem()
                                .then(
                                    if (page.id == "cover_page") {
                                        Modifier
                                    } else {
                                        Modifier.clickable {
                                            onPageSelected(index)
                                        }
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDragging) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditMode) {
                                    if (page.id != "cover_page") {
                                        Icon(
                                            imageVector = Icons.Default.Reorder,
                                            contentDescription = strings.pageManagementDragToReorder,
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 8.dp,
                                                    vertical = 16.dp
                                                )
                                                .pointerInput(page.id) {
                                                    detectDragGestures(
                                                        onDragStart = {
                                                            val currIndex =
                                                                pagesList.indexOfFirst { it.id == page.id }
                                                            if (currIndex != -1) {
                                                                activeDragIndex = currIndex
                                                                dragOffsetY = 0f
                                                            }
                                                        },
                                                        onDragEnd = {
                                                            activeDragIndex = null
                                                            dragOffsetY = 0f
                                                            NovelRepository.savePages(
                                                                context,
                                                                novelName,
                                                                pagesList
                                                            )
                                                        },
                                                        onDragCancel = {
                                                            activeDragIndex = null
                                                            dragOffsetY = 0f
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            dragOffsetY += dragAmount.y

                                                            val currentIndex = activeDragIndex
                                                                ?: return@detectDragGestures
                                                            if (currentIndex == 0) return@detectDragGestures
                                                            val currentHeight =
                                                                itemHeights[page.id] ?: 150f

                                                            if (dragOffsetY > 0f) {
                                                                val nextIndex = currentIndex + 1
                                                                if (nextIndex < pagesList.size) {
                                                                    val nextHeight =
                                                                        itemHeights[pagesList[nextIndex].id]
                                                                            ?: currentHeight
                                                                    if (dragOffsetY > nextHeight * 0.7f) {
                                                                        val listCopy =
                                                                            pagesList.toMutableList()
                                                                        val item = listCopy.removeAt(
                                                                            currentIndex
                                                                        )
                                                                        listCopy.add(nextIndex, item)
                                                                        pagesList = listCopy
                                                                        activeDragIndex = nextIndex
                                                                        dragOffsetY -= nextHeight
                                                                    }
                                                                }
                                                            } else if (dragOffsetY < 0f) {
                                                                val prevIndex = currentIndex - 1
                                                                if (prevIndex >= 1) {
                                                                    val prevHeight =
                                                                        itemHeights[pagesList[prevIndex].id]
                                                                            ?: currentHeight
                                                                    if (dragOffsetY < -prevHeight * 0.7f) {
                                                                        val listCopy =
                                                                            pagesList.toMutableList()
                                                                        val item = listCopy.removeAt(
                                                                            currentIndex
                                                                        )
                                                                        listCopy.add(prevIndex, item)
                                                                        pagesList = listCopy
                                                                        activeDragIndex = prevIndex
                                                                        dragOffsetY += prevHeight
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                },
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(40.dp))
                                    }
                                }

                                // Image Preview if any
                                if (page.imagePath != null) {
                                    val bitmap = remember(page.imagePath) {
                                        try {
                                            BitmapFactory.decodeFile(page.imagePath)
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.LightGray),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                }

                                // Text Preview
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    val isCoverFirst = pagesList.firstOrNull()?.id == "cover_page"
                                    val itemTitle = if (page.id == "cover_page") {
                                        strings.pageManagementCoverPage
                                    } else {
                                        val displayIndex = if (isCoverFirst) index else index + 1
                                        strings.pageManagementPageNumber(displayIndex)
                                    }
                                    Text(
                                        text = itemTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val previewText = when {
                                        page.translatedText.isNotBlank() -> page.translatedText
                                        !page.originalText.isNullOrBlank() -> page.originalText
                                        else -> strings.pageManagementImageOnlyPage
                                    }
                                    Text(
                                        text = previewText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                }

                                if (isEditMode && page.id != "cover_page") {
                                    // Menu Actions
                                    Box {
                                        IconButton(onClick = { expandedItemMenu = true }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = strings.pageManagementOptions
                                            )

                                        }

                                        DropdownMenuPopup(
                                            expanded = expandedItemMenu,
                                            onDismissRequest = { expandedItemMenu = false }
                                        ) {
                                            DropdownMenuGroup(
                                                shapes = MenuDefaults.groupShape(0, 2)
                                            ) {
                                                DropdownMenuItem(
                                                    shape = MenuDefaults.itemShape(0, 2).shape,
                                                    text = { Text(strings.pageManagementInsertAbove) },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.VerticalAlignTop,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedItemMenu = false
                                                        dialogPageId = null
                                                        dialogInsertIndex = index
                                                        dialogTranslatedText = ""
                                                        dialogOriginalText = ""
                                                        dialogImagePath = null
                                                        dialogTitle =
                                                            strings.pageManagementInsertPageTitle
                                                        dialogOnlyImage = false
                                                        showPageDialog = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    shape = MenuDefaults.itemShape(1, 2).shape,
                                                    text = { Text(strings.pageManagementInsertBelow) },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.VerticalAlignBottom,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedItemMenu = false
                                                        dialogPageId = null
                                                        dialogInsertIndex = index + 1
                                                        dialogTranslatedText = ""
                                                        dialogOriginalText = ""
                                                        dialogImagePath = null
                                                        dialogTitle =
                                                            strings.pageManagementInsertPageTitle
                                                        dialogOnlyImage = false
                                                        showPageDialog = true
                                                    }
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                                            DropdownMenuGroup(
                                                shapes = MenuDefaults.groupShape(1, 2)
                                            ) {
                                                DropdownMenuItem(
                                                    shape = MenuDefaults.itemShape(0, 2).shape,
                                                    text = { Text(strings.pageManagementEditPage) },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedItemMenu = false
                                                        dialogPageId = page.id
                                                        dialogInsertIndex = -1
                                                        dialogTranslatedText = page.translatedText
                                                        dialogOriginalText = page.originalText ?: ""
                                                        dialogImagePath = page.imagePath
                                                        dialogTitle =
                                                            strings.pageManagementEditPageTitle
                                                        dialogOnlyImage =
                                                            page.translatedText.isBlank() && (page.originalText.isNullOrBlank()) && page.imagePath != null
                                                        showPageDialog = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    shape = MenuDefaults.itemShape(1, 2).shape,
                                                    text = {
                                                        Text(
                                                            strings.pageManagementDeletePage,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedItemMenu = false
                                                        pageToDeleteIndex = index
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isEditMode) {
            BottomAppBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            dialogPageId = null
                            dialogInsertIndex = pagesList.size
                            dialogTranslatedText = ""
                            dialogOriginalText = ""
                            dialogImagePath = null
                            dialogTitle = strings.pageManagementAddPageTitle
                            dialogOnlyImage = false
                            showPageDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.pageManagementAddPage)
                    }
                }
            }
        }
    }

    // Unified Add/Insert/Edit Dialog
    if (showPageDialog) {
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
            onDismissRequest = { showPageDialog = false },
            title = { Text(dialogTitle) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // Fixed: removed invalid argument
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
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
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
                                            tint = MaterialTheme.colorScheme.error,
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
                                Icon(Icons.Default.Image, contentDescription = null)
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
                        val textToSave = if (dialogOnlyImage) "" else dialogTranslatedText
                        val originalToSave = if (dialogOnlyImage) "" else dialogOriginalText
                        if (textToSave.isNotBlank() || dialogImagePath != null) {
                            val listCopy = pagesList.toMutableList()
                            if (dialogPageId != null) {
                                // Editing existing page
                                val index = listCopy.indexOfFirst { it.id == dialogPageId }
                                if (index != -1) {
                                    val oldPage = listCopy[index]
                                    if (oldPage.imagePath != null && oldPage.imagePath != dialogImagePath) {
                                        try {
                                            File(oldPage.imagePath).delete()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    listCopy[index] = oldPage.copy(
                                        translatedText = textToSave,
                                        originalText = originalToSave.takeIf { it.isNotBlank() },
                                        imagePath = dialogImagePath
                                    )
                                }
                            } else {
                                // Inserting/Adding new page
                                val newPage = NovelPage(
                                    translatedText = textToSave,
                                    originalText = originalToSave.takeIf { it.isNotBlank() },
                                    imagePath = dialogImagePath
                                )
                                if (dialogInsertIndex in 0..listCopy.size) {
                                    listCopy.add(dialogInsertIndex, newPage)
                                } else {
                                    listCopy.add(newPage)
                                }
                            }
                            pagesList = listCopy
                            NovelRepository.savePages(context, novelName, listCopy)
                            showPageDialog = false
                        }
                    },
                    enabled = if (dialogOnlyImage) dialogImagePath != null else (dialogTranslatedText.isNotBlank() || dialogImagePath != null)
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPageDialog = false }) {
                    Text(strings.buttonCancel)
                }
            }

        )
    }

    if (pageToDeleteIndex != null) {
        AlertDialog(
            onDismissRequest = { pageToDeleteIndex = null },
            title = { Text(strings.deletePageConfirmationTitle) },
            text = { Text(strings.deletePageConfirmationMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        val index = pageToDeleteIndex!!
                        val listCopy = pagesList.toMutableList()
                        if (index in 0 until listCopy.size) {
                            val removedPage = listCopy.removeAt(index)
                            if (removedPage.imagePath != null) {
                                try {
                                    File(removedPage.imagePath).delete()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            pagesList = listCopy
                            NovelRepository.savePages(
                                context,
                                novelName,
                                listCopy
                            )
                        }
                        pageToDeleteIndex = null
                    }
                ) {
                    Text(strings.buttonDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { pageToDeleteIndex = null }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }
}
