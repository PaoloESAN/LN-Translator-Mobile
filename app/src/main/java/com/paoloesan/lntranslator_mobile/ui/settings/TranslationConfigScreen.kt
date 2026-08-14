package com.paoloesan.lntranslator_mobile.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.components.CollapsingHeaderScaffold

private data class ModelInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val pricing: String,
    val tag: String,
)

@Composable
private fun ConfigCategoryLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(top = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun OcrModeCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ocrCardColor",
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ocrBorderColor",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
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
    val topBarVisible = LocalTopAppBarVisible.current

    val initialProviderId = remember {
        DataStoreManager.getString(context, "active_translation_provider", "gemini_35_flash_lite")
            ?: "gemini_35_flash_lite"
    }
    var useOcr by remember { mutableStateOf(initialProviderId.startsWith("ocr_")) }
    var selectedModelId by remember {
        val rawId = initialProviderId.removePrefix("ocr_").let {
            if (it == "gemini_36_flash") "gemini_37_flash" else it
        }
        val validIds = listOf("gemini_31_flash", "gemini_35_flash_lite", "gemini_37_flash")
        mutableStateOf(if (rawId in validIds) rawId else "gemini_35_flash_lite")
    }

    val focusManager = LocalFocusManager.current

    val apiKeys = remember { mutableStateListOf<String>() }
    val maxApiKeys = 5

    val availableModels = remember(strings) {
        listOf(
            ModelInfo(
                id = "gemini_37_flash",
                name = "Gemini 3.7 Flash",
                version = "gemini-3.7-flash",
                description = strings.configGemini37FlashDesc,
                pricing = strings.configGemini37FlashPricing,
                tag = "3.7 Flash",
            ),
            ModelInfo(
                id = "gemini_35_flash_lite",
                name = "Gemini 3.5 Flash Lite",
                version = "gemini-3.5-flash-lite",
                description = strings.configGemini35FlashLiteDesc,
                pricing = strings.configGemini35FlashLitePricing,
                tag = "3.5 Flash Lite",
            ),
            ModelInfo(
                id = "gemini_31_flash",
                name = "Gemini 3.1 Flash Lite",
                version = "gemini-3.1-flash-lite",
                description = strings.configGemini31FlashLiteDesc,
                pricing = strings.configGemini31FlashLitePricing,
                tag = "3.1 Flash Lite",
            ),
        )
    }

    val currentIndex = remember(selectedModelId) {
        availableModels.indexOfFirst { it.id == selectedModelId }.coerceAtLeast(0)
    }
    val currentModel = availableModels[currentIndex]

    DisposableEffect(Unit) {
        topBarVisible.value = false
        onDispose {
            topBarVisible.value = true
        }
    }

    LaunchedEffect(Unit) {
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

    LaunchedEffect(useOcr, selectedModelId) {
        val finalProviderId = if (useOcr) "ocr_$selectedModelId" else selectedModelId
        DataStoreManager.putStringSync(context, "active_translation_provider", finalProviderId)
    }

    fun saveApiKeys() {
        val validKeys = apiKeys.filter { it.isNotBlank() }
        DataStoreManager.putStringSync(context, "api_keys_list", Gson().toJson(validKeys))
        DataStoreManager.putIntSync(context, "api_key_index", 0)
    }

    CollapsingHeaderScaffold(
        title = strings.topbarTranslationConfig,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        // Section 1: API Keys
        Column(modifier = Modifier.fillMaxWidth()) {
            ConfigCategoryLabel(text = strings.settingsApikeyTitle)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Header row inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.settingsApikeyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.configSectionApiKeysSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { apiKeys.add(""); saveApiKeys() },
                            enabled = apiKeys.size < maxApiKeys,
                            modifier = Modifier
                                .size(width = 46.dp, height = ButtonDefaults.ExtraSmallContainerHeight),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.keyAddButton,
                                modifier = Modifier.size(ButtonDefaults.SmallIconSize),
                            )
                        }
                    }

                    // Key fields
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
                    }

                    // Inner Info Card (Card in Card)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            Text(
                                text = strings.keyRotationInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Advanced (AI Model & Text Extraction grouped cards)
        Column(modifier = Modifier.fillMaxWidth()) {
            ConfigCategoryLabel(text = strings.configAdvancedTitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Card 1: AI Model (topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp,
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Title & Subtitle inside card
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.configAiModelTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.configSectionModelSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Inner cards for model options
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableModels.forEach { model ->
                                val isSelected = model.id == selectedModelId

                                val containerColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "modelItemColor",
                                )

                                val borderColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "modelBorderColor",
                                )

                                Surface(
                                    onClick = { selectedModelId = model.id },
                                    shape = RoundedCornerShape(16.dp),
                                    color = containerColor,
                                    border = BorderStroke(1.dp, borderColor),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 14.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = model.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = model.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pricing reference inner card (no border)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = currentModel.pricing,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Card 2: Text Extraction (topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Title & Subtitle inside card
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.configTextExtraction,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.configSectionOcrSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Inner cards for OCR modes
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OcrModeCard(
                                selected = !useOcr,
                                title = strings.configImageOnly,
                                subtitle = strings.configImageSubtitle,
                                description = strings.configImageOnlyDescription,
                                onClick = { useOcr = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OcrModeCard(
                                selected = useOcr,
                                title = strings.configLocalOcr,
                                subtitle = strings.configOcrSubtitle,
                                description = strings.configLocalOcrDescription,
                                onClick = { useOcr = true },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
}
