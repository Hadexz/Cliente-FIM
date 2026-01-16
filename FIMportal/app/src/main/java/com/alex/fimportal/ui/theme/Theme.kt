package com.alex.fimportal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Forced Dark Mode Scheme
private val DarkColorScheme = darkColorScheme(
    primary = FimBlue,
    secondary = FimPink,
    tertiary = FimTeal,
    background = FimBackground,
    surface = FimSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = FimDark,
    onSurface = FimDark,
)

@Composable
fun FIMportalTheme(
    accentColor: Color = FimBlue, // Default if not provided
    content: @Composable () -> Unit
) {
    // Construct dynamic scheme based on the selected accent
    val dynamicColorScheme = DarkColorScheme.copy(
        primary = accentColor,
        secondary = accentColor, // Optional: make secondary match or complement
        tertiary = FimTeal // Keep distinct tertiary
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = dynamicColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Dark Mode forced
        }
    }

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        content = content
    )
}