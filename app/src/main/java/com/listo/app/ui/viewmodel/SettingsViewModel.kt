package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val isDailyReminderEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.isDarkMode,
        prefs.isNotificationsEnabled,
        prefs.isDailyReminderEnabled
    ) { dark, notif, daily ->
        SettingsUiState(
            isDarkMode = dark,
            isNotificationsEnabled = notif,
            isDailyReminderEnabled = daily
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkMode(value: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(value) }
    }

    fun toggleNotifications(value: Boolean) {
        viewModelScope.launch { prefs.setNotifications(value) }
    }

    fun toggleDailyReminder(value: Boolean) {
        viewModelScope.launch { prefs.setDailyReminder(value) }
    }
}
