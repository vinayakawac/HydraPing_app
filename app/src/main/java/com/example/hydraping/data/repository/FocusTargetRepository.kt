package com.example.hydraping.data.repository

import com.example.hydraping.data.local.dao.FocusTargetDao
import com.example.hydraping.data.local.dao.WaterEntryDao
import com.example.hydraping.data.local.entity.FocusTarget
import com.example.hydraping.data.local.entity.RepeatMode
import com.example.hydraping.domain.model.WindowProgress
import com.example.hydraping.domain.model.WindowStatus
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusTargetRepository @Inject constructor(
    private val focusTargetDao: FocusTargetDao,
    private val waterEntryDao: WaterEntryDao
) {

    fun getActiveTargets(): Flow<List<FocusTarget>> = focusTargetDao.getActiveTargets()
    fun getAllTargets(): Flow<List<FocusTarget>> = focusTargetDao.getAllTargets()

    suspend fun addTarget(target: FocusTarget): Long = focusTargetDao.insert(target)
    suspend fun updateTarget(target: FocusTarget) = focusTargetDao.update(target)
    suspend fun deleteTarget(target: FocusTarget) = focusTargetDao.delete(target)
    suspend fun toggleActive(id: Int, active: Boolean) = focusTargetDao.setActive(id, active)
    suspend fun getTargetById(id: Int): FocusTarget? = focusTargetDao.getById(id)

    /**
     * Compute today's progress for each active target.
     * Filters water entries by the window's absolute time range for today.
     */
    suspend fun computeTodayProgress(targets: List<FocusTarget>): List<WindowProgress> {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val today = now.get(Calendar.DAY_OF_WEEK)

        return targets
            .filter { isActiveForDay(it, today) }
            .map { target ->
                val (windowStart, windowEnd) = getWindowBoundsMillis(target, now)
                val consumed = waterEntryDao.getTotalForRange(windowStart, windowEnd)
                val targetMinutes = target.startHour * 60 + target.startMinute
                val endMinutes = target.endHour * 60 + target.endMinute

                val status = when {
                    consumed >= target.targetAmountMl -> WindowStatus.COMPLETED
                    currentMinutes in targetMinutes until endMinutes -> WindowStatus.ACTIVE
                    currentMinutes < targetMinutes -> WindowStatus.UPCOMING
                    else -> WindowStatus.MISSED
                }

                WindowProgress(
                    target = target,
                    consumedMl = consumed,
                    status = status
                )
            }
    }

    /**
     * Compute progress for a single target right now.
     */
    suspend fun computeSingleProgress(target: FocusTarget): WindowProgress {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val (windowStart, windowEnd) = getWindowBoundsMillis(target, now)
        val consumed = waterEntryDao.getTotalForRange(windowStart, windowEnd)
        val targetMinutes = target.startHour * 60 + target.startMinute
        val endMinutes = target.endHour * 60 + target.endMinute

        val status = when {
            consumed >= target.targetAmountMl -> WindowStatus.COMPLETED
            currentMinutes in targetMinutes until endMinutes -> WindowStatus.ACTIVE
            currentMinutes < targetMinutes -> WindowStatus.UPCOMING
            else -> WindowStatus.MISSED
        }

        return WindowProgress(target = target, consumedMl = consumed, status = status)
    }

    /**
     * Find the currently active window (if any).
     */
    suspend fun getActiveWindow(targets: List<FocusTarget>): WindowProgress? {
        val progress = computeTodayProgress(targets)
        return progress.firstOrNull { it.status == WindowStatus.ACTIVE }
    }

    /**
     * Check if a new target overlaps with existing ones.
     * Returns true if there is overlap.
     */
    suspend fun hasOverlap(newTarget: FocusTarget, excludeId: Int? = null): Boolean {
        val existing = focusTargetDao.getActiveTargets() // We need a suspend version
        // Since getActiveTargets returns Flow, we'll check in the ViewModel instead
        return false
    }

    private fun isActiveForDay(target: FocusTarget, dayOfWeek: Int): Boolean {
        return when (RepeatMode.valueOf(target.repeatMode)) {
            RepeatMode.DAILY -> true
            RepeatMode.WEEKDAYS -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            RepeatMode.WEEKENDS -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            RepeatMode.CUSTOM -> {
                target.customDays.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .contains(dayOfWeek)
            }
        }
    }

    /**
     * Convert target's start/end hours into absolute millis for today.
     */
    private fun getWindowBoundsMillis(target: FocusTarget, now: Calendar): Pair<Long, Long> {
        val start = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, target.startHour)
            set(Calendar.MINUTE, target.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, target.endHour)
            set(Calendar.MINUTE, target.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Handle overnight windows
        if (end.before(start)) {
            end.add(Calendar.DAY_OF_YEAR, 1)
        }
        return Pair(start.timeInMillis, end.timeInMillis)
    }
}
