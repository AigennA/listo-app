package com.listo.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.listo.app.data.models.AccentColor
import com.listo.app.data.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "listo_prefs")

@Singleton
class PreferencesManager @Inject constructor(private val context: Context) {

    private object Keys {
        val THEME_MODE         = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR       = stringPreferencesKey("accent_color")
        val NOTIFICATIONS      = booleanPreferencesKey("notifications")
        val ONBOARDING_DONE    = booleanPreferencesKey("onboarding_done")
        val DAILY_REMINDER     = booleanPreferencesKey("daily_reminder")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { ThemeMode.fromName(it[Keys.THEME_MODE]) }

    val accentColor: Flow<AccentColor> = context.dataStore.data
        .map { AccentColor.fromName(it[Keys.ACCENT_COLOR]) }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS] ?: true }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    val isDailyReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DAILY_REMINDER] ?: false }

    suspend fun setThemeMode(value: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setAccentColor(value: AccentColor) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = value.name }
    }

    suspend fun setNotifications(value: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = value }
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = value }
    }

    suspend fun setDailyReminder(value: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_REMINDER] = value }
    }
}
