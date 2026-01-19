package com.paoloesan.lntranslator_mobile.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.paoloesan.lntranslator_mobile.ui.home.HomeScreen
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptScreen
import com.paoloesan.lntranslator_mobile.ui.settings.SettingsScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    contexto: AppCompatActivity,
    innerPadding: PaddingValues
) {
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
            HomeScreen(
                navController = navController,
                onNavigateToPrompts = { navController.navigate("prompts") })
        }
        composable("ajustes") {
            SettingsScreen()
        }
        composable(
            route = "prompts",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(300)
                        )
            },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
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
    }
}