package com.listo.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.listo.app.ui.screens.categories.CategoriesScreen
import com.listo.app.ui.screens.createtask.CreateTaskScreen
import com.listo.app.ui.screens.home.HomeScreen
import com.listo.app.ui.screens.onboarding.OnboardingScreen
import com.listo.app.ui.screens.premium.PremiumScreen
import com.listo.app.ui.screens.settings.SettingsScreen
import com.listo.app.ui.screens.splash.SplashScreen
import com.listo.app.ui.screens.statistics.StatisticsScreen
import com.listo.app.ui.screens.taskdetail.TaskDetailScreen

@Composable
fun LisToNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId")?.takeIf { it != -1L }
            CreateTaskScreen(navController = navController, taskId = taskId)
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType }
            )
        ) {
            TaskDetailScreen(navController = navController)
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(navController = navController)
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.Premium.route) {
            PremiumScreen(navController = navController)
        }
    }
}
