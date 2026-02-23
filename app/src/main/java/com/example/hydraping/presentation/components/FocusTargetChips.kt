package com.example.hydraping.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydraping.domain.model.WindowProgress
import com.example.hydraping.domain.model.WindowStatus

@Composable
fun FocusTargetChips(
    progressList: List<WindowProgress>,
    onAddTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Focus Targets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Surface(
                onClick = onAddTarget,
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add target",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        if (progressList.isEmpty()) {
            Surface(
                onClick = onAddTarget,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set a focus target for time-based hydration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(progressList, key = { it.target.id }) { wp ->
                    FocusChip(wp)
                }
            }
        }
    }
}

@Composable
private fun FocusChip(windowProgress: WindowProgress) {
    val isActive = windowProgress.status == WindowStatus.ACTIVE
    val isCompleted = windowProgress.status == WindowStatus.COMPLETED
    val isMissed = windowProgress.status == WindowStatus.MISSED

    val containerColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primaryContainer
            isCompleted -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "chip_bg"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Time range + status icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = windowProgress.target.timeRangeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                        isCompleted -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isCompleted) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress text
            Text(
                text = "${windowProgress.consumedMl} / ${windowProgress.target.targetAmountMl} ml",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = when {
                    isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                    isCompleted -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress bar
            val progressColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> MaterialTheme.colorScheme.tertiary
                    isActive -> MaterialTheme.colorScheme.primary
                    isMissed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                animationSpec = tween(300),
                label = "progress_color"
            )
            val trackColor = when {
                isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            }

            LinearProgressIndicator(
                progress = { windowProgress.progressFraction },
                color = progressColor,
                trackColor = trackColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            // Status label
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (windowProgress.status) {
                    WindowStatus.ACTIVE -> "Active now"
                    WindowStatus.COMPLETED -> "Done!"
                    WindowStatus.UPCOMING -> "Upcoming"
                    WindowStatus.MISSED -> "Missed"
                    WindowStatus.EXPIRED -> "Ended"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isActive -> MaterialTheme.colorScheme.primary
                    isCompleted -> MaterialTheme.colorScheme.tertiary
                    isMissed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun FocusTargetBanner(
    windowProgress: WindowProgress,
    modifier: Modifier = Modifier
) {
    val endTimeLabel = String.format(
        "%02d:%02d",
        windowProgress.target.endHour,
        windowProgress.target.endMinute
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Focus Target Active",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${windowProgress.remainingMl}ml remaining before $endTimeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            // Mini circular-like progress
            Text(
                text = "${(windowProgress.progressFraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
