package com.listo.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val DAILY_REMINDER_HOUR   = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val CATEGORIES_SEEDED  = booleanPreferencesKey("categories_seeded")
    }

    val isCategoriesSeeded: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.CATEGORIES_SEEDED] ?: false }

    suspend fun setCategoriesSeeded(value: Boolean) {
        context.dataStore.edit { it[Keys.CATEGORIES_SEEDED] = value }
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

    val dailyReminderHour: Flow<Int> = context.dataStore.data
        .map { it[Keys.DAILY_REMINDER_HOUR] ?: 18 }

    val dailyReminderMinute: Flow<Int> = context.dataStore.data
        .map { it[Keys.DAILY_REMINDER_MINUTE] ?: 0 }

    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.DAILY_REMINDER_HOUR] = hour
            it[Keys.DAILY_REMINDER_MINUTE] = minute
        }
    }

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
