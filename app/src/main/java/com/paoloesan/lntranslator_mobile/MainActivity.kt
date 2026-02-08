package com.paoloesan.lntranslator_mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paoloesan.lntranslator_mobile.ui.navigation.AppNavHost
import com.paoloesan.lntranslator_mobile.ui.strings.SpanishUiStrings
import com.paoloesan.lntranslator_mobile.ui.strings.StringsProvider
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

// CompositionLocal para acceder a los strings en cualquier parte de la app
val LocalStrings = staticCompositionLocalOf<UiStrings> { SpanishUiStrings }

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configurarTema()
        enableEdgeToEdge()

        setContent {
            val prefs = remember { getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }

            var idiomaActual by remember {
                mutableStateOf(prefs.getString("idioma_app", null))
            }

            DisposableEffect(prefs) {
                val listener =
                    android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                        if (key == "idioma_app") {
                            idiomaActual = p.getString(key, null)
                        }
                    }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val strings = StringsProvider.getStrings(idiomaActual)

            CompositionLocalProvider(LocalStrings provides strings) {
                LNTranslatormobileTheme {
                    val navController = rememberNavController()
                    MainContent(navController, this@MainActivity)
                }
            }
        }
    }

    private fun configurarTema() {
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val tema = prefs.getString("tema_app", "Predeterminado del sistema")

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
    val rutasPrincipales = listOf("inicio", "ajustes")
    val mostrarBottombar = rutaActual in rutasPrincipales
    val suiteType = if (mostrarBottombar) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfo()
        )
    } else {
        NavigationSuiteType.None
    }
    NavigationSuiteScaffold(
        layoutType = suiteType,
        navigationSuiteItems = {
            item(
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
                        Icon(
                            Icons.Rounded.Home,
                            contentDescription = strings.navHome
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = strings.navHome
                        )
                    }
                },
                label = { Text(strings.navHome) }
            )
            item(
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
                    if (rutaActual == "ajustes") {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = strings.navSettings
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = strings.navSettings
                        )
                    }
                },
                label = { Text(strings.navSettings) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        when (rutaActual) {
                            "inicio" -> Text(strings.appName)
                            "ajustes" -> Text(strings.navSettings)
                            "prompts" -> Text(strings.topbarPrompts)
                            else -> Text(strings.appName)
                        }
                    },
                    navigationIcon = {
                        if (rutaActual == "prompts") {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = strings.navBack
                                )
                            }
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavHost(navController, contexto, innerPadding)
        }
    }
}