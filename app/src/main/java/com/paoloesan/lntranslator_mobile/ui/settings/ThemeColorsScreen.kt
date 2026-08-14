package com.paoloesan.lntranslator_mobile.ui.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.components.CollapsingHeaderScaffold
import com.paoloesan.lntranslator_mobile.ui.theme.AppThemeColorList
import com.paoloesan.lntranslator_mobile.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeColorsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val topBarVisible = LocalTopAppBarVisible.current

    var temaActual by remember {
        mutableStateOf(
            AppThemeMode.fromKey(DataStoreManager.getString(context, "tema_app", AppThemeMode.SYSTEM.key))
        )
    }

    var dynamicColorEnabled by remember {
        mutableStateOf(
            DataStoreManager.getBoolean(context, "dynamic_color", true)
        )
    }

    var selectedColorId by remember {
        mutableStateOf(
            DataStoreManager.getString(context, "app_color_seed", "blue") ?: "blue"
        )
    }

    val isDynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isAppColorDisabled = dynamicColorEnabled && isDynamicAvailable

    DisposableEffect(Unit) {
        topBarVisible.value = false
        onDispose {
            topBarVisible.value = true
        }
    }

    fun aplicarTema(mode: AppThemeMode) {
        temaActual = mode
        DataStoreManager.putStringSync(context, "tema_app", mode.key)
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }

    fun toggleDynamicColor(enabled: Boolean) {
        dynamicColorEnabled = enabled
        DataStoreManager.putBooleanSync(context, "dynamic_color", enabled)
    }

    fun seleccionarColor(colorId: String) {
        selectedColorId = colorId
        DataStoreManager.putStringSync(context, "app_color_seed", colorId)
        if (dynamicColorEnabled) {
            dynamicColorEnabled = false
            DataStoreManager.putBooleanSync(context, "dynamic_color", false)
        }
    }

    CollapsingHeaderScaffold(
        title = strings.settingsThemeTitle,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Card 1: Theme Mode (topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Moon Icon
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    // Column: Theme Mode Title, Subtitle, and ButtonGroup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.themeModeTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.themeModeSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // M3 Expressive ButtonGroup with connected spacing and interactive animateWidth
                        val themeModes = AppThemeMode.entries
                        val interactionSources =
                            remember { List(themeModes.size) { MutableInteractionSource() } }

                        ButtonGroup(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            expandedRatio = 1f,
                            overflowIndicator = { menuState ->
                                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                            },
                        ) {
                            themeModes.forEachIndexed { index, mode ->
                                val isChecked = temaActual == mode
                                val label = mode.getDisplayName(strings)

                                customItem(
                                    buttonGroupContent = {
                                        ToggleButton(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    aplicarTema(mode)
                                                }
                                            },
                                            shapes = when (index) {
                                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                themeModes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            },
                                            interactionSource = interactionSources[index],
                                            modifier = Modifier
                                                .weight(1f)
                                                .animateWidth(
                                                    interactionSource = interactionSources[index],
                                                ),
                                        ) {
                                            Text(
                                                text = label,
                                                softWrap = false,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                            )
                                        }
                                    },
                                    menuContent = {
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = { aplicarTema(mode) },
                                            interactionSource = interactionSources[index],
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Dynamic Colors & App Color (topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Part A: Dynamic Colors (Android 12+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.dynamicColorsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.dynamicColorsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = dynamicColorEnabled && isDynamicAvailable,
                            enabled = isDynamicAvailable,
                            onCheckedChange = { toggleDynamicColor(it) },
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )

                    // Part B: App Color (Seed palette) - Disabled/grayed out when Dynamic Colors is active
                    val appColorAlpha by animateFloatAsState(
                        targetValue = if (isAppColorDisabled) 0.38f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "appColorAlpha",
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(appColorAlpha),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.appColorTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = strings.appColorSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Circular Color Palette Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppThemeColorList.forEach { appColor ->
                                val isSelected =
                                    selectedColorId == appColor.id && !isAppColorDisabled

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAppColorDisabled)
                                                appColor.seedColor.copy(alpha = 0.4f)
                                            else
                                                appColor.seedColor
                                        )
                                        .clickable(
                                            enabled = !isAppColorDisabled,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) {
                                            seleccionarColor(appColor.id)
                                        }
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 2.5.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
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
