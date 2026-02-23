package com.example.hydraping.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.local.entity.WaterEntry
import com.example.hydraping.data.repository.WaterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val todayTotal: Int = 0,
    val dailyGoal: Int = 2000,
    val todayEntries: List<WaterEntry> = emptyList(),
    val streak: Int = 0,
    val insightText: String = "",
    val goalReached: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Emitted once per water-log so the UI can fire haptic feedback */
    private val _hapticEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticEvent: SharedFlow<Unit> = _hapticEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTodayTotal(),
                repository.getTodayEntries(),
                repository.userSettings
            ) { total, entries, settings ->
                Triple(total, entries, settings)
            }.collect { (total, entries, settings) ->
                val streak = repository.getStreak(settings.dailyGoalMl)
                val goal = settings.dailyGoalMl
                val insight = buildInsightText(total, goal, streak)
                _uiState.value = HomeUiState(
                    todayTotal = total,
                    dailyGoal = goal,
                    todayEntries = entries,
                    streak = streak,
                    insightText = insight,
                    goalReached = total >= goal
                )
            }
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
            _hapticEvent.tryEmit(Unit)
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    private fun buildInsightText(total: Int, goal: Int, streak: Int): String {
        if (goal <= 0) return ""
        val pct = total * 100 / goal
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val remaining = (goal - total).coerceAtLeast(0)

        return when {
            total >= goal && streak > 2 -> "Goal met! $streak-day streak \uD83D\uDD25"
            total >= goal -> "Goal reached! Keep it up!"
            pct >= 75 -> "Almost there — ${remaining}ml to go"
            pct >= 50 && hour < 15 -> "On track — ahead of schedule"
            pct >= 50 -> "Halfway there, keep sipping"
            pct >= 25 && hour >= 17 -> "Behind by ${remaining}ml — you got this"
            hour < 10 -> "Good morning! Start sipping early"
            hour >= 20 && pct < 50 -> "Behind by ${remaining}ml — hydrate before bed"
            else -> "Stay hydrated — ${remaining}ml remaining"
        }
    }
}
