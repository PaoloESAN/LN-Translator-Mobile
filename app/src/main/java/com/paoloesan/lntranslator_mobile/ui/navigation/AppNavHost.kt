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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.paoloesan.lntranslator_mobile.ui.home.HomeScreen
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptScreen
import com.paoloesan.lntranslator_mobile.ui.settings.SettingsScreen
import com.paoloesan.lntranslator_mobile.ui.settings.TranslationConfigScreen
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
    val slideSpec = tween<IntOffset>(durationMillis = 400, easing = FastOutSlowInEasing)
    val tabFadeSpec = tween<Float>(durationMillis = 150)
    val rutasPrincipales = listOf("inicio", "novels", "ajustes")

    NavHost(
        navController = navController,
        startDestination = "inicio",
        enterTransition = {
            val isTabTransition = initialState.destination.route in rutasPrincipales &&
                    targetState.destination.route in rutasPrincipales
            if (isTabTransition) {
                fadeIn(animationSpec = tabFadeSpec)
            } else if (targetState.destination.route !in rutasPrincipales) {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = slideSpec)
            } else {
                fadeIn(animationSpec = tabFadeSpec)
            }
        },
        exitTransition = {
            val isTabTransition = initialState.destination.route in rutasPrincipales &&
                    targetState.destination.route in rutasPrincipales
            if (isTabTransition) {
                fadeOut(animationSpec = tabFadeSpec)
            } else if (targetState.destination.route !in rutasPrincipales) {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = slideSpec) +
                        fadeOut(animationSpec = tween(400))
            } else {
                fadeOut(animationSpec = tabFadeSpec)
            }
        },
        popEnterTransition = {
            val isTabTransition = initialState.destination.route in rutasPrincipales &&
                    targetState.destination.route in rutasPrincipales
            if (isTabTransition) {
                fadeIn(animationSpec = tabFadeSpec)
            } else if (initialState.destination.route !in rutasPrincipales) {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = slideSpec) +
                        fadeIn(animationSpec = tween(400))
            } else {
                fadeIn(animationSpec = tabFadeSpec)
            }
        },
        popExitTransition = {
            val isTabTransition = initialState.destination.route in rutasPrincipales &&
                    targetState.destination.route in rutasPrincipales
            if (isTabTransition) {
                fadeOut(animationSpec = tabFadeSpec)
            } else if (initialState.destination.route !in rutasPrincipales) {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = slideSpec)
            } else {
                fadeOut(animationSpec = tabFadeSpec)
            }
        },
        modifier = Modifier.padding(innerPadding)
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
                }
            )
        }
        composable(route = "config_traduccion") {
            TranslationConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "prompts") {
            PromptScreen(
                contexto
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