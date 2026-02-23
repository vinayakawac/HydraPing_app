package com.example.hydraping.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydraping.data.local.entity.FocusTarget
import com.example.hydraping.data.local.entity.RepeatMode
import com.example.hydraping.data.repository.FocusTargetRepository
import com.example.hydraping.domain.model.WindowProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusTargetUiState(
    val allProgress: List<WindowProgress> = emptyList(),
    val activeWindow: WindowProgress? = null,
    val isLoading: Boolean = true
)

data class CreateTargetState(
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 11,
    val endMinute: Int = 0,
    val targetAmountMl: Int = 500,
    val repeatMode: RepeatMode = RepeatMode.DAILY,
    val overlapError: Boolean = false,
    val validationError: String? = null
)

@HiltViewModel
class FocusTargetViewModel @Inject constructor(
    private val repository: FocusTargetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusTargetUiState())
    val uiState: StateFlow<FocusTargetUiState> = _uiState.asStateFlow()

    private val _createState = MutableStateFlow(CreateTargetState())
    val createState: StateFlow<CreateTargetState> = _createState.asStateFlow()

    /** Emitted when a focus target is just completed — triggers celebration */
    private val _celebrationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val celebrationEvent: SharedFlow<String> = _celebrationEvent.asSharedFlow()

    init {
        observeTargets()
        // Periodic refresh for window status changes (every 30s)
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                refreshProgress()
            }
        }
    }

    private fun observeTargets() {
        viewModelScope.launch {
            repository.getActiveTargets().collect { targets ->
                val progress = repository.computeTodayProgress(targets)
                val active = progress.firstOrNull { it.status == com.example.hydraping.domain.model.WindowStatus.ACTIVE }
                _uiState.value = FocusTargetUiState(
                    allProgress = progress,
                    activeWindow = active,
                    isLoading = false
                )
            }
        }
    }

    fun refreshProgress() {
        viewModelScope.launch {
            val targets = _uiState.value.allProgress.map { it.target }
            if (targets.isEmpty()) return@launch
            val oldActive = _uiState.value.activeWindow
            val progress = repository.computeTodayProgress(targets)
            val active = progress.firstOrNull { it.status == com.example.hydraping.domain.model.WindowStatus.ACTIVE }

            // Check for newly completed windows
            val oldStatuses = _uiState.value.allProgress.associate { it.target.id to it.status }
            progress.forEach { wp ->
                if (wp.status == com.example.hydraping.domain.model.WindowStatus.COMPLETED &&
                    oldStatuses[wp.target.id] != com.example.hydraping.domain.model.WindowStatus.COMPLETED
                ) {
                    _celebrationEvent.tryEmit(wp.target.timeRangeLabel)
                }
            }

            _uiState.value = _uiState.value.copy(
                allProgress = progress,
                activeWindow = active
            )
        }
    }

    // ── Create Target Form ──

    fun updateStartTime(hour: Int, minute: Int) {
        _createState.value = _createState.value.copy(
            startHour = hour,
            startMinute = minute,
            validationError = null
        )
    }

    fun updateEndTime(hour: Int, minute: Int) {
        _createState.value = _createState.value.copy(
            endHour = hour,
            endMinute = minute,
            validationError = null
        )
    }

    fun updateTargetAmount(amount: Int) {
        _createState.value = _createState.value.copy(
            targetAmountMl = amount.coerceIn(100, 5000),
            validationError = null
        )
    }

    fun updateRepeatMode(mode: RepeatMode) {
        _createState.value = _createState.value.copy(repeatMode = mode)
    }

    fun saveTarget(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _createState.value
            val startTotal = state.startHour * 60 + state.startMinute
            val endTotal = state.endHour * 60 + state.endMinute

            // Validate end > start
            if (endTotal <= startTotal) {
                _createState.value = state.copy(
                    validationError = "End time must be after start time"
                )
                return@launch
            }

            // Validate minimum window (15 min)
            if (endTotal - startTotal < 15) {
                _createState.value = state.copy(
                    validationError = "Window must be at least 15 minutes"
                )
                return@launch
            }

            val target = FocusTarget(
                startHour = state.startHour,
                startMinute = state.startMinute,
                endHour = state.endHour,
                endMinute = state.endMinute,
                targetAmountMl = state.targetAmountMl,
                repeatMode = state.repeatMode.name
            )

            repository.addTarget(target)
            // Reset form
            _createState.value = CreateTargetState()
            onSuccess()
        }
    }

    fun deleteTarget(target: FocusTarget) {
        viewModelScope.launch {
            repository.deleteTarget(target)
        }
    }

    fun toggleTargetActive(id: Int, active: Boolean) {
        viewModelScope.launch {
            repository.toggleActive(id, active)
        }
    }
}
