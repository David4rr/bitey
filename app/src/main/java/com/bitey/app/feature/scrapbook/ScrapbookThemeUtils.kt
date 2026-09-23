package com.bitey.app.feature.scrapbook

import androidx.compose.ui.graphics.Color
import com.bitey.app.core.ui.theme.StickerDieCutWhite

/**
 * Utility for determining optimal contrast and text colors for scrapbook canvas backgrounds.
 */
object ScrapbookThemeUtils {

    fun isDarkCanvas(colorHex: Long): Boolean {
        val r = ((colorHex shr 16) and 0xFF) / 255f
        val g = ((colorHex shr 8) and 0xFF) / 255f
        val b = (colorHex and 0xFF) / 255f
        val luminance = 0.299f * r + 0.587f * g + 0.114f * b
        return luminance < 0.5f
    }

    fun getCanvasPrimaryTextColor(colorHex: Long): Color {
        return if (isDarkCanvas(colorHex)) StickerDieCutWhite else Color(0xFF261C1A)
    }

    fun getCanvasSecondaryTextColor(colorHex: Long): Color {
        return if (isDarkCanvas(colorHex)) Color(0xB3FFFFFF) else Color(0x99261C1A)
    }
}
