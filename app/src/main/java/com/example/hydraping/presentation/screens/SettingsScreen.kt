package com.example.hydraping.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hydraping.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showSleepPicker by remember { mutableStateOf(false) }
    var showWakePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Remind",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Schedule visualization bar
        ScheduleBar(
            sleepStart = settings.sleepStartHour,
            sleepEnd = settings.sleepEndHour,
            intervalMinutes = settings.reminderIntervalMinutes,
            enabled = settings.notificationsEnabled
        )

        // Daily Goal card
        SettingCard(
            icon = Icons.Filled.WaterDrop,
            title = "Daily Goal"
        ) {
            // Slider value + real-world equivalent
            val glasses = settings.dailyGoalMl / 250
            val bottles = String.format("%.1f", settings.dailyGoalMl / 500f)

            Text(
                text = "${settings.dailyGoalMl}ml",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "≈ $glasses glasses  •  $bottles bottles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Animated slider color based on goal value
            val sliderProgress = (settings.dailyGoalMl - 500f) / (5000f - 500f)
            val activeColor by animateColorAsState(
                targetValue = if (sliderProgress > 0.5f) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                animationSpec = tween(300),
                label = "slider_color"
            )

            Slider(
                value = settings.dailyGoalMl.toFloat(),
                onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                valueRange = 500f..5000f,
                steps = 17,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("500ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("5000ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Notifications card
        SettingCard(
            icon = Icons.Filled.NotificationsActive,
            title = "Reminders"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settings.notificationsEnabled) "On" else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (settings.notificationsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Interval",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Animated segmented control for intervals
                val intervals = listOf(15, 30, 60, 90, 120)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    intervals.forEach { interval ->
                        val label = if (interval < 60) "${interval}m"
                        else "${interval / 60}h${if (interval % 60 > 0) "${interval % 60}m" else ""}"
                        val isSelected = settings.reminderIntervalMinutes == interval

                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = tween(250),
                            label = "chip_$interval"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(250),
                            label = "chip_text_$interval"
                        )

                        Surface(
                            onClick = { viewModel.updateReminderInterval(interval) },
                            shape = RoundedCornerShape(10.dp),
                            color = containerColor,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sleep schedule card
        SettingCard(
            icon = Icons.Filled.Bedtime,
            title = "Sleep Schedule"
        ) {
            // Calculate sleep duration
            val sleepDuration = if (settings.sleepStartHour > settings.sleepEndHour) {
                (24 - settings.sleepStartHour) + settings.sleepEndHour
            } else {
                settings.sleepEndHour - settings.sleepStartHour
            }
            val awakeHours = 24 - sleepDuration

            Text(
                text = "No reminders during sleep  •  ${sleepDuration}h sleep  •  ${awakeHours}h awake",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimePickerButton(
                    label = "Sleep",
                    hour = settings.sleepStartHour,
                    onClick = { showSleepPicker = true }
                )
                TimePickerButton(
                    label = "Wake",
                    hour = settings.sleepEndHour,
                    onClick = { showWakePicker = true }
                )
            }
        }

        // About
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "HydraPing v1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Stay hydrated, stay healthy!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Time picker dialogs
    if (showSleepPicker) {
        TimePickerDialog(
            title = "Sleep Time",
            initialHour = settings.sleepStartHour,
            onConfirm = { hour ->
                viewModel.updateSleepStart(hour)
                showSleepPicker = false
            },
            onDismiss = { showSleepPicker = false }
        )
    }

    if (showWakePicker) {
        TimePickerDialog(
            title = "Wake Time",
            initialHour = settings.sleepEndHour,
            onConfirm = { hour ->
                viewModel.updateSleepEnd(hour)
                showWakePicker = false
            },
            onDismiss = { showWakePicker = false }
        )
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ScheduleBar(
    sleepStart: Int,
    sleepEnd: Int,
    intervalMinutes: Int,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "24h Schedule",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val activeBarColor = MaterialTheme.colorScheme.primaryContainer
            val inactiveBarColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val barHeight = size.height
                val barWidth = size.width
                val hourWidth = barWidth / 24f

                // Background bar
                drawRoundRect(
                    color = inactiveBarColor,
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                )

                // Active hours
                if (enabled) {
                    val activeStart = sleepEnd
                    val activeEnd = sleepStart
                    if (activeEnd > activeStart) {
                        drawRoundRect(
                            color = activeBarColor,
                            topLeft = Offset(activeStart * hourWidth, 0f),
                            size = Size((activeEnd - activeStart) * hourWidth, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                    } else if (activeEnd < activeStart) {
                        drawRoundRect(
                            color = activeBarColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(activeEnd * hourWidth, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                        drawRoundRect(
                            color = activeBarColor,
                            topLeft = Offset(activeStart * hourWidth, 0f),
                            size = Size((24 - activeStart) * hourWidth, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                    }

                    // Reminder dots
                    val count = if (activeEnd > activeStart) activeEnd - activeStart else 24 - activeStart + activeEnd
                    var elapsed = 0
                    while (elapsed < count * 60) {
                        val dotHour = (activeStart + elapsed / 60) % 24
                        val cx = dotHour * hourWidth + hourWidth / 2
                        drawCircle(
                            color = primaryColor,
                            radius = 3f,
                            center = Offset(cx, barHeight / 2)
                        )
                        elapsed += intervalMinutes
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0:00", "6:00", "12:00", "18:00", "24:00").forEach { t ->
                    Text(t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun TimePickerButton(
    label: String,
    hour: Int,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialHour: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = 0,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "Cancel",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = { onConfirm(timePickerState.hour) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Confirm",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
