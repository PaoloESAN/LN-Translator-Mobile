package com.paoloesan.lntranslator_mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paoloesan.lntranslator_mobile.ui.home.HomeScreen
import com.paoloesan.lntranslator_mobile.ui.home.PromptScreen
import com.paoloesan.lntranslator_mobile.ui.settings.SettingsScreen
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val tema = prefs.getString("tema_app", "Predeterminado del sistema")

        val modo = when (tema) {
            "Claro" -> AppCompatDelegate.MODE_NIGHT_NO
            "Oscuro" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(modo)
        enableEdgeToEdge()
        setContent {
            LNTranslatormobileTheme {
                val navController = rememberNavController()
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
                                        contentDescription = "Inicio"
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Home,
                                        contentDescription = "Inicio"
                                    )
                                }
                            },
                            label = { Text("Inicio") }
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
                                        contentDescription = null
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Settings,
                                        contentDescription = null
                                    )
                                }
                            },
                            label = { Text("Ajustes") }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    when (rutaActual) {
                                        "inicio" -> Text("LN Translator")
                                        "ajustes" -> Text("Ajustes")
                                        "prompts" -> Text("Prompts guardados")
                                        else -> Text("LN Translator")
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "inicio",
                            enterTransition = {
                                fadeIn(animationSpec = tween(300)) +
                                        slideInVertically(
                                            animationSpec = tween(300),
                                            initialOffsetY = { 40 })
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(300)) +
                                        slideInVertically(
                                            animationSpec = tween(300),
                                            initialOffsetY = { 40 })
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(300))
                            },
                            modifier = Modifier.padding(
                                innerPadding
                            )
                        ) {
                            composable("inicio") {
                                HomeScreen(onNavigateToPrompts = { navController.navigate("prompts") })
                            }
                            composable("ajustes") {
                                SettingsScreen()
                            }
                            composable(
                                route = "prompts",
                                enterTransition = {
                                    slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(500)
                                    )
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(500)) +
                                            slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(500)
                                            )
                                },
                                exitTransition = { fadeOut(animationSpec = tween(300)) }
                            ) {
                                PromptScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}