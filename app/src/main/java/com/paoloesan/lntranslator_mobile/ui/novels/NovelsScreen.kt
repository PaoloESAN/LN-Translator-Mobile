package com.paoloesan.lntranslator_mobile.ui.novels

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import coil3.compose.AsyncImage
import com.paoloesan.lntranslator_mobile.LocalStrings
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NovelsScreen(
    onNavigateToDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    val strings = LocalStrings.current

    var savedNovelsString by remember { mutableStateOf(prefs.getString("saved_novels", "") ?: "") }
    val novelsList =
        remember(savedNovelsString) { savedNovelsString.split(",").filter { it.isNotBlank() } }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var newNovelName by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(prefs.getBoolean("is_grid_view", true)) }
    var selectedNovels by remember { mutableStateOf(setOf<String>()) }
    var coverUpdateTrigger by remember { mutableIntStateOf(0) }

    var novelCoverUri by remember { mutableStateOf<Uri?>(null) }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        novelCoverUri = uri
    }

    fun saveNovelsList(newList: List<String>) {
        val updated = newList.joinToString(",")
        prefs.edit { putString("saved_novels", updated) }
        savedNovelsString = updated

        // Check if selected_novel was deleted
        val currentSelected = prefs.getString("selected_novel", null)
        if (currentSelected != null && !newList.contains(currentSelected)) {
            prefs.edit { remove("selected_novel") }
        }
    }

    fun moveNovelToTop(novel: String) {
        val newList = listOf(novel) + (novelsList - novel)
        saveNovelsList(newList)
    }

    val scope = rememberCoroutineScope()
    var fabMenuExpanded by remember { mutableStateOf(false) }

    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1) {
                    isFabVisible = false
                } else if (available.y > 1) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val finalName = NovelRepository.importNovelFromZip(context, uri, novelsList)
            if (finalName != null) {
                val updatedList = listOf(finalName) + novelsList
                saveNovelsList(updatedList)
                Toast.makeText(
                    context,
                    strings.importSuccess(finalName),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    strings.importError,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var tempZipFileForSharing by remember { mutableStateOf<java.io.File?>(null) }
    var novelNameBeingShared by remember { mutableStateOf("") }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            val file = tempZipFileForSharing
            if (file != null && file.exists()) {
                scope.launch {
                    val success = try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            java.io.FileInputStream(file).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                    if (success) {
                        Toast.makeText(context, strings.shareDialogSaveSuccess, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, strings.shareDialogSaveError, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    if (showShareOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showShareOptionsDialog = false },
            title = { Text(strings.shareDialogTitle) },
            text = { Text(strings.shareDialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showShareOptionsDialog = false
                        createDocumentLauncher.launch("novel_${novelNameBeingShared}.zip")
                    }
                ) {
                    Text(strings.shareDialogDownload)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showShareOptionsDialog = false
                        val file = tempZipFileForSharing
                        if (file != null && file.exists()) {
                            try {
                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/zip"
                                    putExtra(Intent.EXTRA_STREAM, fileUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        strings.menuShare
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    strings.importError,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(strings.shareDialogShare)
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                novelCoverUri = null
            },
            title = { Text(strings.novelsAddTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newNovelName,
                        onValueChange = { newNovelName = it },
                        label = { Text(strings.novelsAddNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.novelsCoverOptional,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .aspectRatio(0.7f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { coverPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (novelCoverUri != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = novelCoverUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { novelCoverUri = null },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.HighlightOff,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newNovelName.trim()
                        if (trimmed.isNotBlank() && !novelsList.contains(trimmed)) {
                            saveNovelsList(listOf(trimmed) + novelsList)
                            novelCoverUri?.let { uri ->
                                NovelRepository.saveCoverImage(context, trimmed, uri)
                            }
                        }
                        showAddDialog = false
                        newNovelName = ""
                        novelCoverUri = null
                    }
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    novelCoverUri = null
                }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    if (showEditDialog && selectedNovels.size == 1) {
        val oldName = selectedNovels.first()
        var editName by remember { mutableStateOf(oldName) }
        var isCoverDeleted by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                novelCoverUri = null
            },
            title = { Text(strings.novelsEditTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(strings.novelsAddNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.novelsCoverOptional,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .aspectRatio(0.7f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { coverPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val coverFile =
                                remember(oldName) { NovelRepository.getCoverFile(context, oldName) }

                            if (novelCoverUri != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = novelCoverUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { novelCoverUri = null },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.HighlightOff,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                }
                            } else if (coverFile.exists() && !isCoverDeleted) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = remember(oldName, coverUpdateTrigger) {
                                            coil3.request.ImageRequest.Builder(context)
                                                .data(coverFile)
                                                .memoryCacheKey("${coverFile.absolutePath}_$coverUpdateTrigger")
                                                .build()
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { isCoverDeleted = true },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.HighlightOff,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editName.trim()
                        if (trimmed.isNotBlank()) {
                            val finalName = if (trimmed != oldName) {
                                val newList = novelsList.toMutableList()
                                val index = newList.indexOf(oldName)
                                if (index != -1) {
                                    newList[index] = trimmed
                                    saveNovelsList(newList)

                                    val currentActive = prefs.getString("selected_novel", null)
                                    if (currentActive == oldName) {
                                        prefs.edit { putString("selected_novel", trimmed) }
                                    }
                                    NovelRepository.renameNovelData(context, oldName, trimmed)
                                }
                                trimmed
                            } else {
                                oldName
                            }

                            if (novelCoverUri != null) {
                                NovelRepository.saveCoverImage(context, finalName, novelCoverUri!!)
                                coverUpdateTrigger++
                            } else if (isCoverDeleted) {
                                NovelRepository.deleteCoverImage(context, finalName)
                                coverUpdateTrigger++
                            }

                            selectedNovels = emptySet()
                        }
                        showEditDialog = false
                        novelCoverUri = null
                    }
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    novelCoverUri = null
                }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(strings.deleteNovelConfirmationTitle) },
            text = { Text(strings.deleteNovelConfirmationMessage(selectedNovels.size)) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedNovels.forEach { novel ->
                            NovelRepository.deleteNovelData(context, novel)
                            prefs.edit { remove("last_page_$novel") }
                        }
                        val newList = novelsList.filterNot { selectedNovels.contains(it) }
                        saveNovelsList(newList)
                        selectedNovels = emptySet()
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(strings.buttonDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    Scaffold { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (selectedNovels.isNotEmpty()) {
                    TopAppBar(
                        title = { Text(strings.novelsSelected(selectedNovels.size)) },
                        navigationIcon = {
                            IconButton(onClick = { selectedNovels = emptySet() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = strings.cdCancelSelection
                                )
                            }
                        },
                        actions = {
                            if (selectedNovels.size == 1) {
                                IconButton(onClick = {
                                    val novelName = selectedNovels.first()
                                    val zipFile = NovelRepository.exportNovelToZip(
                                        context,
                                        novelName
                                    )
                                    if (zipFile != null && zipFile.exists()) {
                                        tempZipFileForSharing = zipFile
                                        novelNameBeingShared = novelName
                                        showShareOptionsDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            strings.novelEmptyError,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = strings.menuShare
                                    )
                                }
                                IconButton(onClick = { showEditDialog = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = strings.cdEdit
                                    )
                                }
                            }
                            IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = strings.cdDelete
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                } else {
                    TopAppBar(
                        title = { Text(strings.novelsTitle) },
                        actions = {
                            Row(
                                Modifier.padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                ToggleButton(
                                    checked = !isGridView,
                                    onCheckedChange = {
                                        isGridView = false
                                        prefs.edit { putBoolean("is_grid_view", false) }
                                    },
                                    shapes =
                                        ButtonGroupDefaults.connectedLeadingButtonShapes()

                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                                        contentDescription = strings.cdListView
                                    )
                                }
                                ToggleButton(
                                    checked = isGridView,
                                    onCheckedChange = {
                                        isGridView = true
                                        prefs.edit { putBoolean("is_grid_view", true) }
                                    },
                                    shapes =
                                        ButtonGroupDefaults.connectedTrailingButtonShapes()

                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.GridView,
                                        contentDescription = strings.cdGridView
                                    )
                                }
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (novelsList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = strings.novelsEmptyTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.novelsEmptySubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        if (isGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(novelsList) { novel ->
                                    val isSelected = selectedNovels.contains(novel)
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.7f)
                                            .then(
                                                if (isSelected) Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    CardDefaults.elevatedShape
                                                ) else Modifier
                                            ),
                                        colors = CardDefaults.elevatedCardColors()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (selectedNovels.isNotEmpty()) {
                                                            selectedNovels =
                                                                if (isSelected) selectedNovels - novel else selectedNovels + novel
                                                        } else {
                                                            moveNovelToTop(novel)
                                                            onNavigateToDetails(novel)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        selectedNovels = selectedNovels + novel
                                                    }
                                                ),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            val coverFile = remember(novel, coverUpdateTrigger) {
                                                NovelRepository.getCoverFile(
                                                    context,
                                                    novel
                                                )
                                            }
                                            val hasCover = coverFile.exists()

                                            if (hasCover) {
                                                AsyncImage(
                                                    model = remember(novel, coverUpdateTrigger) {
                                                        coil3.request.ImageRequest.Builder(context)
                                                            .data(coverFile)
                                                            .memoryCacheKey("${coverFile.absolutePath}_$coverUpdateTrigger")
                                                            .build()
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Gradient overlay for text readability - only when there is a cover
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.7f)
                                                                ),
                                                                startY = 300f // Start gradient near the bottom
                                                            )
                                                        )
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Book,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp),
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.4f
                                                        )
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .padding(12.dp)
                                                    .fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = if (novel.length > 5) novel.dropLast(5) else novel,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    textAlign = TextAlign.Center,
                                                    color = if (hasCover) Color.White else (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (novel.length > 5) {
                                                    Text(
                                                        text = novel.takeLast(5),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = if (hasCover) Color.White else (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(novelsList) { novel ->
                                    val isSelected = selectedNovels.contains(novel)
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = if (isSelected) CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ) else CardDefaults.elevatedCardColors()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (selectedNovels.isNotEmpty()) {
                                                            selectedNovels =
                                                                if (isSelected) selectedNovels - novel else selectedNovels + novel
                                                        } else {
                                                            moveNovelToTop(novel)
                                                            onNavigateToDetails(novel)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        selectedNovels = selectedNovels + novel
                                                    }
                                                )
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val coverFile = remember(novel, coverUpdateTrigger) {
                                                NovelRepository.getCoverFile(
                                                    context,
                                                    novel
                                                )
                                            }
                                            if (coverFile.exists()) {
                                                AsyncImage(
                                                    model = remember(novel, coverUpdateTrigger) {
                                                        coil3.request.ImageRequest.Builder(context)
                                                            .data(coverFile)
                                                            .memoryCacheKey("${coverFile.absolutePath}_$coverUpdateTrigger")
                                                            .build()
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(MaterialTheme.shapes.small),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Book,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (novel.length > 5) novel.dropLast(5) else novel,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (novel.length > 5) {
                                                    Text(
                                                        text = novel.takeLast(5),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Close content Box
                }

                // Close Column
            }

            if (fabMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            fabMenuExpanded = false
                        }
                )
            }

            if (selectedNovels.isEmpty()) {
                AnimatedVisibility(
                    visible = isFabVisible || fabMenuExpanded,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    FloatingActionButtonMenu(
                        expanded = fabMenuExpanded,
                        button = {
                            ToggleFloatingActionButton(
                                containerSize = ToggleFloatingActionButtonDefaults.containerSize(
                                    80.dp,
                                    55.dp
                                ),
                                checked = fabMenuExpanded,
                                onCheckedChange = { fabMenuExpanded = it },
                            ) {
                                Icon(
                                    imageVector = if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                                    tint = if (fabMenuExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    contentDescription = if (fabMenuExpanded) strings.buttonClose else strings.novelsAddTitle
                                )
                            }
                        }
                    ) {
                        FloatingActionButtonMenuItem(
                            onClick = {
                                fabMenuExpanded = false
                                importLauncher.launch("application/zip")
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.DriveFolderUpload,
                                    contentDescription = null
                                )
                            },
                            text = { Text(strings.menuImport) }
                        )
                        FloatingActionButtonMenuItem(
                            onClick = {
                                fabMenuExpanded = false
                                showAddDialog = true
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text(strings.novelsAddTitle) }
                        )
                    }
                }
            }
        }
    }
}
