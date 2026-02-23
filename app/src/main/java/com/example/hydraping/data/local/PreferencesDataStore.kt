package com.example.hydraping.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydraping_preferences")

data class UserSettings(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalMinutes: Int = 60,
    val sleepStartHour: Int = 22,
    val sleepEndHour: Int = 7,
    val notificationsEnabled: Boolean = true
)

@Singleton
class PreferencesDataStore @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val DAILY_GOAL = intPreferencesKey("daily_goal_ml")
        val REMINDER_INTERVAL = intPreferencesKey("reminder_interval_minutes")
        val SLEEP_START_HOUR = intPreferencesKey("sleep_start_hour")
        val SLEEP_END_HOUR = intPreferencesKey("sleep_end_hour")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            dailyGoalMl = preferences[Keys.DAILY_GOAL] ?: 2000,
            reminderIntervalMinutes = preferences[Keys.REMINDER_INTERVAL] ?: 60,
            sleepStartHour = preferences[Keys.SLEEP_START_HOUR] ?: 22,
            sleepEndHour = preferences[Keys.SLEEP_END_HOUR] ?: 7,
            notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL] = goalMl }
    }

    suspend fun updateReminderInterval(minutes: Int) {
        context.dataStore.edit { it[Keys.REMINDER_INTERVAL] = minutes }
    }

    suspend fun updateSleepStartHour(hour: Int) {
        context.dataStore.edit { it[Keys.SLEEP_START_HOUR] = hour }
    }

    suspend fun updateSleepEndHour(hour: Int) {
        context.dataStore.edit { it[Keys.SLEEP_END_HOUR] = hour }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
