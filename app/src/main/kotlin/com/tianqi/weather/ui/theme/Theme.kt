package com.tianqi.weather.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.tianqi.weather.data.AccentTone

// 天气天气 · 双主题（v0.0.5）：isLight 决定纸面/磷光两套色板与 M3 colorScheme
private fun darkScheme(p: TianQiPalette) = darkColorScheme(
    primary = p.mint,
    onPrimary = Color(0xFF001500),
    primaryContainer = Color(0xFF003300),
    onPrimaryContainer = Color(0xFFB0FFB0),
    secondary = p.cyan,
    onSecondary = Color(0xFF003333),
    background = p.bg,
    onBackground = p.text,
    surface = p.surface,
    onSurface = p.text,
    surfaceVariant = p.card,
    onSurfaceVariant = p.textSecondary,
    outline = p.cardBorder,
    outlineVariant = p.cardBorder,
    error = p.red,
    onError = Color(0xFF3F0B0B),
    errorContainer = Color(0xFF461313),
    onErrorContainer = Color(0xFFFFDAD4),
)

private fun lightScheme(p: TianQiPalette) = lightColorScheme(
    primary = p.mint,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEAE0),
    onPrimaryContainer = Color(0xFF06382E),
    secondary = p.cyan,
    onSecondary = Color.White,
    background = p.bg,
    onBackground = p.text,
    surface = p.surface,
    onSurface = p.text,
    surfaceVariant = Color(0xFFE8ECEF),
    onSurfaceVariant = p.textSecondary,
    outline = p.cardBorder,
    outlineVariant = p.cardBorder,
    error = p.red,
    onError = Color.White,
    errorContainer = Color(0xFFFBE7E7),
    onErrorContainer = Color(0xFF3D0B0B),
)

@Composable
fun TianQiWeatherTheme(
    isLight: Boolean = false,
    accentTone: AccentTone = AccentTone.STANDARD,
    content: @Composable () -> Unit,
) {
    val palette = when {
        isLight && accentTone == AccentTone.SOFT -> TianQiLightSoftAccentPalette
        isLight -> TianQiLightPalette
        accentTone == AccentTone.SOFT -> TianQiDarkSoftAccentPalette
        else -> TianQiDarkPalette
    }
    CompositionLocalProvider(LocalTianQiPalette provides palette) {
        MaterialTheme(
            colorScheme = if (isLight) lightScheme(palette) else darkScheme(palette),
            typography = TianQiTypography,
            content = content,
        )
    }
}
