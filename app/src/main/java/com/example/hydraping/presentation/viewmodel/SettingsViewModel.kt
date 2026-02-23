package com.example.hydraping.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.local.UserSettings
import com.example.hydraping.data.repository.WaterRepository
import com.example.hydraping.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WaterRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent = _toastEvent.asSharedFlow()

    // ──── Remind screen methods (existing) ────

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            repository.updateSettings(dailyGoalMl = goalMl)
        }
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            repository.updateSettings(reminderIntervalMinutes = minutes)
            if (settings.value.notificationsEnabled) {
                ReminderWorker.schedule(context, minutes)
            }
        }
    }

    fun updateSleepStart(hour: Int) {
        viewModelScope.launch {
            repository.updateSettings(sleepStartHour = hour)
        }
    }

    fun updateSleepEnd(hour: Int) {
        viewModelScope.launch {
            repository.updateSettings(sleepEndHour = hour)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(notificationsEnabled = enabled)
            if (enabled) {
                ReminderWorker.schedule(context, settings.value.reminderIntervalMinutes)
            } else {
                ReminderWorker.cancel(context)
            }
        }
    }

    // ──── Profile ────

    fun updateUserName(name: String) {
        viewModelScope.launch { repository.preferencesStore.updateUserName(name) }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch { repository.preferencesStore.updateGender(gender) }
    }

    fun updateHeightCm(height: Int) {
        viewModelScope.launch {
            repository.preferencesStore.updateHeightCm(height)
            autoRecalcGoal()
        }
    }

    fun updateWeightKg(weight: Int) {
        viewModelScope.launch {
            repository.preferencesStore.updateWeightKg(weight)
            autoRecalcGoal()
        }
    }

    fun updateAge(age: Int) {
        viewModelScope.launch { repository.preferencesStore.updateAge(age) }
    }

    fun updateActivityLevel(level: String) {
        viewModelScope.launch {
            repository.preferencesStore.updateActivityLevel(level)
            autoRecalcGoal()
        }
    }

    // ──── Hydration preferences ────

    fun toggleAutoCalculateGoal(auto: Boolean) {
        viewModelScope.launch {
            repository.preferencesStore.updateAutoCalculateGoal(auto)
            if (auto) autoRecalcGoal()
        }
    }

    private suspend fun autoRecalcGoal() {
        if (settings.value.autoCalculateGoal) {
            val recommended = settings.value.recommendedGoalMl
            repository.updateSettings(dailyGoalMl = recommended)
        }
    }

    // ──── Reminder sound ────

    fun updateReminderSoundUri(uri: String) {
        viewModelScope.launch { repository.preferencesStore.updateReminderSoundUri(uri) }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateVibrationEnabled(enabled) }
    }

    fun toggleSilentModeOverride(override: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateSilentModeOverride(override) }
    }

    // ──── Notification controls ────

    fun toggleHeadsUp(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateHeadsUpEnabled(enabled) }
    }

    fun updateSnoozeDuration(minutes: Int) {
        viewModelScope.launch { repository.preferencesStore.updateSnoozeDuration(minutes) }
    }

    fun togglePersistentNotification(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updatePersistentNotification(enabled) }
    }

    fun toggleLockScreenVisibility(visible: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateLockScreenVisibility(visible) }
    }

    // ──── Focus target settings ────

    fun updateFocusWindowReminder(minutes: Int) {
        viewModelScope.launch { repository.preferencesStore.updateFocusWindowReminder(minutes) }
    }

    fun toggleOverlapAllowed(allowed: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateOverlapAllowed(allowed) }
    }

    fun toggleCelebrationAnimation(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateCelebrationAnimation(enabled) }
    }

    // ──── Appearance ────

    fun updateThemeMode(mode: String) {
        viewModelScope.launch { repository.preferencesStore.updateThemeMode(mode) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateDynamicColor(enabled) }
    }

    fun toggleReduceAnimations(reduce: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateReduceAnimations(reduce) }
    }

    fun toggleCompactMode(compact: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateCompactMode(compact) }
    }

    // ──── Achievements ────

    fun toggleAchievements(enabled: Boolean) {
        viewModelScope.launch { repository.preferencesStore.updateAchievementsEnabled(enabled) }
    }

    // ──── Data management ────

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _toastEvent.tryEmit("All hydration history cleared")
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            repository.preferencesStore.clearAll()
            _toastEvent.tryEmit("All settings reset to defaults")
        }
    }

    fun forceRescheduleReminders() {
        viewModelScope.launch {
            ReminderWorker.cancel(context)
            if (settings.value.notificationsEnabled) {
                ReminderWorker.schedule(context, settings.value.reminderIntervalMinutes)
            }
            _toastEvent.tryEmit("Reminders rescheduled")
        }
    }
}
