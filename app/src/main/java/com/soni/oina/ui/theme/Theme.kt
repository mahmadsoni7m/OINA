package com.soni.oina.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.soni.oina.settings.ThemeMode

private val OinaDarkColorScheme = darkColorScheme(
    primary = OinaAccent,
    secondary = OinaAccentSoft,
    background = OinaBlack,
    surface = OinaSurfaceDark,
    surfaceVariant = OinaSurfaceDarkElevated,
    onBackground = OinaOnDark,
    onSurface = OinaOnDark,
    onPrimary = OinaBlack,
    error = OinaError
)

private val OinaLightColorScheme = lightColorScheme(
    primary = OinaAccentLight,
    secondary = OinaAccentLight,
    background = OinaSurfaceLight,
    surface = OinaSurfaceLightElevated,
    surfaceVariant = OinaSurfaceLight,
    onBackground = OinaOnLight,
    onSurface = OinaOnLight,
    onPrimary = OinaWhite,
    error = OinaError
)

/**
 * Resolves the effective dark/light choice from user preference + system setting.
 */
@Composable
fun isOinaDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun OinaTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = isOinaDarkTheme(themeMode)
    val colorScheme = if (useDark) OinaDarkColorScheme else OinaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OinaTypography,
        content = content
    )
}
