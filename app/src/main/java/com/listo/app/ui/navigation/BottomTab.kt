package com.listo.app.ui.navigation

import androidx.annotation.StringRes
import com.listo.app.R

/** The top-level screens reachable from the bottom navigation bar, in display order. */
enum class BottomTab(val route: String, @StringRes val labelRes: Int) {
    Lists(Screen.Home.route, R.string.nav_lists),
    Categories(Screen.Categories.route, R.string.nav_categories),
    Calendar(Screen.Calendar.route, R.string.nav_calendar),
    History(Screen.History.route, R.string.nav_history),
    Settings(Screen.Settings.route, R.string.nav_settings);

    companion object {
        fun forRoute(route: String?): BottomTab? = entries.firstOrNull { it.route == route }

        /** The bar shows on top-level screens only, never on details or start-up. */
        fun showsBar(route: String?): Boolean = forRoute(route) != null
    }
}
