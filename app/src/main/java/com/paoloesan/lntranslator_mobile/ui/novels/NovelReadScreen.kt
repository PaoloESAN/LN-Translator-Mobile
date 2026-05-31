package com.paoloesan.lntranslator_mobile.ui.novels

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.ui.novels.components.NovelPage
import com.paoloesan.lntranslator_mobile.ui.novels.components.NovelRepository
import com.paoloesan.lntranslator_mobile.ui.novels.components.ReaderConfigDialog
import com.paoloesan.lntranslator_mobile.ui.novels.components.ShareNovelDialog
import com.paoloesan.lntranslator_mobile.ui.novels.components.SwipeDismissZoomImage
import com.paoloesan.lntranslator_mobile.ui.overlay.OverlayFontOption
import com.paoloesan.lntranslator_mobile.ui.overlay.toComposeFontFamily
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NovelDetailsScreen(novelName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current

    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var expandedMenu by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }
    var zoomImagePage by remember { mutableStateOf<NovelPage?>(null) }

    // Configurable States
    var showOriginal by remember { mutableStateOf(prefs.getBoolean("reader_show_original", false)) }
    var invertHorizontalNav by remember {
        mutableStateOf(
            prefs.getBoolean(
                "reader_invert_horizontal_nav",
                false
            )
        )
    }
    var isVerticalMode by remember {
        mutableStateOf(
            prefs.getBoolean(
                "reader_vertical_mode",
                false
            )
        )
    }
    var readerFontSize by remember {
        mutableIntStateOf(prefs.getInt("reader_font_size", 18))
    }
    var readerLineSpacing by remember {
        mutableIntStateOf(prefs.getInt("reader_line_spacing", 5))
    }
    var readerFontFamily by remember {
        mutableStateOf(
            OverlayFontOption.fromPref(
                prefs.getString("reader_font_family", OverlayFontOption.ROBOTO.prefValue)
            )
        )
    }
    var showConfigDialog by remember { mutableStateOf(false) }

    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var tempFileForSharing by remember { mutableStateOf<java.io.File?>(null) }

    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { saveToUri(context, it, tempFileForSharing, coroutineScope, strings) }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { saveToUri(context, it, tempFileForSharing, coroutineScope, strings) }
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
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    val savedLastPage = remember(novelName, pages) {
        prefs.getInt("last_page_$novelName", 0).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    }

    // Navigation and Page Synchronization
    val pagerState = androidx.compose.runtime.key(pages) {
        if (pages.isNotEmpty()) {
            rememberPagerState(
                pageCount = { pages.size },
                initialPage = savedLastPage
            )
        } else {
            null
        }
    }
    val listState = androidx.compose.runtime.key(pages) {
        rememberLazyListState(initialFirstVisibleItemIndex = savedLastPage)
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

    LaunchedEffect(
        showSystemBars,
        isManagingPages,
        novelName,
        pages,
        showOriginal,
        isVerticalMode,
        expandedMenu,
        isSearchActive,
        searchQuery
    ) {
        if (isManagingPages) {
            topBarVisible.value = true
        } else {
            topBarVisible.value = showSystemBars
            if (showSystemBars) {
                if (isSearchActive) {
                    topBarTitle.value = {
                        val focusRequester = remember { FocusRequester() }
                        val keyboardController = LocalSoftwareKeyboardController.current
                        LaunchedEffect(Unit) {
                            try {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            } catch (_: Exception) {
                            }
                        }
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(strings.readerSearchPlaceholder) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        )
                    }
                    topBarNavIcon.value = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.navBack
                            )
                        }
                    }
                    topBarActions.value = {}
                } else {
                    topBarTitle.value = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (novelName.length > 5) novelName.dropLast(5) else novelName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (novelName.length > 5) {
                                Text(
                                    text = novelName.takeLast(5),
                                    style = MaterialTheme.typography.titleMedium,
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
                    topBarActions.value = {
                        if (pages.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
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
                                            HorizontalDivider(
                                                modifier = Modifier.padding(
                                                    MenuDefaults.HorizontalDividerPadding
                                                )
                                            )

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

                                            HorizontalDivider(
                                                modifier = Modifier.padding(
                                                    MenuDefaults.HorizontalDividerPadding
                                                )
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        strings.menuShare,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Share,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    expandedMenu = false
                                                    val zipFile =
                                                        NovelRepository.exportNovelToZip(
                                                            context,
                                                            novelName
                                                        )
                                                    if (zipFile != null && zipFile.exists()) {
                                                        tempFileForSharing = zipFile
                                                        showShareOptionsDialog = true
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            strings.novelEmptyError,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        strings.overlayConfig,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Settings,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    expandedMenu = false
                                                    showConfigDialog = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                topBarColors.value = null
            }
        }
    }

    var isFirstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(currentPageIndex) {
        if (pages.isNotEmpty()) {
            prefs.edit { putInt("last_page_$novelName", currentPageIndex) }
        }
        if (isFirstLoad) {
            isFirstLoad = false
        } else {
            showSystemBars = false
        }
    }

    AnimatedContent(
        targetState = isManagingPages,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(
                    animationSpec = tween(400)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400)))
            } else {
                (slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400)))
            }
        },
        label = "PageManagementTransition",
        modifier = Modifier.fillMaxSize()
    ) { managing ->
        if (managing) {
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
            val isCoverFirst = remember(pages) { pages.firstOrNull()?.id == "cover_page" }
            val displayPageNumber = if (isCoverFirst) {
                if (currentPageIndex == 0) 0 else currentPageIndex
            } else {
                currentPageIndex + 1
            }
            var pageInputText by remember(currentPageIndex) {
                mutableStateOf(if (displayPageNumber == 0) "" else displayPageNumber.toString())
            }

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
                                    showOriginal = showOriginal,
                                    isScrollEnabled = false,
                                    readerFontSize = readerFontSize,
                                    readerLineSpacing = readerLineSpacing,
                                    readerFontFamily = readerFontFamily,
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
                            beyondViewportPageCount = 1,
                            reverseLayout = invertHorizontalNav
                        ) { pageIndex ->
                            val page = pages[pageIndex]
                            NovelPageItem(
                                page = page,
                                showOriginal = showOriginal,
                                isScrollEnabled = true,
                                readerFontSize = readerFontSize,
                                readerLineSpacing = readerLineSpacing,
                                readerFontFamily = readerFontFamily,
                                onImageClick = {
                                    zoomImagePage = page
                                    showZoomDialog = true
                                },
                                strings = strings
                            )
                        }
                    }
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
                            pageInputText =
                                if (displayPageNumber == 0) "" else displayPageNumber.toString()
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
                                    val maxPage = if (isCoverFirst) pages.size - 1 else pages.size
                                    if (target != null && target in 1..maxPage) {
                                        coroutineScope.launch {
                                            val targetIndex =
                                                if (isCoverFirst) target else target - 1
                                            if (isVerticalMode) {
                                                listState.scrollToItem(targetIndex)
                                            } else {
                                                pagerState?.scrollToPage(targetIndex)
                                            }
                                            isExpanded = false
                                            showSystemBars = false
                                        }
                                    }
                                },
                                enabled = pageInputText.toIntOrNull()?.let { target ->
                                    val maxPage = if (isCoverFirst) pages.size - 1 else pages.size
                                    target in 1..maxPage
                                } == true
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = strings.buttonGo,
                                    tint = if (pageInputText.toIntOrNull()?.let { target ->
                                            val maxPage =
                                                if (isCoverFirst) pages.size - 1 else pages.size
                                            target in 1..maxPage
                                        } == true) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            }
                        },
                        trailingContent = {
                            val pageDisplayText = if (isCoverFirst) {
                                if (currentPageIndex == 0) {
                                    strings.pageManagementCoverPage
                                } else {
                                    "$currentPageIndex ${strings.novelDetailsPageOf} ${pages.size - 1}"
                                }
                            } else {
                                "${currentPageIndex + 1} ${strings.novelDetailsPageOf} ${pages.size}"
                            }
                            Text(
                                text = pageDisplayText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        },
                        content = {
                            val pageDisplayText = if (isCoverFirst) {
                                if (currentPageIndex == 0) {
                                    strings.pageManagementCoverPage
                                } else {
                                    "$currentPageIndex ${strings.novelDetailsPageOf} ${pages.size - 1}"
                                }
                            } else {
                                "${currentPageIndex + 1} ${strings.novelDetailsPageOf} ${pages.size}"
                            }
                            if (!isExpanded) {
                                Text(
                                    text = pageDisplayText,
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
                                            val maxPage =
                                                if (isCoverFirst) pages.size - 1 else pages.size
                                            if (target != null && target in 1..maxPage) {
                                                coroutineScope.launch {
                                                    val targetIndex =
                                                        if (isCoverFirst) target else target - 1
                                                    if (isVerticalMode) {
                                                        listState.scrollToItem(targetIndex)
                                                    } else {
                                                        pagerState?.scrollToPage(targetIndex)
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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    singleLine = true
                                )
                            }
                        }
                    )
                }
                // Search Results Overlay
                if (isSearchActive && searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isSearchActive = false
                                searchQuery = ""
                            }
                            .statusBarsPadding()
                            .padding(top = 64.dp)
                            .zIndex(2f) // Sit on top of floating toolbar
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(Alignment.TopCenter)
                                .clickable(enabled = false) {}, // Prevent clicks on backdrop
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            val filteredPages = remember(pages, searchQuery) {
                                pages.mapIndexed { index, page -> index to page }
                                    .filter { (_, page) ->
                                        page.translatedText.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        ) ||
                                                (page.originalText?.contains(
                                                    searchQuery,
                                                    ignoreCase = true
                                                ) == true)
                                    }
                            }

                            if (filteredPages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.readerSearchNoResults,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                ) {
                                    itemsIndexed(filteredPages) { itemIndex, (index, page) ->
                                        val isCover = page.id == "cover_page"
                                        val displayTitle = if (isCover) {
                                            strings.pageManagementCoverPage
                                        } else {
                                            val isCoverFirst =
                                                pages.firstOrNull()?.id == "cover_page"
                                            val displayIndex =
                                                if (isCoverFirst) index else index + 1
                                            strings.novelDetailsPageNumber + " $displayIndex"
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    coroutineScope.launch {
                                                        if (isVerticalMode) {
                                                            listState.scrollToItem(index)
                                                        } else {
                                                            pagerState?.scrollToPage(index)
                                                        }
                                                    }
                                                    isSearchActive = false
                                                    searchQuery = ""
                                                    showSystemBars = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = displayTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val textContent =
                                                page.translatedText.ifBlank {
                                                    page.originalText ?: ""
                                                }
                                            val snippet = getSearchSnippet(textContent, searchQuery)
                                            Text(
                                                text = snippet,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (itemIndex < filteredPages.size - 1) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                    alpha = 0.5f
                                                )
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


        if (showConfigDialog) {
            ReaderConfigDialog(
                fontSize = readerFontSize,
                lineSpacing = readerLineSpacing,
                fontFamily = readerFontFamily,
                showOriginal = showOriginal,
                onFontSizeChange = { newSize ->
                    readerFontSize = newSize
                    prefs.edit { putInt("reader_font_size", newSize) }
                },
                onLineSpacingChange = { newSpacing ->
                    readerLineSpacing = newSpacing
                    prefs.edit { putInt("reader_line_spacing", newSpacing) }
                },
                onFontFamilyChange = { newFontFamily ->
                    readerFontFamily = newFontFamily
                    prefs.edit { putString("reader_font_family", newFontFamily.prefValue) }
                },
                onShowOriginalChange = { value ->
                    showOriginal = value
                    prefs.edit { putBoolean("reader_show_original", value) }
                },
                invertHorizontalNav = invertHorizontalNav,
                onInvertHorizontalNavChange = { value ->
                    invertHorizontalNav = value
                    prefs.edit { putBoolean("reader_invert_horizontal_nav", value) }
                },
                onDismiss = { showConfigDialog = false },
                strings = strings
            )
        }

        if (showShareOptionsDialog) {
            ShareNovelDialog(
                novelName = novelName,
                strings = strings,
                onDismiss = { showShareOptionsDialog = false },
                onExportZip = {
                    val zipFile = NovelRepository.exportNovelToZip(context, novelName)
                    if (zipFile != null && zipFile.exists()) {
                        tempFileForSharing = zipFile
                        showShareOptionsDialog = false
                        createZipLauncher.launch("novel_${novelName}.zip")
                    } else {
                        Toast.makeText(context, strings.novelEmptyError, Toast.LENGTH_SHORT).show()
                    }
                },
                onExportPdf = {
                    val pdfFile = NovelRepository.exportNovelToPdf(context, novelName)
                    if (pdfFile != null && pdfFile.exists()) {
                        tempFileForSharing = pdfFile
                        showShareOptionsDialog = false
                        createPdfLauncher.launch("novel_${novelName}.pdf")
                    } else {
                        Toast.makeText(context, strings.novelEmptyError, Toast.LENGTH_SHORT).show()
                    }
                }
            )
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
                        SwipeDismissZoomImage(
                            imageUrl = java.io.File(zoomImagePage!!.imagePath!!),
                            onDismiss = { showZoomDialog = false },
                            onClick = { isUiVisible = !isUiVisible },
                            onZoomedChanged = { zoomed ->
                                isZoomed = zoomed
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
}


@Composable
fun NovelPageItem(
    page: NovelPage,
    showOriginal: Boolean,
    isScrollEnabled: Boolean,
    readerFontSize: Int,
    readerLineSpacing: Int,
    readerFontFamily: OverlayFontOption,
    onImageClick: () -> Unit,
    strings: UiStrings
) {
    val scrollState = rememberScrollState()
    val isOnlyImage = page.translatedText.isBlank() && page.originalText.isNullOrBlank()
    val showGradient by remember(scrollState, isScrollEnabled, isOnlyImage) {
        derivedStateOf {
            isScrollEnabled && !isOnlyImage && scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue
        }
    }
    val scrollModifier =
        if (isScrollEnabled && !isOnlyImage) Modifier.verticalScroll(scrollState) else Modifier

    val baseModifier = if (isScrollEnabled) {
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Box(modifier = baseModifier) {
        Column(
            modifier = if (isScrollEnabled) Modifier
                .fillMaxSize()
                .then(scrollModifier) else Modifier
                .fillMaxWidth()
                .then(scrollModifier)
        ) {
            if (isScrollEnabled) {
                Spacer(modifier = Modifier.height(72.dp))
            }
            if ((showOriginal || isOnlyImage) && page.imagePath != null) {
                val bitmap = remember(page.imagePath) {
                    try {
                        BitmapFactory.decodeFile(page.imagePath)
                    } catch (_: Exception) {
                        null
                    }
                }
                bitmap?.let {
                    if (isOnlyImage) {
                        BoxWithConstraints(
                            modifier = if (isScrollEnabled) Modifier
                                .fillMaxWidth()
                                .weight(1f) else Modifier
                                .fillMaxWidth()
                                .height(500.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageRatio = it.width.toFloat() / it.height.toFloat()
                            val maxRatio = maxWidth / maxHeight
                            val (imageWidth, imageHeight) = if (imageRatio > maxRatio) {
                                Pair(maxWidth, maxWidth / imageRatio)
                            } else {
                                Pair(maxHeight * imageRatio, maxHeight)
                            }
                            Card(
                                modifier = Modifier
                                    .size(imageWidth, imageHeight)
                                    .clickable { onImageClick() },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else {
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
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            if (page.translatedText.isNotBlank()) {
                if (showOriginal) {
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
                        fontSize = readerFontSize.sp,
                        lineHeight = (readerFontSize + readerLineSpacing).sp,
                        fontFamily = readerFontFamily.toComposeFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (!page.originalText.isNullOrBlank() && showOriginal) {
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (readerFontSize * 0.85f).sp,
                        lineHeight = ((readerFontSize + readerLineSpacing) * 0.85f).sp,
                        fontFamily = readerFontFamily.toComposeFontFamily(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (isScrollEnabled) {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Show bottom gradient indicator if scrollable and not at the end
        if (showGradient) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
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


private fun saveToUri(
    context: Context,
    uri: android.net.Uri,
    file: java.io.File?,
    scope: kotlinx.coroutines.CoroutineScope,
    strings: UiStrings
) {
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

private fun getSearchSnippet(text: String, query: String): String {
    val cleanText = text
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("\\*(.*?)\\*"), "$1")
        .replace(Regex("#+\\s+"), "")
        .replace('\n', ' ')
        .trim()

    val index = cleanText.indexOf(query, ignoreCase = true)
    if (index == -1) return if (cleanText.length > 80) cleanText.take(80) + "..." else cleanText

    val start = (index - 35).coerceAtLeast(0)
    val end = (index + query.length + 35).coerceAtMost(cleanText.length)
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < cleanText.length) "..." else ""
    return prefix + cleanText.substring(start, end) + suffix
}

