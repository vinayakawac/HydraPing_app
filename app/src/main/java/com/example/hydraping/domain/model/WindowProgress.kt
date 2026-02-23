package com.example.hydraping.domain.model

import com.example.hydraping.data.local.entity.FocusTarget

/**
 * Computed progress for a single focus window.
 * Not persisted — derived from WaterEntry timestamps filtered to the window.
 */
data class WindowProgress(
    val target: FocusTarget,
    val consumedMl: Int,
    val status: WindowStatus
) {
    val remainingMl: Int get() = (target.targetAmountMl - consumedMl).coerceAtLeast(0)
    val progressFraction: Float
        get() = if (target.targetAmountMl > 0) (consumedMl.toFloat() / target.targetAmountMl).coerceIn(0f, 1f) else 0f
}

enum class WindowStatus {
    UPCOMING,   // Window hasn't started yet
    ACTIVE,     // Currently inside the time window
    COMPLETED,  // Target reached within the window
    MISSED,     // Window ended without meeting target
    EXPIRED     // Window ended (neutral — could be completed or missed)
}
