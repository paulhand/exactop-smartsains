package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = TextDark,
    primaryContainer = SurfaceDarkElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = BrightViolet,
    onSecondary = TextWhite,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = BrightViolet,
    tertiary = AcidLime,
    onTertiary = TextDark,
    background = DarkCanvas,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = TextWhite,
    outline = SurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
