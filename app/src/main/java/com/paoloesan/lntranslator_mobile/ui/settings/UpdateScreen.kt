package com.paoloesan.lntranslator_mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarActions
import com.paoloesan.lntranslator_mobile.LocalTopAppBarColors
import com.paoloesan.lntranslator_mobile.LocalTopAppBarLarge
import com.paoloesan.lntranslator_mobile.LocalTopAppBarNavigationIcon
import com.paoloesan.lntranslator_mobile.LocalTopAppBarTitle
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val topBarLarge = LocalTopAppBarLarge.current

    LaunchedEffect(Unit) {
        topBarVisible.value = true
        topBarLarge.value = false
        topBarTitle.value = { Text(strings.settingsUpdateTitle) }
        topBarNavIcon.value = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.navBack
                )
            }
        }
        topBarActions.value = {}
        topBarColors.value = null

        // Mark update as seen when user enters the screen
        DataStoreManager.putBoolean(context, "update_seen", true)
    }

    DisposableEffect(Unit) {
        onDispose {
            topBarLarge.value = false
        }
    }

    // Auto-check updates setting
    val checkUpdatesOnStart by DataStoreManager.getBooleanFlow(
        context,
        "check_updates_on_start",
        true
    )
        .collectAsState(
            initial = DataStoreManager.getBoolean(
                context,
                "check_updates_on_start",
                true
            )
        )

    // Observe UpdateManager states
    val versionActual = UpdateManager.obtenerVersionActual(context)
    val nuevaVersion = UpdateManager.nuevaVersion
    val ultimaTagVersion = UpdateManager.ultimaTagVersion
    val cargando = UpdateManager.cargando
    val errorBusqueda = UpdateManager.errorBusqueda
    val contenidoCambios = UpdateManager.contenidoCambios
    val progresoDescarga = UpdateManager.progresoDescarga
    val descargaCompletada = UpdateManager.descargaCompletada
    val simulandoInstalacion = UpdateManager.simulandoInstalacion

    var mostrarModalCambios by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!UpdateManager.consultaRealizada) {
            UpdateManager.buscarActualizaciones(context, strings)
        } else {
            UpdateManager.verificarArchivoDescargado(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Auto check switch row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        DataStoreManager.putBoolean(
                            context,
                            "check_updates_on_start",
                            !checkUpdatesOnStart
                        )
                    }
                }
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.updateAutoCheckTitle,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = strings.updateAutoCheckDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checkUpdatesOnStart,
                onCheckedChange = { checked ->
                    scope.launch {
                        DataStoreManager.putBoolean(context, "check_updates_on_start", checked)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.updateCurrentVersion(
                        versionActual ?: strings.updateUnknownVersion
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when {
                    cargando -> {
                        Text(
                            text = strings.updateChecking,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    errorBusqueda != null -> {
                        Text(
                            text = errorBusqueda,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    nuevaVersion == null -> {
                        Text(
                            text = strings.updateNoPublishedVersion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    nuevaVersion == versionActual -> {
                        Text(
                            text = strings.updateUpToDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.updateNewVersionAvailable(nuevaVersion),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (progresoDescarga >= 0f) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    LinearWavyProgressIndicator(
                                        progress = { progresoDescarga },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = strings.updateDownloadingProgress((progresoDescarga * 100).toInt()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else if (simulandoInstalacion) {
                                Text(
                                    text = strings.updateInstallingStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = {
                    mostrarModalCambios = true
                    if ((!UpdateManager.consultaRealizada || contenidoCambios == null) && !cargando) {
                        scope.launch { UpdateManager.buscarActualizaciones(context, strings) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = strings.updateViewChangelogButton)
            }

            val updateAvailable = nuevaVersion != null && nuevaVersion != versionActual
            val isDownloadingOrInstalling = progresoDescarga >= 0f || simulandoInstalacion

            Button(
                onClick = {
                    if (updateAvailable) {
                        if (!descargaCompletada) {
                            UpdateManager.iniciarDescarga(context)
                        } else {
                            UpdateManager.iniciarInstalacion(context, strings)
                        }
                    } else {
                        scope.launch { UpdateManager.buscarActualizaciones(context, strings) }
                    }
                },
                enabled = !isDownloadingOrInstalling && !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        updateAvailable -> if (descargaCompletada) strings.updateInstallButton else strings.updateDownloadButton
                        else -> strings.updateCheckButton
                    }
                )
            }
        }
    }

    if (mostrarModalCambios) {
        AlertDialog(
            onDismissRequest = { mostrarModalCambios = false },
            title = {
                Text(ultimaTagVersion ?: strings.updateLatestChangesTitle)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    when {
                        cargando -> Text(strings.updateLatestChangesLoading)
                        contenidoCambios != null -> Text(contenidoCambios)
                        else -> Text(strings.updateLatestChangesEmpty)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { mostrarModalCambios = false }) {
                    Text(strings.buttonClose)
                }
            }
        )
    }
}
