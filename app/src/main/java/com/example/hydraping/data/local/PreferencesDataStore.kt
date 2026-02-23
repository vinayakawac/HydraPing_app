package com.example.hydraping.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydraping_preferences")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

data class UserSettings(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalMinutes: Int = 60,
    val sleepStartHour: Int = 22,
    val sleepEndHour: Int = 7,
    val notificationsEnabled: Boolean = true,
    // Profile
    val userName: String = "",
    val gender: String = "Prefer not to say",
    val heightCm: Int = 170,
    val weightKg: Int = 70,
    val age: Int = 0,
    val activityLevel: String = "Moderate",
    // Hydration preferences
    val autoCalculateGoal: Boolean = false,
    // Reminder sound
    val reminderSoundUri: String = "",
    val vibrationEnabled: Boolean = true,
    val silentModeOverride: Boolean = false,
    // Notification controls
    val headsUpEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 10,
    val persistentNotification: Boolean = false,
    val lockScreenVisibility: Boolean = true,
    // Focus target settings
    val focusWindowReminderMinutes: Int = 15,
    val overlapAllowed: Boolean = false,
    val celebrationAnimationEnabled: Boolean = true,
    // Appearance
    val themeMode: String = "System",
    val dynamicColorEnabled: Boolean = true,
    val reduceAnimations: Boolean = false,
    val compactMode: Boolean = false,
    // Achievements
    val achievementsEnabled: Boolean = true
) {
    /** Calculate recommended daily goal from weight + activity */
    val recommendedGoalMl: Int
        get() {
            val base = weightKg * 35
            val activityBonus = when (activityLevel) {
                "High" -> 500
                "Moderate" -> 250
                else -> 0
            }
            return base + activityBonus
        }
}

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
        // Profile
        val USER_NAME = stringPreferencesKey("user_name")
        val GENDER = stringPreferencesKey("gender")
        val HEIGHT_CM = intPreferencesKey("height_cm")
        val WEIGHT_KG = intPreferencesKey("weight_kg")
        val AGE = intPreferencesKey("age")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        // Hydration
        val AUTO_CALCULATE_GOAL = booleanPreferencesKey("auto_calculate_goal")
        // Reminder sound
        val REMINDER_SOUND_URI = stringPreferencesKey("reminder_sound_uri")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SILENT_MODE_OVERRIDE = booleanPreferencesKey("silent_mode_override")
        // Notification controls
        val HEADS_UP_ENABLED = booleanPreferencesKey("heads_up_enabled")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration_minutes")
        val PERSISTENT_NOTIFICATION = booleanPreferencesKey("persistent_notification")
        val LOCK_SCREEN_VISIBILITY = booleanPreferencesKey("lock_screen_visibility")
        // Focus target settings
        val FOCUS_WINDOW_REMINDER = intPreferencesKey("focus_window_reminder_minutes")
        val OVERLAP_ALLOWED = booleanPreferencesKey("overlap_allowed")
        val CELEBRATION_ANIMATION = booleanPreferencesKey("celebration_animation_enabled")
        // Appearance
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        // Achievements
        val ACHIEVEMENTS_ENABLED = booleanPreferencesKey("achievements_enabled")
    }

    /** Dedicated flow for app-root theme observation (avoids full UserSettings recomposition) */
    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: "System"
        when (raw.lowercase()) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    /** Dedicated flow for dynamic color toggle */
    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DYNAMIC_COLOR] ?: true
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            dailyGoalMl = prefs[Keys.DAILY_GOAL] ?: 2000,
            reminderIntervalMinutes = prefs[Keys.REMINDER_INTERVAL] ?: 60,
            sleepStartHour = prefs[Keys.SLEEP_START_HOUR] ?: 22,
            sleepEndHour = prefs[Keys.SLEEP_END_HOUR] ?: 7,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            userName = prefs[Keys.USER_NAME] ?: "",
            gender = prefs[Keys.GENDER] ?: "Prefer not to say",
            heightCm = prefs[Keys.HEIGHT_CM] ?: 170,
            weightKg = prefs[Keys.WEIGHT_KG] ?: 70,
            age = prefs[Keys.AGE] ?: 0,
            activityLevel = prefs[Keys.ACTIVITY_LEVEL] ?: "Moderate",
            autoCalculateGoal = prefs[Keys.AUTO_CALCULATE_GOAL] ?: false,
            reminderSoundUri = prefs[Keys.REMINDER_SOUND_URI] ?: "",
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            silentModeOverride = prefs[Keys.SILENT_MODE_OVERRIDE] ?: false,
            headsUpEnabled = prefs[Keys.HEADS_UP_ENABLED] ?: true,
            snoozeDurationMinutes = prefs[Keys.SNOOZE_DURATION] ?: 10,
            persistentNotification = prefs[Keys.PERSISTENT_NOTIFICATION] ?: false,
            lockScreenVisibility = prefs[Keys.LOCK_SCREEN_VISIBILITY] ?: true,
            focusWindowReminderMinutes = prefs[Keys.FOCUS_WINDOW_REMINDER] ?: 15,
            overlapAllowed = prefs[Keys.OVERLAP_ALLOWED] ?: false,
            celebrationAnimationEnabled = prefs[Keys.CELEBRATION_ANIMATION] ?: true,
            themeMode = prefs[Keys.THEME_MODE] ?: "System",
            dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: true,
            reduceAnimations = prefs[Keys.REDUCE_ANIMATIONS] ?: false,
            compactMode = prefs[Keys.COMPACT_MODE] ?: false,
            achievementsEnabled = prefs[Keys.ACHIEVEMENTS_ENABLED] ?: true
        )
    }

    suspend fun updateDailyGoal(goalMl: Int) { context.dataStore.edit { it[Keys.DAILY_GOAL] = goalMl } }
    suspend fun updateReminderInterval(minutes: Int) { context.dataStore.edit { it[Keys.REMINDER_INTERVAL] = minutes } }
    suspend fun updateSleepStartHour(hour: Int) { context.dataStore.edit { it[Keys.SLEEP_START_HOUR] = hour } }
    suspend fun updateSleepEndHour(hour: Int) { context.dataStore.edit { it[Keys.SLEEP_END_HOUR] = hour } }
    suspend fun updateNotificationsEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled } }

    // Profile
    suspend fun updateUserName(name: String) { context.dataStore.edit { it[Keys.USER_NAME] = name } }
    suspend fun updateGender(gender: String) { context.dataStore.edit { it[Keys.GENDER] = gender } }
    suspend fun updateHeightCm(height: Int) { context.dataStore.edit { it[Keys.HEIGHT_CM] = height } }
    suspend fun updateWeightKg(weight: Int) { context.dataStore.edit { it[Keys.WEIGHT_KG] = weight } }
    suspend fun updateAge(age: Int) { context.dataStore.edit { it[Keys.AGE] = age } }
    suspend fun updateActivityLevel(level: String) { context.dataStore.edit { it[Keys.ACTIVITY_LEVEL] = level } }

    // Hydration
    suspend fun updateAutoCalculateGoal(auto: Boolean) { context.dataStore.edit { it[Keys.AUTO_CALCULATE_GOAL] = auto } }

    // Sound & vibration
    suspend fun updateReminderSoundUri(uri: String) { context.dataStore.edit { it[Keys.REMINDER_SOUND_URI] = uri } }
    suspend fun updateVibrationEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled } }
    suspend fun updateSilentModeOverride(override: Boolean) { context.dataStore.edit { it[Keys.SILENT_MODE_OVERRIDE] = override } }

    // Notification controls
    suspend fun updateHeadsUpEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.HEADS_UP_ENABLED] = enabled } }
    suspend fun updateSnoozeDuration(minutes: Int) { context.dataStore.edit { it[Keys.SNOOZE_DURATION] = minutes } }
    suspend fun updatePersistentNotification(enabled: Boolean) { context.dataStore.edit { it[Keys.PERSISTENT_NOTIFICATION] = enabled } }
    suspend fun updateLockScreenVisibility(visible: Boolean) { context.dataStore.edit { it[Keys.LOCK_SCREEN_VISIBILITY] = visible } }

    // Focus target settings
    suspend fun updateFocusWindowReminder(minutes: Int) { context.dataStore.edit { it[Keys.FOCUS_WINDOW_REMINDER] = minutes } }
    suspend fun updateOverlapAllowed(allowed: Boolean) { context.dataStore.edit { it[Keys.OVERLAP_ALLOWED] = allowed } }
    suspend fun updateCelebrationAnimation(enabled: Boolean) { context.dataStore.edit { it[Keys.CELEBRATION_ANIMATION] = enabled } }

    // Appearance
    suspend fun updateThemeMode(mode: String) { context.dataStore.edit { it[Keys.THEME_MODE] = mode } }
    suspend fun updateDynamicColor(enabled: Boolean) { context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled } }
    suspend fun updateReduceAnimations(reduce: Boolean) { context.dataStore.edit { it[Keys.REDUCE_ANIMATIONS] = reduce } }
    suspend fun updateCompactMode(compact: Boolean) { context.dataStore.edit { it[Keys.COMPACT_MODE] = compact } }

    // Achievements
    suspend fun updateAchievementsEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.ACHIEVEMENTS_ENABLED] = enabled } }

    /** Clear all preferences */
    suspend fun clearAll() { context.dataStore.edit { it.clear() } }
}
