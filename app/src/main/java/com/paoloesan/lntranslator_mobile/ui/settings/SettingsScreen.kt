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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.LocalCurrentRoute
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
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
    onNavigateToUpdates: () -> Unit
) {
    val context = LocalContext.current
    val strings: UiStrings = LocalStrings.current

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val currentRoute = LocalCurrentRoute.current

    LaunchedEffect(currentRoute) {
        if (currentRoute == "ajustes") {
            topBarVisible.value = true
            topBarTitle.value = { Text(strings.navSettings) }
            topBarActions.value = {}
            topBarNavIcon.value = {}
            topBarColors.value = null
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

    val secciones = remember(context, strings) {
        listOf(
            SeccionTema(
                Icons.Outlined.BrightnessMedium,
                strings.settingsThemeTitle,
                strings.settingsThemeDescription,
                context
            ),
            SeccionIdioma(
                Icons.Outlined.Translate,
                strings.settingsLanguageTitle,
                strings.settingsLanguageDescription,
                context
            )
        )
    }

    var seccionSeleccionada by remember { mutableStateOf<Seccion?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SeccionRow(
            Icons.Outlined.AutoAwesome,
            strings.settingsProviderTitle,
            strings.settingsProviderDescription
        ) {
            onNavigateToTranslationConfig()
        }

        secciones.forEach { seccionData ->
            SeccionRow(
                icono = seccionData.icono,
                titulo = seccionData.titulo,
                descripcion = seccionData.descripcion
            ) { seccionSeleccionada = seccionData }
        }

        // Dedicated Updates Row
        SeccionRow(
            icono = Icons.Outlined.Download,
            titulo = strings.settingsUpdateTitle,
            descripcion = updateSubtitle,
            mostrarBadge = mostrarUpdateBadge
        ) {
            onNavigateToUpdates()
        }
    }

    if (seccionSeleccionada != null) {
        key(seccionSeleccionada) {
            val cerrarModal: () -> Unit = {
                seccionSeleccionada = null
            }

            BasicAlertDialog(
                onDismissRequest = cerrarModal
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = seccionSeleccionada!!.titulo,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        seccionSeleccionada?.ContenidoModal(cerrarModal)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = cerrarModal) {
                                Text(text = strings.buttonClose)
                            }
                        }
                    }
                }
            }
        }
    }
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
            .padding(12.dp),
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
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialShapes.Cookie12Sided.toShape()
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
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialShapes.Cookie12Sided.toShape()
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
            horizontalAlignment = Alignment.Start
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
