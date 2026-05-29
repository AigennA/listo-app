package com.listo.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.BuildConfig
import com.listo.app.ui.components.LisToTopBar
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.*
import com.listo.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { LisToTopBar(title = "Settings", navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("Appearance")

            SettingsToggleItem(
                icon = Icons.Rounded.DarkMode,
                label = "Dark Mode",
                subtitle = "Switch between light and dark theme",
                checked = uiState.isDarkMode,
                onCheckedChange = viewModel::toggleDarkMode,
                iconColor = LisPurple
            )

            SettingsSectionHeader("Notifications")

            SettingsToggleItem(
                icon = Icons.Rounded.Notifications,
                label = "Push Notifications",
                subtitle = "Receive task reminders",
                checked = uiState.isNotificationsEnabled,
                onCheckedChange = viewModel::toggleNotifications,
                iconColor = LisGreen
            )

            SettingsToggleItem(
                icon = Icons.Rounded.Alarm,
                label = "Daily Reminder",
                subtitle = "Get a daily productivity summary",
                checked = uiState.isDailyReminderEnabled,
                onCheckedChange = viewModel::toggleDailyReminder,
                iconColor = Color(0xFF00B894)
            )

            SettingsSectionHeader("Premium")

            SettingsClickItem(
                icon = Icons.Rounded.Star,
                label = "Upgrade to Premium",
                subtitle = "Unlock all features",
                iconColor = Color(0xFFFDCB6E),
                onClick = { navController.navigate(Screen.Premium.route) }
            )

            SettingsSectionHeader("About")

            SettingsClickItem(
                icon = Icons.Rounded.Info,
                label = "App Version",
                subtitle = BuildConfig.VERSION_NAME,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {}
            )

            SettingsClickItem(
                icon = Icons.Rounded.PrivacyTip,
                label = "Privacy Policy",
                subtitle = "View our privacy policy",
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LisGreen)
            )
        }
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
