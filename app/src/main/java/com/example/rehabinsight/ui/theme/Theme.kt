package com.example.rehabinsight.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = RehabPrimaryLight,
    onPrimary = Color.White,
    secondary = RehabPrimary,
    tertiary = RehabPrimaryLight,
    background = Color(0xFF191A30),
    surface = Color(0xFF222443),
    onBackground = Color(0xFFEDEFFF),
    onSurface = Color(0xFFEDEFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = RehabPrimary,
    onPrimary = Color.White,
    primaryContainer = RehabPrimarySoft,
    onPrimaryContainer = RehabTextPrimary,
    secondary = RehabPrimaryDeep,
    onSecondary = Color.White,
    secondaryContainer = RehabPrimarySoft,
    onSecondaryContainer = RehabTextPrimary,
    tertiary = RehabPrimaryLight,
    background = RehabBackgroundBottom,
    onBackground = RehabTextPrimary,
    surface = RehabSurface,
    onSurface = RehabTextPrimary,
    surfaceVariant = RehabSurfaceSoft,
    onSurfaceVariant = RehabTextSecondary,
    surfaceTint = RehabPrimary,
    surfaceContainerLowest = RehabSurface,
    surfaceContainerLow = RehabSurface,
    surfaceContainer = RehabSurface,
    surfaceContainerHigh = RehabSurface,
    surfaceContainerHighest = RehabSurfaceSoft,
    outline = RehabDivider,
    outlineVariant = RehabDivider,
    error = RehabError,
    onError = Color.White,
    errorContainer = RehabErrorSoft,
    onErrorContainer = RehabError
)

// Generous corner radii everywhere - no sharp edges
private val RehabShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = RehabShapes,
        content = content
    )
}
