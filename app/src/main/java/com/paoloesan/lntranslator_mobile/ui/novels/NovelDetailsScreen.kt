package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NovelDetailsScreen(novelName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var expandedMenu by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }
    var zoomImagePage by remember { mutableStateOf<NovelPage?>(null) }

    // Configurable States
    var showImages by remember { mutableStateOf(prefs.getBoolean("reader_show_images", true)) }
    var isVerticalMode by remember {
        mutableStateOf(
            prefs.getBoolean(
                "reader_vertical_mode",
                false
            )
        )
    }

    var pages by remember(novelName) {
        mutableStateOf(
            NovelRepository.getPages(
                context,
                novelName
            )
        )
    }
    var isManagingPages by remember { mutableStateOf(false) }

    // Navigation and Page Synchronization
    val pagerState = androidx.compose.runtime.key(pages) {
        if (pages.isNotEmpty()) {
            rememberPagerState(
                pageCount = { pages.size },
                initialPage = (pages.size - 1).coerceAtLeast(0)
            )
        } else {
            null
        }
    }
    val listState = androidx.compose.runtime.key(pages) {
        rememberLazyListState(initialFirstVisibleItemIndex = (pages.size - 1).coerceAtLeast(0))
    }

    val currentPageIndex by remember(listState, pagerState, isVerticalMode) {
        derivedStateOf {
            if (isVerticalMode) {
                listState.firstVisibleItemIndex
            } else {
                pagerState?.currentPage ?: 0
            }
        }
    }

    LaunchedEffect(isVerticalMode, pagerState, listState) {
        if (isVerticalMode) {
            val targetPage = pagerState?.currentPage ?: 0
            if (targetPage < pages.size) {
                listState.scrollToItem(targetPage)
            }
        } else {
            val targetPage = listState.firstVisibleItemIndex
            if (targetPage < pages.size) {
                pagerState?.scrollToPage(targetPage)
            }
        }
    }


    var showSystemBars by remember { mutableStateOf(true) }

    if (isManagingPages) {
        PageManagementScreen(
            novelName = novelName,
            onBack = {
                pages = NovelRepository.getPages(context, novelName)
                isManagingPages = false
            },
            onPageSelected = { index ->
                pages = NovelRepository.getPages(context, novelName)
                isManagingPages = false
                coroutineScope.launch {
                    if (isVerticalMode) {
                        listState.scrollToItem(index)
                    } else {
                        pagerState?.scrollToPage(index)
                    }
                }
                showSystemBars = false
            }
        )
    } else {
        var pageInputText by remember(currentPageIndex) { mutableStateOf((currentPageIndex + 1).toString()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showSystemBars = !showSystemBars
                }
        ) {
            // Main reader contents (Pager or LazyColumn)
            if (pages.isEmpty()) {
                EmptyNovelState(PaddingValues())
            } else {
                if (isVerticalMode) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp)
                    ) {
                        itemsIndexed(pages) { index, page ->
                            NovelPageItem(
                                page = page,
                                showImages = showImages,
                                isScrollEnabled = false,
                                onImageClick = {
                                    zoomImagePage = page
                                    showZoomDialog = true
                                },
                                strings = strings
                            )
                            if (index < pages.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else if (pagerState != null) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        val page = pages[pageIndex]
                        NovelPageItem(
                            page = page,
                            showImages = showImages,
                            isScrollEnabled = true,
                            onImageClick = {
                                zoomImagePage = page
                                showZoomDialog = true
                            },
                            strings = strings
                        )
                    }
                }
            }

            // Top Bar Overlay
            AnimatedVisibility(
                visible = showSystemBars,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(novelName, style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.navBack
                            )
                        }
                    },
                    actions = {
                        if (pages.isNotEmpty()) {
                            Box {
                                IconButton(onClick = { expandedMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = strings.novelDetailsViewPages
                                    )
                                }
                                DropdownMenuPopup(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false },
                                    modifier = Modifier.width(260.dp)
                                ) {
                                    DropdownMenuGroup(
                                        shapes = MenuDefaults.groupShape(0, 1)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    strings.novelDetailsGoToPage,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Book,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                expandedMenu = false
                                                isManagingPages = true
                                            }
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding))

                                        // Show/Hide Images Switch Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showImages = !showImages
                                                    prefs.edit {
                                                        putBoolean("reader_show_images", showImages)
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (showImages) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    strings.readerShowImages,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                            }
                                            Switch(
                                                checked = showImages,
                                                onCheckedChange = { value ->
                                                    showImages = value
                                                    prefs.edit {
                                                        putBoolean("reader_show_images", value)
                                                    }
                                                },
                                                thumbContent = {
                                                    if (showImages) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                                        )
                                                    }
                                                }
                                            )
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding))

                                        // Reading orientation layout selector
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = strings.readerReadingOrientation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    ButtonGroupDefaults.ConnectedSpaceBetween
                                                )
                                            ) {
                                                ToggleButton(
                                                    checked = !isVerticalMode,
                                                    onCheckedChange = {
                                                        isVerticalMode = false
                                                        prefs.edit {
                                                            putBoolean(
                                                                "reader_vertical_mode",
                                                                false
                                                            )
                                                        }
                                                    },
                                                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.StayCurrentLandscape,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            strings.readerHorizontal,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )

                                                    }
                                                }
                                                ToggleButton(
                                                    checked = isVerticalMode,
                                                    onCheckedChange = {
                                                        isVerticalMode = true
                                                        prefs.edit {
                                                            putBoolean(
                                                                "reader_vertical_mode",
                                                                true
                                                            )
                                                        }
                                                    },
                                                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.StayCurrentPortrait,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            strings.readerVertical,
                                                            style = MaterialTheme.typography.bodySmall
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
                )
            }

            // Bottom Floating Navigation Overlay
            AnimatedVisibility(
                visible = showSystemBars && pages.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                var isExpanded by remember { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current

                // Reset expanded state when overlay is hidden
                LaunchedEffect(showSystemBars) {
                    if (!showSystemBars) {
                        isExpanded = false
                    }
                }

                // Sync pageInputText and request focus when expanded state changes
                LaunchedEffect(isExpanded) {
                    if (!isExpanded) {
                        pageInputText = (currentPageIndex + 1).toString()
                    } else {
                        pageInputText = ""
                        try {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } catch (_: Exception) {
                        }
                    }
                }
                HorizontalFloatingToolbar(
                    expanded = isExpanded,
                    modifier = Modifier.padding(8.dp),
                    leadingContent = {
                        IconButton(
                            onClick = {
                                val target = pageInputText.toIntOrNull()
                                if (target != null && target in 1..pages.size) {
                                    coroutineScope.launch {
                                        if (isVerticalMode) {
                                            listState.scrollToItem(target - 1)
                                        } else {
                                            pagerState?.scrollToPage(target - 1)
                                        }
                                        isExpanded = false
                                        showSystemBars = false
                                    }
                                }
                            },
                            enabled = pageInputText.toIntOrNull() in 1..pages.size
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = strings.buttonGo,
                                tint = if (pageInputText.toIntOrNull() in 1..pages.size) {

                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    },
                    trailingContent = {
                        Text(
                            text = "${currentPageIndex + 1} ${strings.novelDetailsPageOf} ${pages.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    },
                    content = {
                        if (!isExpanded) {
                            Text(
                                text = "${currentPageIndex + 1} ${strings.novelDetailsPageOf} ${pages.size}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable { isExpanded = true }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                try {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                } catch (_: Exception) {
                                }
                            }
                            BasicTextField(
                                value = pageInputText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 4) {
                                        pageInputText = input
                                    }
                                },
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        val target = pageInputText.toIntOrNull()
                                        if (target != null && target in 1..pages.size) {
                                            coroutineScope.launch {
                                                if (isVerticalMode) {
                                                    listState.scrollToItem(target - 1)
                                                } else {
                                                    pagerState?.scrollToPage(target - 1)
                                                }
                                                isExpanded = false
                                                showSystemBars = false
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .width(60.dp)
                                    .focusRequester(focusRequester)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                singleLine = true
                            )
                        }
                    }
                )
            }
        }
    }


    // Pinch-to-zoom full screen dialog
    if (showZoomDialog && zoomImagePage?.imagePath != null) {
        var isUiVisible by remember { mutableStateOf(true) }
        var isZoomed by remember { mutableStateOf(false) }
        Dialog(
            onDismissRequest = { showZoomDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TelephotoSwipeDismissImage(
                        imageUrl = java.io.File(zoomImagePage!!.imagePath!!),
                        onDismiss = { showZoomDialog = false },
                        onClick = { isUiVisible = !isUiVisible },
                        onZoomedChanged = { zoomed ->
                            isZoomed = zoomed
                            if (zoomed) {
                                isUiVisible = false
                            } else {
                                isUiVisible = true
                            }
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
                            IconButton(onClick = { showZoomDialog = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
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

@Composable
fun NovelPageItem(
    page: NovelPage,
    showImages: Boolean,
    isScrollEnabled: Boolean,
    onImageClick: () -> Unit,
    strings: UiStrings
) {
    val scrollState = rememberScrollState()
    val isOnlyImage = page.translatedText.isBlank() && page.originalText.isNullOrBlank()
    val scrollModifier = if (isScrollEnabled && !isOnlyImage) Modifier.verticalScroll(scrollState) else Modifier

    val baseModifier = if (isScrollEnabled) {
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Column(
        modifier = baseModifier
            .then(scrollModifier)
    ) {
        if (isScrollEnabled) {
            Spacer(modifier = Modifier.height(72.dp))
        }
        if ((showImages || isOnlyImage) && page.imagePath != null) {
            val bitmap = remember(page.imagePath) {
                try {
                    BitmapFactory.decodeFile(page.imagePath)
                } catch (_: Exception) {
                    null
                }
            }
            bitmap?.let {
                val cardModifier = if (isOnlyImage) {
                    if (isScrollEnabled) {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onImageClick() }
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .clickable { onImageClick() }
                    }
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { onImageClick() }
                }

                Card(
                    modifier = cardModifier,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isOnlyImage) 0.dp else 4.dp),
                    colors = if (isOnlyImage) CardDefaults.cardColors(containerColor = Color.Transparent) else CardDefaults.cardColors()
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (isOnlyImage) ContentScale.Fit else ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (!page.translatedText.isNullOrBlank()) {
            if (showImages) {
                Text(
                    text = strings.readerTranslationHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            MarkdownText(
                markdown = page.translatedText,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        if (!page.originalText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = strings.readerOriginalTextHeader,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.originalText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isScrollEnabled) {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
fun EmptyNovelState(padding: PaddingValues) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = strings.novelDetailsEmptyTitle,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.novelDetailsEmptySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
