package com.example.hydraping.presentation.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hydraping.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── 1. Profile ──
        item {
            SettingsCard(icon = Icons.Filled.Person, title = "User Profile") {
                // Name
                var nameField by remember(settings.userName) { mutableStateOf(settings.userName) }
                SettingsTextField(
                    label = "Name",
                    value = nameField,
                    onValueChange = { nameField = it },
                    onDone = { viewModel.updateUserName(nameField) }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Gender dropdown
                SettingsDropdown(
                    label = "Gender",
                    selected = settings.gender,
                    options = listOf("Male", "Female", "Other", "Prefer not to say"),
                    onSelected = { viewModel.updateGender(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Height + Weight row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var heightField by remember(settings.heightCm) { mutableStateOf(settings.heightCm.toString()) }
                    SettingsTextField(
                        label = "Height (cm)",
                        value = heightField,
                        onValueChange = { heightField = it.filter { c -> c.isDigit() } },
                        onDone = { heightField.toIntOrNull()?.let { viewModel.updateHeightCm(it) } },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    var weightField by remember(settings.weightKg) { mutableStateOf(settings.weightKg.toString()) }
                    SettingsTextField(
                        label = "Weight (kg)",
                        value = weightField,
                        onValueChange = { weightField = it.filter { c -> c.isDigit() } },
                        onDone = { weightField.toIntOrNull()?.let { viewModel.updateWeightKg(it) } },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Age
                var ageField by remember(settings.age) { mutableStateOf(if (settings.age > 0) settings.age.toString() else "") }
                SettingsTextField(
                    label = "Age (optional)",
                    value = ageField,
                    onValueChange = { ageField = it.filter { c -> c.isDigit() } },
                    onDone = { ageField.toIntOrNull()?.let { viewModel.updateAge(it) } },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Activity level
                Text(
                    text = "Activity Level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                SegmentedControl(
                    options = listOf("Low", "Moderate", "High"),
                    selected = settings.activityLevel,
                    onSelected = { viewModel.updateActivityLevel(it) },
                    icon = Icons.Filled.FitnessCenter
                )
            }
        }

        // ── 2. Hydration Goal ──
        item {
            SettingsCard(icon = Icons.Filled.WaterDrop, title = "Hydration Goal") {
                SettingsToggleRow(
                    label = "Auto-calculate from profile",
                    checked = settings.autoCalculateGoal,
                    onCheckedChange = { viewModel.toggleAutoCalculateGoal(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (settings.autoCalculateGoal) {
                    Text(
                        text = "Recommended: ${settings.recommendedGoalMl}ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Based on ${settings.weightKg}kg × 35ml" +
                                if (settings.activityLevel == "High") " + 500ml (high activity)"
                                else if (settings.activityLevel == "Moderate") " + 250ml (moderate)"
                                else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "${settings.dailyGoalMl}ml",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val glasses = settings.dailyGoalMl / 250
                    val bottles = String.format("%.1f", settings.dailyGoalMl / 500f)
                    Text(
                        text = "≈ $glasses glasses  •  $bottles bottles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.dailyGoalMl.toFloat(),
                        onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                        valueRange = 500f..5000f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
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

                if (settings.autoCalculateGoal) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${settings.dailyGoalMl}ml",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── 3. Reminder Sound ──
        item {
            SettingsCard(icon = Icons.Filled.MusicNote, title = "Reminder Sound") {
                val soundLabel = if (settings.reminderSoundUri.isEmpty()) "Default app tone" else "Custom tone"
                Text(
                    text = soundLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    label = "Vibration",
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration(it) },
                    icon = Icons.Filled.Vibration
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Override silent mode",
                    checked = settings.silentModeOverride,
                    onCheckedChange = { viewModel.toggleSilentModeOverride(it) }
                )
            }
        }

        // ── 4. Notification Controls ──
        item {
            SettingsCard(icon = Icons.Filled.Notifications, title = "Notification Settings") {
                SettingsToggleRow(
                    label = "Heads-up notifications",
                    checked = settings.headsUpEnabled,
                    onCheckedChange = { viewModel.toggleHeadsUp(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Persistent notification",
                    checked = settings.persistentNotification,
                    onCheckedChange = { viewModel.togglePersistentNotification(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Show on lock screen",
                    checked = settings.lockScreenVisibility,
                    onCheckedChange = { viewModel.toggleLockScreenVisibility(it) },
                    icon = Icons.Filled.Visibility
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Snooze Duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                SegmentedControl(
                    options = listOf("5m", "10m", "15m", "30m"),
                    selected = "${settings.snoozeDurationMinutes}m",
                    onSelected = {
                        val mins = it.replace("m", "").toIntOrNull() ?: 10
                        viewModel.updateSnoozeDuration(mins)
                    }
                )
            }
        }

        // ── 5. Focus Target Settings ──
        item {
            SettingsCard(icon = Icons.Filled.Timer, title = "Focus Targets") {
                Text(
                    text = "Window Reminder Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                SegmentedControl(
                    options = listOf("5m", "10m", "15m", "30m"),
                    selected = "${settings.focusWindowReminderMinutes}m",
                    onSelected = {
                        val mins = it.replace("m", "").toIntOrNull() ?: 15
                        viewModel.updateFocusWindowReminder(mins)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    label = "Allow overlapping windows",
                    checked = settings.overlapAllowed,
                    onCheckedChange = { viewModel.toggleOverlapAllowed(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Completion celebration",
                    checked = settings.celebrationAnimationEnabled,
                    onCheckedChange = { viewModel.toggleCelebrationAnimation(it) },
                    icon = Icons.Filled.Celebration
                )
            }
        }

        // ── 6. Appearance ──
        item {
            SettingsCard(icon = Icons.Filled.Palette, title = "Appearance") {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                SegmentedControl(
                    options = listOf("Light", "Dark", "System"),
                    selected = settings.themeMode,
                    onSelected = { viewModel.updateThemeMode(it) },
                    icon = Icons.Filled.DarkMode
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    label = "Dynamic color (Material You)",
                    checked = settings.dynamicColorEnabled,
                    onCheckedChange = { viewModel.toggleDynamicColor(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Reduce animations",
                    checked = settings.reduceAnimations,
                    onCheckedChange = { viewModel.toggleReduceAnimations(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SettingsToggleRow(
                    label = "Compact mode",
                    checked = settings.compactMode,
                    onCheckedChange = { viewModel.toggleCompactMode(it) }
                )
            }
        }

        // ── 7. Data Management ──
        item {
            SettingsCard(icon = Icons.Filled.Storage, title = "Data") {
                SettingsToggleRow(
                    label = "Achievement system",
                    checked = settings.achievementsEnabled,
                    onCheckedChange = { viewModel.toggleAchievements(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                var showClearConfirm by remember { mutableStateOf(false) }
                if (!showClearConfirm) {
                    SettingsActionButton(
                        label = "Clear all hydration history",
                        icon = Icons.Filled.DeleteForever,
                        isDestructive = true,
                        onClick = { showClearConfirm = true }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { showClearConfirm = false },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("Cancel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            onClick = {
                                viewModel.clearAllHistory()
                                showClearConfirm = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("Confirm Clear", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                var showResetConfirm by remember { mutableStateOf(false) }
                if (!showResetConfirm) {
                    SettingsActionButton(
                        label = "Reset all settings",
                        icon = Icons.Filled.Refresh,
                        isDestructive = true,
                        onClick = { showResetConfirm = true }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { showResetConfirm = false },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("Cancel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            onClick = {
                                viewModel.resetAllSettings()
                                showResetConfirm = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("Confirm Reset", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }

        // ── 8. Advanced ──
        item {
            SettingsCard(icon = Icons.Filled.Code, title = "Advanced") {
                SettingsActionButton(
                    label = "Force reschedule reminders",
                    icon = Icons.Filled.Refresh,
                    onClick = { viewModel.forceRescheduleReminders() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.BatteryFull,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Disable battery optimization for reliable reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HydraPing v1.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Build 1  •  Stay hydrated, stay healthy!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────── Reusable components ───────────

@Composable
private fun SettingsCard(
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
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            onDone()
        },
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                animationSpec = tween(250),
                label = "seg_$option"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(250),
                label = "seg_text_$option"
            )
            Surface(
                onClick = { onSelected(option) },
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsActionButton(
    label: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (isDestructive) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.surface
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
