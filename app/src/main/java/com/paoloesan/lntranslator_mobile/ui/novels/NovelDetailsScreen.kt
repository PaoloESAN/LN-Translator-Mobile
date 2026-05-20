package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
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
    var showPageDialog by remember { mutableStateOf(false) }
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

    val pages = remember(novelName) { NovelRepository.getPages(context, novelName) }

    // Navigation and Page Synchronization
    val pagerState = if (pages.isNotEmpty()) {
        rememberPagerState(pageCount = { pages.size }, initialPage = pages.size - 1)
    } else {
        null
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = pages.size - 1)

    val currentPageIndex by remember {
        derivedStateOf {
            if (isVerticalMode) {
                listState.firstVisibleItemIndex
            } else {
                pagerState?.currentPage ?: 0
            }
        }
    }

    LaunchedEffect(isVerticalMode) {
        if (isVerticalMode) {
            val targetPage = pagerState?.currentPage ?: 0
            listState.scrollToItem(targetPage)
        } else {
            val targetPage = listState.firstVisibleItemIndex
            pagerState?.scrollToPage(targetPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(novelName, style = MaterialTheme.typography.titleMedium)
                        if (pages.isNotEmpty()) {
                            Text(
                                "${strings.novelDetailsPageNumber} ${currentPageIndex + 1} ${strings.novelDetailsPageOf} ${pages.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                                            showPageDialog = true
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
                                                "Imágenes",
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
                                            text = "Orientación de lectura",
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
                                                        "Horizontal",
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
                                                        "Vertical",
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
    ) { padding ->
        if (pages.isEmpty()) {
            EmptyNovelState(padding)
        } else {
            if (isVerticalMode) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(pages) { index, page ->
                        NovelPageItem(
                            page = page,
                            showImages = showImages,
                            isScrollEnabled = false,
                            onImageClick = {
                                zoomImagePage = page
                                showZoomDialog = true
                            }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                        }
                    )
                }
            }
        }
    }

    if (showPageDialog) {
        PageSelectionDialog(
            pages = pages,
            currentPage = currentPageIndex,
            onPageSelected = { index ->
                coroutineScope.launch {
                    if (isVerticalMode) {
                        listState.scrollToItem(index)
                    } else {
                        pagerState?.scrollToPage(index)
                    }
                }
            },
            onDismiss = { showPageDialog = false }
        )
    }

    // Pinch-to-zoom full screen dialog
    if (showZoomDialog && zoomImagePage?.imagePath != null) {
        val bitmap = remember(zoomImagePage!!.imagePath) {
            try {
                BitmapFactory.decodeFile(zoomImagePage!!.imagePath)
            } catch (e: Exception) {
                null
            }
        }
        bitmap?.let { b ->
            Dialog(
                onDismissRequest = { showZoomDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.95f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        var scale by remember { mutableFloatStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        val state = rememberTransformableState { _, zoomChange, panChange, _ ->
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            offset += panChange
                        }

                        Image(
                            bitmap = b.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(state = state),
                            contentScale = ContentScale.Fit
                        )

                        IconButton(
                            onClick = { showZoomDialog = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .statusBarsPadding()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageSelectionDialog(
    pages: List<NovelPage>,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var sliderValue by remember { mutableFloatStateOf(currentPage.toFloat()) }

    // Sync sliderValue if currentPage changes
    LaunchedEffect(currentPage) {
        sliderValue = currentPage.toFloat()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.novelDetailsGoToPage) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Interactive Slider controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${strings.novelDetailsPageNumber} ${sliderValue.toInt() + 1} ${strings.novelDetailsPageOf} ${pages.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            enabled = sliderValue > 0f,
                            onClick = {
                                sliderValue = (sliderValue - 1).coerceAtLeast(0f)
                                onPageSelected(sliderValue.toInt())
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = {
                                onPageSelected(sliderValue.toInt())
                            },
                            valueRange = 0f..(pages.size - 1).toFloat(),
                            steps = if (pages.size > 2) pages.size - 2 else 0,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            enabled = sliderValue < (pages.size - 1).toFloat(),
                            onClick = {
                                sliderValue =
                                    (sliderValue + 1).coerceAtMost((pages.size - 1).toFloat())
                                onPageSelected(sliderValue.toInt())
                            }
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Página siguiente"
                            )
                        }
                    }
                }

                Text(
                    text = "Todas las páginas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(pages) { index, page ->
                        val isSelected = index == sliderValue.toInt()
                        val snippet =
                            page.translatedText.lineSequence().firstOrNull()?.take(40) ?: ""
                        val pageText = "${strings.novelDetailsPageNumber} ${index + 1}"

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.4f
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sliderValue = index.toFloat()
                                    onPageSelected(index)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pageText,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (snippet.isNotEmpty()) {
                                        Text(
                                            text = if (snippet.length >= 40) "$snippet..." else snippet,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.8f
                                            ) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun NovelPageItem(
    page: NovelPage,
    showImages: Boolean,
    isScrollEnabled: Boolean,
    onImageClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scrollModifier = if (isScrollEnabled) Modifier.verticalScroll(scrollState) else Modifier

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
        if (showImages && page.imagePath != null) {
            val bitmap = remember(page.imagePath) {
                try {
                    BitmapFactory.decodeFile(page.imagePath)
                } catch (_: Exception) {
                    null
                }
            }
            bitmap?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { onImageClick() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (showImages) {
            Text(
                text = "Traducción",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        MarkdownText(
            markdown = page.translatedText,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        if (!page.originalText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Texto Original",
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
            Spacer(modifier = Modifier.height(32.dp))
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
