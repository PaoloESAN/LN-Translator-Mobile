package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.ui.novels.NovelRepository

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
    var newNovelName by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }
    var selectedNovels by remember { mutableStateOf(setOf<String>()) }

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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.novelsAddTitle) },
            text = {
                OutlinedTextField(
                    value = newNovelName,
                    onValueChange = { newNovelName = it },
                    label = { Text(strings.novelsAddNameLabel) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newNovelName.trim()
                        if (trimmed.isNotBlank() && !novelsList.contains(trimmed)) {
                            saveNovelsList(novelsList + trimmed)
                        }
                        showAddDialog = false
                        newNovelName = ""
                    }
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    if (showEditDialog && selectedNovels.size == 1) {
        val oldName = selectedNovels.first()
        var editName by remember { mutableStateOf(oldName) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(strings.editPromptTitle) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(strings.novelsAddNameLabel) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editName.trim()
                        if (trimmed.isNotBlank() && trimmed != oldName) {
                            val newList = novelsList.toMutableList()
                            val index = newList.indexOf(oldName)
                            if (index != -1) {
                                newList[index] = trimmed
                                saveNovelsList(newList)

                                val currentActive = prefs.getString("selected_novel", null)
                                if (currentActive == oldName) {
                                    prefs.edit().putString("selected_novel", trimmed).apply()
                                }
                            }
                            selectedNovels = setOf(trimmed)
                        }
                        showEditDialog = false
                    }
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (selectedNovels.isNotEmpty()) {
                TopAppBar(
                    title = { Text(strings.novelsSelected(selectedNovels.size)) },
                    navigationIcon = {
                        IconButton(onClick = { selectedNovels = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = strings.cdCancelSelection)
                        }
                    },
                    actions = {
                        if (selectedNovels.size == 1) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = strings.cdEdit)
                            }
                        }
                        IconButton(onClick = {
                            selectedNovels.forEach { novel ->
                                NovelRepository.deleteNovelData(context, novel)
                            }
                            val newList = novelsList.filterNot { selectedNovels.contains(it) }
                            saveNovelsList(newList)
                            selectedNovels = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = strings.cdDelete)
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
                                onCheckedChange = { isGridView = !isGridView },
                                shapes =
                                    ButtonGroupDefaults.connectedLeadingButtonShapes()

                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ViewList,
                                    contentDescription = strings.cdListView
                                )
                            }
                            ToggleButton(
                                checked = isGridView,
                                onCheckedChange = { isGridView = !isGridView },
                                shapes =
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()

                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = strings.cdGridView
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedNovels.isEmpty()) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = strings.novelsAddTitle)
                }
            }
        }
    ) { padding ->
        if (novelsList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(novelsList) { novel ->
                        val isSelected = selectedNovels.contains(novel)
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            colors = if (isSelected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.elevatedCardColors()
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
                                                onNavigateToDetails(novel)
                                            }
                                        },
                                        onLongClick = {
                                            selectedNovels = selectedNovels + novel
                                        }
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = novel,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(novelsList) { novel ->
                        val isSelected = selectedNovels.contains(novel)
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (isSelected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.elevatedCardColors()
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
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = novel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
