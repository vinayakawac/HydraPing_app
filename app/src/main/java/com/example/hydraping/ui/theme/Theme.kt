package com.example.hydraping.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HydraBlueLight,
    onPrimary = Dark95,
    primaryContainer = HydraBlue80,
    onPrimaryContainer = HydraBlue10,
    secondary = HydraBlue30,
    onSecondary = Dark95,
    secondaryContainer = Dark70,
    onSecondaryContainer = HydraBlue20,
    tertiary = SuccessGreenLight,
    onTertiary = Dark95,
    background = Dark95,
    onBackground = Grey15,
    surface = Dark90,
    onSurface = Grey15,
    surfaceVariant = Dark80,
    onSurfaceVariant = Grey40,
    surfaceContainerHighest = Dark70,
    error = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = HydraBlue,
    onPrimary = White,
    primaryContainer = HydraBlue10,
    onPrimaryContainer = HydraBlue90,
    secondary = HydraBlueDark,
    onSecondary = White,
    secondaryContainer = HydraBlue20,
    onSecondaryContainer = HydraBlue90,
    tertiary = SuccessGreen,
    onTertiary = White,
    background = Grey10,
    onBackground = Dark90,
    surface = White,
    onSurface = Dark90,
    surfaceVariant = Grey15,
    onSurfaceVariant = Dark60,
    surfaceContainerHighest = Grey20,
    error = Color(0xFFB00020)
)

@Composable
fun HydraPingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HydraPingTypography,
        content = content
    )
}