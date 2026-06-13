package com.paoloesan.lntranslator_mobile.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.paoloesan.lntranslator_mobile.ui.home.HomeScreen
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptScreen
import com.paoloesan.lntranslator_mobile.ui.settings.SettingsScreen
import com.paoloesan.lntranslator_mobile.ui.settings.TranslationConfigScreen
import com.paoloesan.lntranslator_mobile.ui.settings.UpdateScreen
import com.paoloesan.lntranslator_mobile.ui.novels.NovelsScreen
import com.paoloesan.lntranslator_mobile.ui.novels.NovelDetailsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavHost(
    navController: NavHostController,
    contexto: AppCompatActivity,
    innerPadding: PaddingValues
) {
    val slideSpec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)
    val tabFadeSpec = tween<Float>(durationMillis = 400) // Increased duration
    val rutasPrincipales = listOf("inicio", "novels", "ajustes")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val isImmersive = rutaActual?.startsWith("novel_details") == true

    NavHost(
        navController = navController,
        startDestination = "inicio",
        enterTransition = {
            val fromMain = initialState.destination.route in rutasPrincipales
            val toMain = targetState.destination.route in rutasPrincipales
            
            if (fromMain && toMain) {
                fadeIn(animationSpec = tabFadeSpec)
            } else if (toMain) {
                // Entering a main route from a sub-route (e.g. going back)
                fadeIn(animationSpec = tabFadeSpec)
            } else {
                // Entering a sub-route (e.g. prompts)
                slideInHorizontally(initialOffsetX = { it }, animationSpec = slideSpec)
            }
        },
        exitTransition = {
            val fromMain = initialState.destination.route in rutasPrincipales
            val toMain = targetState.destination.route in rutasPrincipales

            if (fromMain && toMain) {
                fadeOut(animationSpec = tabFadeSpec)
            } else if (fromMain) {
                // Leaving a main route to a sub-route
                fadeOut(animationSpec = tabFadeSpec)
            } else {
                // Leaving a sub-route
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = slideSpec) +
                        fadeOut(animationSpec = tween(400))
            }
        },
        popEnterTransition = {
            val fromMain = initialState.destination.route in rutasPrincipales
            val toMain = targetState.destination.route in rutasPrincipales

            if (fromMain && toMain) {
                fadeIn(animationSpec = tabFadeSpec)
            } else if (toMain) {
                // Popping back to a main route
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = slideSpec) +
                        fadeIn(animationSpec = tween(400))
            } else {
                fadeIn(animationSpec = tabFadeSpec)
            }
        },
        popExitTransition = {
            val fromMain = initialState.destination.route in rutasPrincipales
            val toMain = targetState.destination.route in rutasPrincipales

            if (fromMain && toMain) {
                fadeOut(animationSpec = tabFadeSpec)
            } else if (fromMain) {
                // Popping out of a main route (unlikely with tab navigation)
                fadeOut(animationSpec = tabFadeSpec)
            } else {
                // Popping back from a sub-route
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = slideSpec)
            }
        },
        modifier = if (isImmersive) Modifier else Modifier.padding(innerPadding)
    ) {
        composable("inicio") {
            HomeScreen(
                navController = navController,
                onNavigateToPrompts = {
                    navController.navigate("prompts") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("ajustes") {
            SettingsScreen(
                onNavigateToTranslationConfig = {
                    navController.navigate("config_traduccion")
                },
                onNavigateToUpdates = {
                    navController.navigate("actualizar")
                }
            )
        }
        composable(route = "actualizar") {
            UpdateScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "config_traduccion") {
            TranslationConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "prompts") {
            PromptScreen(
                context = contexto,
                onBack = { navController.popBackStack() }
            ) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "prompt_seleccionado",
                    it
                )
                navController.popBackStack()
            }
        }
        composable(route = "novels") {
            NovelsScreen(
                onNavigateToDetails = { novelName ->
                    navController.navigate("novel_details/$novelName") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "novel_details/{novelName}",
            arguments = listOf(navArgument("novelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val novelName = backStackEntry.arguments?.getString("novelName") ?: ""
            NovelDetailsScreen(
                novelName = novelName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
