package com.example.hydraping.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.repository.WaterRepository
import com.example.hydraping.domain.model.DailySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val dailySummaries: List<DailySummary> = emptyList(),
    val weeklyAverage: Int = 0,
    val streak: Int = 0,
    val dailyGoal: Int = 2000
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val settings = repository.userSettings.first()
            val summaries = repository.getDailySummaries(7)
            val streak = repository.getStreak(settings.dailyGoalMl)
            val avg = if (summaries.isNotEmpty()) {
                summaries.sumOf { it.totalMl } / summaries.size
            } else 0

            _uiState.value = HistoryUiState(
                dailySummaries = summaries.reversed(),
                weeklyAverage = avg,
                streak = streak,
                dailyGoal = settings.dailyGoalMl
            )
        }
    }
}
