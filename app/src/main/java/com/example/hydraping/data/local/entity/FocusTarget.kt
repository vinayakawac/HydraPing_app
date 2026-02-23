package com.example.hydraping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatMode {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    CUSTOM
}

@Entity(tableName = "focus_targets")
data class FocusTarget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val targetAmountMl: Int,
    val repeatMode: String = RepeatMode.DAILY.name,
    val customDays: String = "",       // comma-separated day-of-week ints (e.g. "2,3,4,5,6")
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** "09:00–11:00" display label */
    val timeRangeLabel: String
        get() = String.format("%02d:%02d–%02d:%02d", startHour, startMinute, endHour, endMinute)

    /** Total window length in minutes */
    val durationMinutes: Int
        get() {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            return if (endTotal > startTotal) endTotal - startTotal
            else (24 * 60 - startTotal) + endTotal
        }
}
