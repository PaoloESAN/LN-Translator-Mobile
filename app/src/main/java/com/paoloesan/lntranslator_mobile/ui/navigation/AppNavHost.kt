package com.paoloesan.lntranslator_mobile.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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


@Composable
fun AppNavHost(
    navController: NavHostController,
    contexto: AppCompatActivity,
    innerPadding: PaddingValues
) {
    val navAnimationSpec = tween<IntOffset>(durationMillis = 360, easing = FastOutSlowInEasing)
    val fadeAnimationSpec = tween<Float>(durationMillis = 220)

    NavHost(
        navController = navController,
        startDestination = "inicio",
        enterTransition = {
            fadeIn(animationSpec = fadeAnimationSpec) +
                    slideInVertically(
                        animationSpec = navAnimationSpec,
                        initialOffsetY = { 56 }
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = fadeAnimationSpec)
        },
        popEnterTransition = {
            fadeIn(animationSpec = fadeAnimationSpec) +
                    slideInVertically(
                        animationSpec = navAnimationSpec,
                        initialOffsetY = { 56 }
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = fadeAnimationSpec)
        },
        modifier = Modifier.padding(
            innerPadding
        )
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
            SettingsScreen()
        }
        composable(
            route = "prompts",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = navAnimationSpec
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = fadeAnimationSpec) +
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = navAnimationSpec
                        )
            },
            exitTransition = { fadeOut(animationSpec = fadeAnimationSpec) }
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