package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlastDarkColorScheme = darkColorScheme(
    primary = KronDarkAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0E7490),
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E293B),
    tertiary = KronDarkPositive,
    onTertiary = Color.Black,
    background = KronDarkBg,
    onBackground = KronDarkText,
    surface = KronDarkCard,
    onSurface = KronDarkText,
    surfaceVariant = KronDarkBgSoft,
    onSurfaceVariant = KronDarkMuted
)

private val BlastLightColorScheme = lightColorScheme(
    primary = KronLightAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF08A),
    onPrimaryContainer = Color(0xFF854D0E),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    tertiary = KronLightPositive,
    onTertiary = Color.White,
    background = KronLightBg,
    onBackground = KronLightText,
    surface = KronLightCard,
    onSurface = KronLightText,
    surfaceVariant = KronLightBgSoft,
    onSurfaceVariant = KronLightMuted
)

@Composable
fun BlastTheBlocksTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BlastDarkColorScheme else BlastLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
