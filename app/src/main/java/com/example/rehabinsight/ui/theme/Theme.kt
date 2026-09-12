package com.example.rehabinsight.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RehabBlue,
    onPrimary = Color.White,
    secondary = RehabIndigo,
    tertiary = TaskBlueConnection,
    background = Color(0xFF14162B),
    surface = Color(0xFF1D2040),
    onBackground = Color(0xFFEDEFFF),
    onSurface = Color(0xFFEDEFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = RehabBlue,
    onPrimary = Color.White,
    primaryContainer = RehabBackgroundBottom,
    onPrimaryContainer = RehabTextPrimary,
    secondary = RehabIndigo,
    onSecondary = Color.White,
    tertiary = TaskBlueConnection,
    background = RehabBackgroundTop,
    onBackground = RehabTextPrimary,
    surface = RehabSurface,
    onSurface = RehabTextPrimary,
    surfaceVariant = RehabBackgroundBottom,
    onSurfaceVariant = RehabTextSecondary,
    outline = RehabDivider
)

@Composable
fun RehabInsightTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
