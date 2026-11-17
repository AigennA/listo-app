package com.listo.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController

private val BottomTab.icon: ImageVector
    get() = when (this) {
        BottomTab.Lists -> Icons.Rounded.ListAlt
        BottomTab.Categories -> Icons.Rounded.Category
        BottomTab.Calendar -> Icons.Rounded.CalendarMonth
        BottomTab.History -> Icons.Rounded.History
        BottomTab.Settings -> Icons.Rounded.Settings
    }

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab.route == currentRoute,
                onClick = { navController.navigateToTab(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = {
                    // labelSmall so the longest label ("Inställningar") fits five tabs on a phone.
                    Text(
                        stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                )
            )
        }
    }
}

/**
 * Switches tab the standard way: one copy of each tab on the back stack, each
 * tab keeps its scroll state, and Back from any tab returns to Lists.
 */
private fun NavController.navigateToTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
