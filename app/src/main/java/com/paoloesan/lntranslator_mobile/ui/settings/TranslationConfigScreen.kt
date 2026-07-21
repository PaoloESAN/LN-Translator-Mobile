package com.paoloesan.lntranslator_mobile.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarLarge
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager

private data class ModelInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val pricing: String,
    val icon: ImageVector,
    val tag: String,
)

@Composable
private fun CategoryLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier.padding(top = 10.dp, bottom = 6.dp),
    )
}

@Composable
private fun OcrModeCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    index: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ocrCardColor",
    )

    val defaultTopStart = if (index == 0) 20.dp else 4.dp
    val defaultTopEnd = if (index == 0) 20.dp else 4.dp
    val defaultBottomStart = if (index == totalCount - 1) 20.dp else 4.dp
    val defaultBottomEnd = if (index == totalCount - 1) 20.dp else 4.dp

    val topStart by animateDpAsState(
        targetValue = if (selected) 20.dp else defaultTopStart,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ocrTopStart",
    )
    val topEnd by animateDpAsState(
        targetValue = if (selected) 20.dp else defaultTopEnd,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ocrTopEnd",
    )
    val bottomStart by animateDpAsState(
        targetValue = if (selected) 20.dp else defaultBottomStart,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ocrBottomStart",
    )
    val bottomEnd by animateDpAsState(
        targetValue = if (selected) 20.dp else defaultBottomEnd,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ocrBottomEnd",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TranslationConfigScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val topBarLarge = LocalTopAppBarLarge.current

    val initialProviderId = remember {
        DataStoreManager.getString(context, "active_translation_provider", "gemini_35_flash_lite")
            ?: "gemini_35_flash_lite"
    }
    var useOcr by remember { mutableStateOf(initialProviderId.startsWith("ocr_")) }
    var selectedModelId by remember {
        val rawId = initialProviderId.removePrefix("ocr_")
        val validIds = listOf("gemini_31_flash", "gemini_35_flash_lite", "gemini_36_flash")
        mutableStateOf(if (rawId in validIds) rawId else "gemini_35_flash_lite")
    }

    var showAdvanced by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val apiKeys = remember { mutableStateListOf<String>() }
    val maxApiKeys = 5

    val availableModels = remember(strings) {
        listOf(
            ModelInfo(
                id = "gemini_36_flash",
                name = "Gemini 3.6 Flash",
                version = "gemini-3.6-flash",
                description = strings.configGemini36FlashDesc,
                pricing = strings.configGemini36FlashPricing,
                icon = Icons.Default.AutoAwesome,
                tag = "3.6 Flash",
            ),
            ModelInfo(
                id = "gemini_35_flash_lite",
                name = "Gemini 3.5 Flash Lite",
                version = "gemini-3.5-flash-lite",
                description = strings.configGemini35FlashLiteDesc,
                pricing = strings.configGemini35FlashLitePricing,
                icon = Icons.Default.Speed,
                tag = "3.5 Flash Lite",
            ),
            ModelInfo(
                id = "gemini_31_flash",
                name = "Gemini 3.1 Flash Lite",
                version = "gemini-3.1-flash-lite",
                description = strings.configGemini31FlashLiteDesc,
                pricing = strings.configGemini31FlashLitePricing,
                icon = Icons.Default.FlashOn,
                tag = "3.1 Flash Lite",
            ),
        )
    }

    val currentIndex = remember(selectedModelId) {
        availableModels.indexOfFirst { it.id == selectedModelId }.coerceAtLeast(0)
    }
    val currentModel = availableModels[currentIndex]

    LaunchedEffect(Unit) {
        topBarVisible.value = true
        topBarLarge.value = false
        topBarTitle.value = { Text(strings.topbarTranslationConfig) }
        topBarNavIcon.value = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.navBack,
                )
            }
        }
        topBarActions.value = {}
        topBarColors.value = null

        val jsonString = DataStoreManager.getString(context, "api_keys_list", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val keys: List<String> = Gson().fromJson(jsonString, type)
                apiKeys.addAll(keys)
            } catch (_: Exception) {
            }
        }
        if (apiKeys.isEmpty()) apiKeys.add("")
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            topBarLarge.value = false
        }
    }

    LaunchedEffect(useOcr, selectedModelId) {
        val finalProviderId = if (useOcr) "ocr_$selectedModelId" else selectedModelId
        DataStoreManager.putStringSync(context, "active_translation_provider", finalProviderId)
    }

    fun saveApiKeys() {
        val validKeys = apiKeys.filter { it.isNotBlank() }
        DataStoreManager.putStringSync(context, "api_keys_list", Gson().toJson(validKeys))
        DataStoreManager.putIntSync(context, "api_key_index", 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 64.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryLabel(
                text = strings.settingsApikeyTitle,
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(
                onClick = { apiKeys.add(""); saveApiKeys() },
                enabled = apiKeys.size < maxApiKeys,
                modifier = Modifier
                    .size(width = 50.dp, height = ButtonDefaults.ExtraSmallContainerHeight),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.keyAddButton,
                    modifier = Modifier.size(ButtonDefaults.SmallIconSize),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            apiKeys.forEachIndexed { index, key ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { apiKeys[index] = it; saveApiKeys() },
                        modifier = Modifier.weight(1f),
                        label = { Text(strings.keyLabel(index + 1)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    if (apiKeys.size > 1) {
                        IconButton(
                            onClick = { apiKeys.removeAt(index); saveApiKeys() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = strings.keyDeleteContentDescription,
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        val linkColor = MaterialTheme.colorScheme.primary
                        val annotatedText = buildAnnotatedString {
                            append(strings.keyGetHere)
                            append(" ")
                            withLink(
                                LinkAnnotation.Url(
                                    url = "https://aistudio.google.com/app/apikey",
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = linkColor,
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    ),
                                ),
                            ) { append("Google AI Studio") }
                        }
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = strings.keyRotationInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val arrowRotation by animateFloatAsState(
            targetValue = if (showAdvanced) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "arrowRotation",
        )

        val headerBottomCorner by animateDpAsState(
            targetValue = if (showAdvanced) 4.dp else 20.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "headerBottomCorner",
        )

        val headerContainerColor by animateColorAsState(
            targetValue = if (showAdvanced)
                MaterialTheme.colorScheme.surfaceContainerLow
            else
                androidx.compose.ui.graphics.Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "headerContainerColor",
        )

        Surface(
            onClick = { showAdvanced = !showAdvanced },
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = headerBottomCorner,
                bottomEnd = headerBottomCorner,
            ),
            color = headerContainerColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = if (showAdvanced) 2.dp else 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.configAdvancedOptions,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.configAdvancedSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (showAdvanced) strings.configHideAdvanced else strings.configShowAdvanced,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer(rotationZ = arrowRotation),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showAdvanced,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CategoryLabel(
                    text = strings.configAiModelTitle,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 2.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    availableModels.forEachIndexed { index, model ->
                        val isSelected = index == currentIndex

                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "modelItemColor",
                        )

                        val defaultTopStart = if (index == 0) 20.dp else 4.dp
                        val defaultTopEnd = if (index == 0) 20.dp else 4.dp
                        val defaultBottomStart =
                            if (index == availableModels.size - 1) 20.dp else 4.dp
                        val defaultBottomEnd =
                            if (index == availableModels.size - 1) 20.dp else 4.dp

                        val topStart by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else defaultTopStart,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "modelTopStart",
                        )
                        val topEnd by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else defaultTopEnd,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "modelTopEnd",
                        )
                        val bottomStart by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else defaultBottomStart,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "modelBottomStart",
                        )
                        val bottomEnd by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else defaultBottomEnd,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "modelBottomEnd",
                        )

                        Surface(
                            onClick = { selectedModelId = model.id },
                            shape = RoundedCornerShape(
                                topStart = topStart,
                                topEnd = topEnd,
                                bottomStart = bottomStart,
                                bottomEnd = bottomEnd,
                            ),
                            color = containerColor,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = model.icon,
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = currentModel.pricing,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                CategoryLabel(
                    text = strings.configTextExtraction,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                // Text Extraction Cards (Expressive, integrated descriptions without clumsy toggle)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    OcrModeCard(
                        selected = !useOcr,
                        title = strings.configImageOnly,
                        subtitle = strings.configImageSubtitle,
                        description = strings.configImageOnlyDescription,
                        icon = Icons.Default.Image,
                        onClick = { useOcr = false },
                        index = 0,
                        totalCount = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OcrModeCard(
                        selected = useOcr,
                        title = strings.configLocalOcr,
                        subtitle = strings.configOcrSubtitle,
                        description = strings.configLocalOcrDescription,
                        icon = Icons.Default.TextFields,
                        onClick = { useOcr = true },
                        index = 1,
                        totalCount = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
