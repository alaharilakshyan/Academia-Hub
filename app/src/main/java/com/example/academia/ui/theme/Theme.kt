package com.example.academia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VividViolet,
    secondary = BrightCyan,
    tertiary = SoftCyanAccent,
    background = SlateBackground,
    surface = SlateSurface,
    surfaceVariant = SlateSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextLightMuted,
    error = StatusRejected
)

private val LightColorScheme = lightColorScheme(
    primary = VividViolet,
    secondary = BrightCyan,
    tertiary = SoftCyanAccent,
    background = BrightBackground,
    surface = PureWhite,
    surfaceVariant = PureWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextMuted,
    error = StatusRejected
)

@Composable
fun AcademiaTheme(
    darkTheme: Boolean = false, // Default to stunning soft Light UI theme
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb() // Transparent for Edge-To-Edge effects
            // Always set light status bar since we forced Light theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true 
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}