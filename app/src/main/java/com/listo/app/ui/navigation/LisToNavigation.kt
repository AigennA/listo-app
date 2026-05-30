package com.listo.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.listo.app.ui.screens.calendar.CalendarScreen
import com.listo.app.ui.screens.categories.CategoriesScreen
import com.listo.app.ui.screens.categoryitems.CategoryItemsScreen
import com.listo.app.ui.screens.history.HistoryScreen
import com.listo.app.ui.screens.listdetail.ListDetailScreen
import com.listo.app.ui.screens.lists.ListsScreen
import com.listo.app.ui.screens.onboarding.OnboardingScreen
import com.listo.app.ui.screens.settings.SettingsScreen
import com.listo.app.ui.screens.splash.SplashScreen

@Composable
fun LisToNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            ListsScreen(navController = navController)
        }

        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) {
            ListDetailScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(navController = navController)
        }

        composable(
            route = Screen.CategoryItems.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) {
            CategoryItemsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
