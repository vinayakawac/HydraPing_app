package com.example.hydraping.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hydraping.presentation.screens.HistoryScreen
import com.example.hydraping.presentation.screens.HomeScreen
import com.example.hydraping.presentation.screens.RemindScreen
import com.example.hydraping.presentation.screens.SettingsScreen
import com.example.hydraping.presentation.screens.CreateTargetScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(250)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(250))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(200))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateTarget = {
                    navController.navigate(Screen.CreateTarget.route)
                },
                onNavigateToEditTarget = { targetId ->
                    navController.navigate(Screen.EditTarget.withId(targetId))
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Remind.route) {
            RemindScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.CreateTarget.route) {
            CreateTargetScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditTarget.route,
            arguments = listOf(navArgument("targetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val targetId = backStackEntry.arguments?.getInt("targetId") ?: return@composable
            CreateTargetScreen(
                targetId = targetId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
