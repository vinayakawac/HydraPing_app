package com.example.hydraping.data.repository

import com.example.hydraping.data.local.PreferencesDataStore
import com.example.hydraping.data.local.UserSettings
import com.example.hydraping.data.local.dao.WaterEntryDao
import com.example.hydraping.data.local.entity.WaterEntry
import com.example.hydraping.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepository @Inject constructor(
    private val waterEntryDao: WaterEntryDao,
    private val preferencesDataStore: PreferencesDataStore
) {
    val userSettings: Flow<UserSettings> = preferencesDataStore.userSettings

    fun getTodayEntries(): Flow<List<WaterEntry>> {
        val (start, end) = getDayBounds(System.currentTimeMillis())
        return waterEntryDao.getEntriesForDay(start, end)
    }

    fun getTodayTotal(): Flow<Int> {
        val (start, end) = getDayBounds(System.currentTimeMillis())
        return waterEntryDao.getTotalForDay(start, end)
    }

    suspend fun logWater(amountMl: Int) {
        waterEntryDao.insert(WaterEntry(amountMl = amountMl))
    }

    suspend fun deleteEntry(id: Int) {
        waterEntryDao.deleteById(id)
    }

    suspend fun getDailySummaries(days: Int): List<DailySummary> {
        val summaries = mutableListOf<DailySummary>()
        val calendar = Calendar.getInstance()

        for (i in 0 until days) {
            val dayTime = calendar.timeInMillis
            val (start, end) = getDayBounds(dayTime)
            val total = waterEntryDao.getTotalForRange(start, end)
            summaries.add(
                DailySummary(
                    date = dayTime,
                    totalMl = total,
                    dayLabel = getDayLabel(calendar)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return summaries
    }

    suspend fun getStreak(dailyGoalMl: Int): Int {
        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Start from yesterday

        while (true) {
            val (start, end) = getDayBounds(calendar.timeInMillis)
            val total = waterEntryDao.getTotalForRange(start, end)
            if (total >= dailyGoalMl) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun updateSettings(
        dailyGoalMl: Int? = null,
        reminderIntervalMinutes: Int? = null,
        sleepStartHour: Int? = null,
        sleepEndHour: Int? = null,
        notificationsEnabled: Boolean? = null
    ) {
        dailyGoalMl?.let { preferencesDataStore.updateDailyGoal(it) }
        reminderIntervalMinutes?.let { preferencesDataStore.updateReminderInterval(it) }
        sleepStartHour?.let { preferencesDataStore.updateSleepStartHour(it) }
        sleepEndHour?.let { preferencesDataStore.updateSleepEndHour(it) }
        notificationsEnabled?.let { preferencesDataStore.updateNotificationsEnabled(it) }
    }

    private fun getDayBounds(timeMillis: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    private fun getDayLabel(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }
}
