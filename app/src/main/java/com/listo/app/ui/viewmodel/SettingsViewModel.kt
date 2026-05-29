package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.AccentColor
import com.listo.app.data.models.ThemeMode
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.GREEN,
    val isNotificationsEnabled: Boolean = true,
    val isDailyReminderEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.themeMode,
        prefs.accentColor,
        prefs.isNotificationsEnabled,
        prefs.isDailyReminderEnabled
    ) { theme, accent, notif, daily ->
        SettingsUiState(
            themeMode = theme,
            accentColor = accent,
            isNotificationsEnabled = notif,
            isDailyReminderEnabled = daily
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(value) }
    }

    fun setAccentColor(value: AccentColor) {
        viewModelScope.launch { prefs.setAccentColor(value) }
    }

    fun toggleNotifications(value: Boolean) {
        viewModelScope.launch { prefs.setNotifications(value) }
    }

    fun toggleDailyReminder(value: Boolean) {
        viewModelScope.launch { prefs.setDailyReminder(value) }
    }
}
