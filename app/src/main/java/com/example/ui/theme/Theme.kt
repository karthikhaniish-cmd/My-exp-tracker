package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = FintechGoldPrimary,
    onPrimary = Color(0xFF1E1500),
    primaryContainer = Color(0xFF3D2E0B),
    onPrimaryContainer = FintechGoldLight,
    secondary = FintechGoldLight,
    onSecondary = Color(0xFF221A05),
    secondaryContainer = FintechSurfaceElevatedDark,
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = FintechEmerald,
    onTertiary = Color.White,
    background = FintechBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = FintechSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = FintechSurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = FintechBorderDark,
    outlineVariant = Color(0xFF1A2A40),
    error = FintechCoral,
    onError = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = FintechGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBF4E4),
    onPrimaryContainer = Color(0xFF4A3805),
    secondary = FintechGoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = TextPrimaryLight,
    tertiary = FintechEmerald,
    onTertiary = Color.White,
    background = FintechBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = FintechSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = TextSecondaryLight,
    outline = FintechBorderLight,
    outlineVariant = Color(0xFFCBD5E1),
    error = FintechCoral,
    onError = Color.White
)

@Composable
fun SmartExpenseTrackerTheme(
    darkTheme: Boolean = true, // Default to sleek fintech dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
