package com.listo.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.listo.app.ui.screens.spending.SpendingScreen
import com.listo.app.ui.screens.splash.SplashScreen
import com.listo.app.ui.screens.stores.StoreMapEditorScreen
import com.listo.app.ui.screens.stores.StoreShoppingScreen
import com.listo.app.ui.screens.stores.StoresScreen
import com.listo.app.ui.viewmodel.StoreArgs

@Composable
fun LisToNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // No content insets here: screens keep handling the status bar themselves. The
    // padding is just the bar's height, consumed so tab screens don't add the
    // navigation-bar inset a second time.
    Scaffold(
        bottomBar = {
            if (BottomTab.showsBar(currentRoute)) BottomNavBar(navController, currentRoute)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LisToNavHost(
            navController = navController,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        )
    }
}

private const val TAB_FADE_MS = 200

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    BottomTab.showsBar(initialState.destination.route) && BottomTab.showsBar(targetState.destination.route)

@Composable
private fun LisToNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        // Tabs cross-fade; screens opened on top of a tab slide in from the side.
        enterTransition = {
            if (isTabSwitch()) fadeIn(tween(TAB_FADE_MS))
            else slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
        },
        exitTransition = {
            if (isTabSwitch()) fadeOut(tween(TAB_FADE_MS))
            else slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(tween(300))
        },
        popEnterTransition = {
            if (isTabSwitch()) fadeIn(tween(TAB_FADE_MS))
            else slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn(tween(300))
        },
        popExitTransition = {
            if (isTabSwitch()) fadeOut(tween(TAB_FADE_MS))
            else slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
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

        composable(Screen.Spending.route) {
            SpendingScreen(navController = navController)
        }

        composable(
            route = Screen.Stores.route,
            arguments = listOf(navArgument(StoreArgs.LIST_ID) {
                type = NavType.LongType
                defaultValue = StoreArgs.NO_LIST
            })
        ) { entry ->
            StoresScreen(
                navController = navController,
                listId = entry.arguments?.getLong(StoreArgs.LIST_ID) ?: StoreArgs.NO_LIST
            )
        }

        composable(
            route = Screen.StoreMapEditor.route,
            arguments = listOf(navArgument(StoreArgs.STORE_ID) { type = NavType.LongType })
        ) {
            StoreMapEditorScreen(navController = navController)
        }

        composable(
            route = Screen.StoreShopping.route,
            arguments = listOf(
                navArgument(StoreArgs.LIST_ID) { type = NavType.LongType },
                navArgument(StoreArgs.STORE_ID) { type = NavType.LongType }
            )
        ) {
            StoreShoppingScreen(navController = navController)
        }
    }
}
