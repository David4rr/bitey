package com.bitey.app.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BiteyOrange,
    onPrimary = StickerDieCutWhite,
    primaryContainer = BiteyOrangeVariant,
    onPrimaryContainer = StickerDieCutWhite,
    secondary = BiteyMint,
    onSecondary = StickerDieCutWhite,
    background = MinimalistLightBackground,
    onBackground = InkPrimary,
    surface = MinimalistLightSurface,
    onSurface = InkPrimary,
    surfaceVariant = MinimalistLightSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = MinimalistLightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = BiteyOrange,
    onPrimary = StickerDieCutWhite,
    primaryContainer = BiteyOrangeVariant,
    onPrimaryContainer = StickerDieCutWhite,
    secondary = BiteyMint,
    onSecondary = StickerDieCutWhite,
    background = MinimalistDarkBackground,
    onBackground = DarkInkPrimary,
    surface = MinimalistDarkSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = MinimalistDarkSurfaceVariant,
    onSurfaceVariant = DarkInkSecondary,
    outline = MinimalistDarkBorder
)

@Composable
fun BiteyTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val themeColors = if (isDark) DarkMinimalistColors else LightMinimalistColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalNeumorphicTheme provides themeColors,
        LocalThemeMode provides themeMode,
        LocalOnThemeModeChanged provides onThemeModeChanged,
        androidx.compose.material3.LocalContentColor provides themeColors.inkPrimary
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BiteyTypography,
            content = content
        )
    }
}
