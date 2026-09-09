package com.bitey.app.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
    background = SoftBackground,
    onBackground = InkPrimary,
    surface = NeumorphicSurface,
    onSurface = InkPrimary,
    surfaceVariant = SoftBackground,
    onSurfaceVariant = InkSecondary
)

@Composable
fun BiteyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BiteyTypography,
        content = content
    )
}
