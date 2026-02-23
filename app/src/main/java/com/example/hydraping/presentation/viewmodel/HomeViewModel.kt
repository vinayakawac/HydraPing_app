package com.example.hydraping.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.local.entity.WaterEntry
import com.example.hydraping.data.repository.WaterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayTotal: Int = 0,
    val dailyGoal: Int = 2000,
    val todayEntries: List<WaterEntry> = emptyList(),
    val streak: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                _uiState.value = HomeUiState(
                    todayTotal = total,
                    dailyGoal = settings.dailyGoalMl,
                    todayEntries = entries,
                    streak = streak
                )
            }
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }
}
