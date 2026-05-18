package com.skincoach.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skincoach.app.ui.screens.capture.CaptureScreen
import com.skincoach.app.ui.screens.history.HistoryScreen
import com.skincoach.app.ui.screens.home.HomeScreen
import com.skincoach.app.ui.screens.result.ResultScreen

@Composable
fun SkinCoachNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(tween(320)) { it / 12 }
        },
        exitTransition = { fadeOut(tween(220)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = {
            fadeOut(tween(240)) + slideOutHorizontally(tween(320)) { it / 12 }
        },
    ) {
        composable(Destinations.HOME) {
            HomeScreen(
                onScanClick = { navController.navigate(Destinations.CAPTURE) },
                onHistoryClick = { navController.navigate(Destinations.HISTORY) },
            )
        }
        composable(Destinations.CAPTURE) {
            CaptureScreen(
                onClose = { navController.popBackStack() },
                onCaptured = { photoPath ->
                    navController.navigate(Destinations.result(photoPath)) {
                        popUpTo(Destinations.HOME)
                    }
                },
            )
        }
        composable(Destinations.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Destinations.RESULT_ROUTE,
            arguments = listOf(
                navArgument(Destinations.RESULT_ARG_PHOTO) { type = NavType.StringType },
            ),
        ) { entry ->
            ResultScreen(
                photoPath = entry.arguments?.getString(Destinations.RESULT_ARG_PHOTO),
                onRescan = {
                    navController.navigate(Destinations.CAPTURE) {
                        popUpTo(Destinations.HOME)
                    }
                },
                onDone = {
                    navController.popBackStack(Destinations.HOME, inclusive = false)
                },
            )
        }
    }
}
