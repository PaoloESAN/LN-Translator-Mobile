package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.translation.providers.ProviderFactory

// ---------------------------------------------------------------------------
// Category label – same primary-color style as Google Settings
// ---------------------------------------------------------------------------
@Composable
private fun CategoryLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

// ---------------------------------------------------------------------------
// OCR mode card – previous design with shape morph animation
// ---------------------------------------------------------------------------
@Composable
private fun OcrModeCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ocrCardColor",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 28.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "ocrCardCorner",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.extraLarge.copy(
            topStart = androidx.compose.foundation.shape.CornerSize(cornerRadius),
            topEnd = androidx.compose.foundation.shape.CornerSize(cornerRadius),
            bottomStart = androidx.compose.foundation.shape.CornerSize(cornerRadius),
            bottomEnd = androidx.compose.foundation.shape.CornerSize(cornerRadius),
        ),
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


// ---------------------------------------------------------------------------
// Main screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TranslationConfigScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    val providerFactory = remember { ProviderFactory(context) }

    // --- State ---
    val initialProviderId = remember {
        prefs.getString("active_translation_provider", "gemini_3_flash") ?: "gemini_3_flash"
    }
    var useOcr by remember { mutableStateOf(initialProviderId.startsWith("ocr_")) }
    var selectedModelId by remember { mutableStateOf(initialProviderId.removePrefix("ocr_")) }
    var showModelMenu by remember { mutableStateOf(false) }

    val apiKeys = remember { mutableStateListOf<String>() }
    val maxApiKeys = 5

    val models = remember {
        providerFactory.getAllProviders()
            .filter { !it.providerId.startsWith("ocr_") }
            .map { it.providerId to it.displayName }
    }

    val selectedModelName = remember(selectedModelId, models) {
        models.firstOrNull { it.first == selectedModelId }?.second ?: selectedModelId
    }

    LaunchedEffect(Unit) {
        val jsonString = prefs.getString("api_keys_list", null)
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
        prefs.edit { putString("active_translation_provider", finalProviderId) }
    }

    fun saveApiKeys() {
        val validKeys = apiKeys.filter { it.isNotBlank() }
        prefs.edit {
            putString("api_keys_list", Gson().toJson(validKeys))
            putInt("api_key_index", 0)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(strings.topbarTranslationConfig) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.navBack,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 64.dp),
        ) {

            // ── API Keys ─────────────────────────────────────────────────────
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
                if (apiKeys.size < maxApiKeys) {
                    FilledTonalIconButton(
                        onClick = { apiKeys.add(""); saveApiKeys() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.keyAddButton,
                            modifier = Modifier.size(18.dp),
                        )
                    }
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
                            shape = MaterialTheme.shapes.extraLarge,
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

                // Hint card
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

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // ── Modelo de IA ─────────────────────────────────────────────────
            CategoryLabel(
                text = strings.configAiModelTitle,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            // Simple row – clicking anchors a DropdownMenu right below
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showModelMenu = true }
                        .padding(vertical = 14.dp),
                ) {
                    Text(
                        text = strings.configActiveModel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = selectedModelName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                DropdownMenuPopup(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(0, 1)
                    ) {
                        val totalItems = models.size
                        models.forEachIndexed { index, (modelId, displayName) ->
                            val isSelected = modelId == selectedModelId
                            DropdownMenuItem(
                                selected = isSelected,
                                onClick = {
                                    selectedModelId = modelId
                                    showModelMenu = false
                                },
                                text = { Text(displayName) },
                                shapes = MenuDefaults.itemShape(index, totalItems),
                                selectedLeadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                leadingIcon = {
                                    // Keep text aligned even when no checkmark
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
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // ── Extracción de Texto (OCR) ─────────────────────────────
            CategoryLabel(
                text = strings.configTextExtraction,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            val visionWeight by animateFloatAsState(
                targetValue = if (!useOcr) 1.25f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "visionWeight",
            )
            val ocrWeight by animateFloatAsState(
                targetValue = if (useOcr) 1.25f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "ocrWeight",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OcrModeCard(
                    selected = !useOcr,
                    title = strings.configVisionOnly,
                    subtitle = strings.configVisionSubtitle,
                    onClick = { useOcr = false },
                    modifier = Modifier.weight(visionWeight),
                )
                OcrModeCard(
                    selected = useOcr,
                    title = strings.configLocalOcr,
                    subtitle = strings.configOcrSubtitle,
                    onClick = { useOcr = true },
                    modifier = Modifier.weight(ocrWeight),
                )
            }
        }
    }
}
