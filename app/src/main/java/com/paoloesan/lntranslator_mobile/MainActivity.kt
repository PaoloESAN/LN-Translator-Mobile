package com.paoloesan.lntranslator_mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.navigation.AppNavHost
import com.paoloesan.lntranslator_mobile.ui.settings.UpdateManager
import com.paoloesan.lntranslator_mobile.ui.strings.SpanishUiStrings
import com.paoloesan.lntranslator_mobile.ui.strings.StringsProvider
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

// CompositionLocal para acceder a los strings en cualquier parte de la app
val LocalStrings = staticCompositionLocalOf<UiStrings> { SpanishUiStrings }

val LocalCurrentRoute = staticCompositionLocalOf<String?> { null }

// New CompositionLocals for TopAppBar control
val LocalTopAppBarActions =
    staticCompositionLocalOf<MutableState<@Composable RowScope.() -> Unit>> {
        error("No TopAppBarActions provider")
    }
val LocalTopAppBarTitle = staticCompositionLocalOf<MutableState<@Composable () -> Unit>> {
    error("No TopAppBarTitle provider")
}
val LocalTopAppBarNavigationIcon = staticCompositionLocalOf<MutableState<@Composable () -> Unit>> {
    error("No TopAppBarNavigationIcon provider")
}
val LocalTopAppBarColors = staticCompositionLocalOf<MutableState<TopAppBarColors?>> {
    error("No TopAppBarColors provider")
}
val LocalTopAppBarVisible = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No TopAppBarVisible provider")
}
val LocalTopAppBarLarge = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No TopAppBarLarge provider")
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configurarTema()
        enableEdgeToEdge()

        setContent {
            val idiomaActual by DataStoreManager.getStringFlow(this, "idioma_app")
                .collectAsState(initial = DataStoreManager.getString(this, "idioma_app"))

            val strings = StringsProvider.getStrings(idiomaActual)

            val topBarTitle = remember { mutableStateOf<@Composable () -> Unit>({}) }
            val topBarActions = remember { mutableStateOf<@Composable RowScope.() -> Unit>({}) }
            val topBarNavIcon = remember { mutableStateOf<@Composable () -> Unit>({}) }
            val topBarColors = remember { mutableStateOf<TopAppBarColors?>(null) }
            val topBarVisible = remember { mutableStateOf(true) }
            val topBarLarge = remember { mutableStateOf(false) }

            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalTopAppBarTitle provides topBarTitle,
                LocalTopAppBarActions provides topBarActions,
                LocalTopAppBarNavigationIcon provides topBarNavIcon,
                LocalTopAppBarColors provides topBarColors,
                LocalTopAppBarVisible provides topBarVisible,
                LocalTopAppBarLarge provides topBarLarge
            ) {
                LNTranslatormobileTheme {
                    val navController = rememberNavController()
                    LaunchedEffect(Unit) {
                        val checkUpdates = DataStoreManager.getBoolean(
                            this@MainActivity,
                            "check_updates_on_start",
                            true
                        )
                        if (checkUpdates) {
                            UpdateManager.comprobarActualizacionesSilenciosamente(this@MainActivity)
                        }
                    }
                    MainContent(navController, this@MainActivity)
                }
            }
        }
    }

    private fun configurarTema() {
        val tema = DataStoreManager.getString(this, "tema_app", "Predeterminado del sistema")

        val modo = when (tema) {
            "Claro" -> AppCompatDelegate.MODE_NIGHT_NO
            "Oscuro" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(modo)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(navController: NavHostController, contexto: AppCompatActivity) {
    val strings = LocalStrings.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val rutasPrincipales = listOf("inicio", "novels", "ajustes")
    val mostrarBottombar = rutaActual in rutasPrincipales

    val availableVersion by DataStoreManager.getStringFlow(contexto, "available_update_version")
        .collectAsState(initial = DataStoreManager.getString(contexto, "available_update_version"))
    val updateSeen by DataStoreManager.getBooleanFlow(contexto, "update_seen", true)
        .collectAsState(initial = DataStoreManager.getBoolean(contexto, "update_seen", true))
    val mostrarBadge = availableVersion != null && !updateSeen

    val topBarTitle = LocalTopAppBarTitle.current
    val topBarActions = LocalTopAppBarActions.current
    val topBarNavIcon = LocalTopAppBarNavigationIcon.current
    val topBarColors = LocalTopAppBarColors.current
    val topBarVisible = LocalTopAppBarVisible.current
    val topBarLarge = LocalTopAppBarLarge.current

    Scaffold(
        topBar = {
            if (topBarVisible.value) {
                if (topBarLarge.value) {
                    LargeTopAppBar(
                        title = topBarTitle.value,
                        navigationIcon = topBarNavIcon.value,
                        actions = topBarActions.value,
                        colors = topBarColors.value ?: TopAppBarDefaults.topAppBarColors()
                    )
                } else {
                    TopAppBar(
                        title = topBarTitle.value,
                        navigationIcon = topBarNavIcon.value,
                        actions = topBarActions.value,
                        colors = topBarColors.value ?: TopAppBarDefaults.topAppBarColors()
                    )
                }
            }
        },
        bottomBar = {
            if (mostrarBottombar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = rutaActual == "inicio",
                        onClick = {
                            if (rutaActual != "inicio") {
                                navController.navigate("inicio") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (rutaActual == "inicio") {
                                Icon(Icons.Rounded.Home, contentDescription = strings.navHome)
                            } else {
                                Icon(Icons.Outlined.Home, contentDescription = strings.navHome)
                            }
                        },
                        label = { Text(strings.navHome) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors()
                    )
                    NavigationBarItem(
                        selected = rutaActual == "novels",
                        onClick = {
                            if (rutaActual != "novels") {
                                navController.navigate("novels") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (rutaActual == "novels") {
                                Icon(Icons.Rounded.Book, contentDescription = strings.novelsTitle)
                            } else {
                                Icon(Icons.Outlined.Book, contentDescription = strings.novelsTitle)
                            }
                        },
                        label = { Text(strings.novelsTitle) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors()
                    )
                    NavigationBarItem(
                        selected = rutaActual == "ajustes",
                        onClick = {
                            if (rutaActual != "ajustes") {
                                navController.navigate("ajustes") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            val settingsIcon = if (rutaActual == "ajustes") {
                                Icons.Rounded.Settings
                            } else {
                                Icons.Outlined.Settings
                            }
                            if (mostrarBadge) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("!")
                                        }
                                    }
                                ) {
                                    Icon(
                                        settingsIcon,
                                        contentDescription = strings.navSettings
                                    )
                                }
                            } else {
                                Icon(
                                    settingsIcon,
                                    contentDescription = strings.navSettings
                                )
                            }
                        },
                        label = { Text(strings.navSettings) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors()
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalCurrentRoute provides rutaActual
        ) {
            AppNavHost(navController, contexto, innerPadding)
        }
    }
}
