package com.example.hydraping.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.local.UserSettings
import com.example.hydraping.data.repository.WaterRepository
import com.example.hydraping.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
}
