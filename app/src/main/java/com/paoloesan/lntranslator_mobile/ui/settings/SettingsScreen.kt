package com.paoloesan.lntranslator_mobile.ui.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.LocalCurrentRoute
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarLarge
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToTranslationConfig: () -> Unit,
    onNavigateToThemeColors: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToUpdates: () -> Unit
) {
    val context = LocalContext.current
    val strings: UiStrings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val topBarLarge = LocalTopAppBarLarge.current

    val currentRoute = LocalCurrentRoute.current

    LaunchedEffect(currentRoute) {
        if (currentRoute == "ajustes") {
            topBarVisible.value = true
            topBarLarge.value = false
            topBarTitle.value = {
                Text(
                    text = strings.navSettings,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                )
            }
            topBarActions.value = {}
            topBarNavIcon.value = {}
            topBarColors.value = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            topBarLarge.value = false
        }
    }

    // Observe update info to show subtitle and badge dynamically
    val availableVersion by DataStoreManager.getStringFlow(context, "available_update_version")
        .collectAsState(initial = DataStoreManager.getString(context, "available_update_version"))
    val updateSeen by DataStoreManager.getBooleanFlow(context, "update_seen", true)
        .collectAsState(initial = DataStoreManager.getBoolean(context, "update_seen", true))

    val updateSubtitle = if (availableVersion != null && !updateSeen) {
        strings.updateNewVersionAvailable(availableVersion!!)
    } else {
        strings.settingsUpdateDescription
    }

    val mostrarUpdateBadge = availableVersion != null && !updateSeen

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
            .padding(bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section: Traducción e IA
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsCategoryLabel(text = strings.settingsCategoryServices)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                SeccionRow(
                    icono = Icons.Outlined.AutoAwesome,
                    titulo = strings.settingsProviderTitle,
                    descripcion = strings.settingsProviderDescription,
                    onClick = { onNavigateToTranslationConfig() }
                )
            }
        }

        // Section: Personalización
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsCategoryLabel(text = strings.settingsCategoryPreferences)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SeccionRow(
                        icono = Icons.Outlined.Palette,
                        titulo = strings.settingsThemeTitle,
                        descripcion = strings.settingsThemeDescription,
                        onClick = { onNavigateToThemeColors() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                    )
                    SeccionRow(
                        icono = Icons.Outlined.Translate,
                        titulo = strings.settingsLanguageTitle,
                        descripcion = strings.settingsLanguageDescription,
                        onClick = { onNavigateToLanguage() }
                    )
                }
            }
        }

        // Section: Sistema
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsCategoryLabel(text = strings.settingsCategorySystem)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                SeccionRow(
                    icono = Icons.Outlined.Download,
                    titulo = strings.settingsUpdateTitle,
                    descripcion = updateSubtitle,
                    mostrarBadge = mostrarUpdateBadge,
                    onClick = { onNavigateToUpdates() }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(top = 4.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SeccionRow(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    mostrarBadge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (mostrarBadge) {
            BadgedBox(
                badge = {
                    Badge {
                        Text("!")
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
