package com.paoloesan.lntranslator_mobile.ui.home

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.paoloesan.lntranslator_mobile.LocalCurrentRoute
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.service.OverlayService
import com.paoloesan.lntranslator_mobile.service.ScreenCaptureService
import com.paoloesan.lntranslator_mobile.ui.novels.components.NovelRepository
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToPrompts: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val strings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val currentRoute = LocalCurrentRoute.current

    LaunchedEffect(currentRoute) {
        if (currentRoute == "inicio") {
            topBarVisible.value = true
            topBarTitle.value = { Text(strings.appName) }
            topBarActions.value = {}
            topBarNavIcon.value = {}
            topBarColors.value = null
        }
    }

    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    var savedNovelsString by remember { mutableStateOf(prefs.getString("saved_novels", "") ?: "") }
    val novelsList by remember {
        derivedStateOf {
            savedNovelsString.split(",").filter { it.isNotBlank() }
        }
    }
    var textPrompt by rememberSaveable { mutableStateOf("") }
    var selectedNovel by remember {
        mutableStateOf(
            prefs.getString(
                "selected_novel",
                null
            )
        )
    }

    androidx.compose.runtime.DisposableEffect(prefs) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == "saved_novels") {
                    savedNovelsString = sharedPreferences.getString("saved_novels", "") ?: ""
                }
                if (key == "selected_novel") {
                    selectedNovel = sharedPreferences.getString("selected_novel", null)
                }
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var expandedNovels by remember { mutableStateOf(false) }
    var showAddNovelDialog by remember { mutableStateOf(false) }
    var newNovelName by remember { mutableStateOf("") }
    var novelCoverUri by remember { mutableStateOf<Uri?>(null) }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        novelCoverUri = uri
    }
    val puedeGuardarPrompt = textPrompt.trim().isNotBlank()
    val navigateInteraction = remember { MutableInteractionSource() }
    val saveInteraction = remember { MutableInteractionSource() }
    val navigatePressed by navigateInteraction.collectIsPressedAsState()
    val savePressed by saveInteraction.collectIsPressedAsState()
    var highlightedButton by remember { mutableStateOf<Int?>(null) }
    var isNavigatingToPrompts by remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.route == "inicio") {
                isNavigatingToPrompts = false
            }
        }
    }

    val navigateWeight by animateFloatAsState(
        targetValue = when (highlightedButton) {
            0 -> 1.15f
            1 -> 0.85f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 180),
        label = "navigateWeight"
    )
    val saveWeight by animateFloatAsState(
        targetValue = when (highlightedButton) {
            0 -> 0.85f
            1 -> 1.15f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 180),
        label = "saveWeight"
    )

    LaunchedEffect(navigatePressed, savePressed) {
        when {
            navigatePressed -> highlightedButton = 0
            savePressed -> highlightedButton = 1
            highlightedButton != null -> {
                delay(220)
                highlightedButton = null
            }
        }
    }

    val mediaProjectionManager = remember {
        context.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            ScreenCaptureService.start(context, result.resultCode, result.data!!)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                OverlayService.start(context)
            }, 500)
        }
    }

    val result by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("prompt_seleccionado")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(result) {
        result?.let {
            textPrompt = it
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("prompt_seleccionado")
        }
    }

    PromptDialog(
        descripcion = textPrompt,
        abierto = showDialog,
        contexto = context,
        onDismissRequest = { showDialog = false }
    )

    if (showAddNovelDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddNovelDialog = false
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
                                            .clickable { novelCoverUri = null },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
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
                    enabled = newNovelName.trim().isNotEmpty(),
                    onClick = {
                        val trimmed = newNovelName.trim()
                        if (trimmed.isNotBlank() && !novelsList.contains(trimmed)) {
                            val updated =
                                if (savedNovelsString.isEmpty()) trimmed else "$trimmed,$savedNovelsString"
                            prefs.edit { putString("saved_novels", updated) }
                            savedNovelsString = updated
                            selectedNovel = trimmed
                            prefs.edit { putString("selected_novel", trimmed) }

                            novelCoverUri?.let { uri ->
                                NovelRepository.saveCoverImage(context, trimmed, uri)
                            }
                        }
                        showAddNovelDialog = false
                        newNovelName = ""
                        novelCoverUri = null
                    }
                ) {
                    Text(strings.buttonSave)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddNovelDialog = false
                    novelCoverUri = null
                }) {
                    Text(strings.buttonCancel)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val currentNovelCover = remember(selectedNovel) {
            selectedNovel?.let { NovelRepository.getCoverFile(context, it) }
        }

        AnimatedContent(
            targetState = currentNovelCover?.takeIf { it.exists() },
            label = "logoTransition",
            modifier = Modifier.weight(1f, fill = false)
        ) { coverFile ->
            if (coverFile != null) {
                AsyncImage(
                    model = coverFile,
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxHeight = 200.dp, maxWidth = 200.dp)
                        .aspectRatio(0.7f)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.paoloesan.lntranslator_mobile.R.drawable.ln_translator_logo),
                    contentDescription = strings.cdLogo,
                    modifier = Modifier
                        .sizeIn(maxHeight = 200.dp, maxWidth = 200.dp)
                        .aspectRatio(1f)
                        .clip(MaterialShapes.Clover4Leaf.toShape()),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = "\"${strings.homeWelcome}\"",
            fontStyle = FontStyle.Italic
        )

        // Section for Novels
        ExposedDropdownMenuBox(
            expanded = expandedNovels,
            onExpandedChange = { expandedNovels = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val displayValue = remember(selectedNovel, strings.novelsNone) {
                val novel = selectedNovel ?: strings.novelsNone
                if (novel.length > 25) {
                    novel.take(17) + "..." + novel.takeLast(5)
                } else {
                    novel
                }
            }
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(strings.homeNovelsSectionTitle) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNovels)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            DropdownMenuPopup(
                expanded = expandedNovels,
                onDismissRequest = { expandedNovels = false }
            ) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(0, 1)
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.novelsNone) },
                        selected = selectedNovel == null,
                        shapes = MenuDefaults.itemShape(0, novelsList.count() + 2),
                        onClick = {
                            selectedNovel = null
                            prefs.edit { remove("selected_novel") }
                            expandedNovels = false
                        },
                        selectedLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Transparent,
                            )
                        },
                    )

                    val dropdownScrollState = rememberScrollState()
                    val showDropdownGradient by remember(dropdownScrollState) {
                        derivedStateOf {
                            dropdownScrollState.maxValue > 0 && dropdownScrollState.value < dropdownScrollState.maxValue
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 170.dp)
                                .verticalScroll(dropdownScrollState)
                        ) {
                            novelsList.forEachIndexed { index, novel ->
                                DropdownMenuItem(
                                    selected = novel == selectedNovel,
                                    shapes = MenuDefaults.itemShape(
                                        index + 1,
                                        novelsList.count() + 2
                                    ),
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (novel.length > 5) novel.dropLast(5) else novel,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (novel.length > 5) {
                                                Text(
                                                    text = novel.takeLast(5),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedNovel = novel
                                        prefs.edit { putString("selected_novel", novel) }
                                        expandedNovels = false
                                    },
                                    selectedLeadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.Transparent,
                                        )
                                    },
                                )
                            }
                        }

                        if (showDropdownGradient) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surfaceContainer.copy(
                                                    alpha = 0.95f
                                                )
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
                    )
                    DropdownMenuItem(
                        selected = false,
                        shapes = MenuDefaults.itemShape(
                            novelsList.count() + 1,
                            novelsList.count() + 2
                        ),
                        text = {
                            Text(
                                strings.novelsAddTitle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            expandedNovels = false
                            showAddNovelDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(navigateWeight)
                        .heightIn(min = 56.dp),
                    interactionSource = navigateInteraction,
                    shape = MaterialTheme.shapes.large,
                    enabled = !isNavigatingToPrompts,
                    onClick = {
                        if (!isNavigatingToPrompts) {
                            isNavigatingToPrompts = true
                            onNavigateToPrompts()
                        }
                    },
                ) {
                    Text(strings.homeViewPrompts)
                }
                FilledTonalButton(
                    modifier = Modifier
                        .weight(saveWeight)
                        .heightIn(min = 56.dp),
                    interactionSource = saveInteraction,
                    shape = MaterialTheme.shapes.large,
                    enabled = puedeGuardarPrompt,
                    onClick = { showDialog = true },
                ) {
                    Text(strings.homeSavePrompt)
                }
            }

            OutlinedTextField(
                label = { Text(text = strings.homePromptLabel) },
                value = textPrompt,
                onValueChange = { textPrompt = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(ButtonDefaults.MediumContainerHeight),
            shape = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight).shape,
            contentPadding = ButtonDefaults.MediumContentPadding,
            onClick = {
                prefs.edit { putString("prompt_app", textPrompt) }
                if (!android.provider.Settings.canDrawOverlays(context)) {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                } else {
                    val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                    screenCaptureLauncher.launch(captureIntent)
                }
            }) {
            Text(strings.homeStartButton)
        }
    }
}
