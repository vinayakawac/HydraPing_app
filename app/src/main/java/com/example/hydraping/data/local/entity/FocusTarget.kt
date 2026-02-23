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
    /** "9:00 AM – 11:00 AM" display label */
    val timeRangeLabel: String
        get() = "${formatTime12(startHour, startMinute)} – ${formatTime12(endHour, endMinute)}"

    /** Total window length in minutes */
    val durationMinutes: Int
        get() {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            return if (endTotal > startTotal) endTotal - startTotal
            else (24 * 60 - startTotal) + endTotal
        }
}

/** Convert 24-hour time to 12-hour AM/PM string, e.g. 14:05 → "2:05 PM" */
fun formatTime12(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", h, minute, amPm)
}
