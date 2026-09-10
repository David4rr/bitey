package com.bitey.app.core.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    AUTO("Auto (System)"),
    LIGHT("Light"),
    DARK("Dark")
}

data class MinimalistThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val isDark: Boolean,
    val lightShadow: Color = Color.Transparent,
    val darkShadow: Color = Color.Transparent
)

typealias NeumorphicThemeColors = MinimalistThemeColors

val LightMinimalistColors = MinimalistThemeColors(
    background = MinimalistLightBackground,
    surface = MinimalistLightSurface,
    surfaceVariant = MinimalistLightSurfaceVariant,
    border = MinimalistLightBorder,
    inkPrimary = InkPrimary,
    inkSecondary = InkSecondary,
    inkMuted = InkMuted,
    isDark = false
)

val DarkMinimalistColors = MinimalistThemeColors(
    background = MinimalistDarkBackground,
    surface = MinimalistDarkSurface,
    surfaceVariant = MinimalistDarkSurfaceVariant,
    border = MinimalistDarkBorder,
    inkPrimary = DarkInkPrimary,
    inkSecondary = DarkInkSecondary,
    inkMuted = DarkInkMuted,
    isDark = true
)

val LightNeumorphicColors = LightMinimalistColors
val DarkNeumorphicColors = DarkMinimalistColors

val LocalNeumorphicTheme = staticCompositionLocalOf { LightMinimalistColors }
val LocalMinimalistTheme = LocalNeumorphicTheme

val LocalThemeMode = compositionLocalOf { ThemeMode.AUTO }
val LocalOnThemeModeChanged = compositionLocalOf<(ThemeMode) -> Unit> { {} }

object BiteyThemeDefaults {
    val theme: MinimalistThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMinimalistTheme.current

    val neumorphic: NeumorphicThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNeumorphicTheme.current
}

object ThemePreferences {
    private const val PREFS_NAME = "bitey_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
