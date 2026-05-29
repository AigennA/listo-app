package com.listo.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "listo_prefs")

@Singleton
class PreferencesManager @Inject constructor(private val context: Context) {

    private object Keys {
        val DARK_MODE          = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS      = booleanPreferencesKey("notifications")
        val ONBOARDING_DONE    = booleanPreferencesKey("onboarding_done")
        val DAILY_REMINDER     = booleanPreferencesKey("daily_reminder")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DARK_MODE] ?: false }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS] ?: true }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    val isDailyReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DAILY_REMINDER] ?: false }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = value }
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
