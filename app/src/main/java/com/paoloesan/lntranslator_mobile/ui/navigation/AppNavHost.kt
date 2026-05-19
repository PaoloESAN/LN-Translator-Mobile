package com.paoloesan.lntranslator_mobile.ui.navigation

import androidx.appcompat.app.AppCompatActivity
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
    val navAnimationSpec = tween<IntOffset>(durationMillis = 360, easing = FastOutSlowInEasing)
    val fadeAnimationSpec = tween<Float>(durationMillis = 220)

    NavHost(
        navController = navController,
        startDestination = "inicio",
        enterTransition = {
            fadeIn(animationSpec = fadeAnimationSpec) +
                    slideInVertically(
                        animationSpec = navAnimationSpec,
                        initialOffsetY = { it }
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = fadeAnimationSpec)
        },
        popEnterTransition = {
            fadeIn(animationSpec = fadeAnimationSpec) +
                    slideInVertically(
                        animationSpec = navAnimationSpec,
                        initialOffsetY = { it }
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
            SettingsScreen(
                onNavigateToTranslationConfig = {
                    navController.navigate("config_traduccion")
                }
            )
        }
        composable(
            route = "config_traduccion",
            // Slide in from right (like native Google apps)
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 200))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
            }
        ) {
            TranslationConfigScreen(
                onBack = { navController.popBackStack() }
            )
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
        composable(
            route = "novels",
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
            arguments = listOf(navArgument("novelName") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = navAnimationSpec
                )
            },
            exitTransition = {
                fadeOut(animationSpec = fadeAnimationSpec)
            },
            popEnterTransition = {
                fadeIn(animationSpec = fadeAnimationSpec)
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = navAnimationSpec
                )
            }
        ) { backStackEntry ->
            val novelName = backStackEntry.arguments?.getString("novelName") ?: ""
            NovelDetailsScreen(
                novelName = novelName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}