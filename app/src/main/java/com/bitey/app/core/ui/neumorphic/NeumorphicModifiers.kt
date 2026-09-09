package com.bitey.app.core.ui.neumorphic

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.NeumorphicDarkShadow
import com.bitey.app.core.ui.theme.NeumorphicLightShadow
import com.bitey.app.core.ui.theme.NeumorphicSurface
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.core.ui.theme.StickerSoftShadow

/**
 * Renders dual soft shadows (top-left light, bottom-right dark) for tactile Neumorphism.
 */
fun Modifier.neumorphicRaised(
    cornerRadius: Dp = 16.dp,
    lightShadowColor: Color = NeumorphicLightShadow.copy(alpha = 0.9f),
    darkShadowColor: Color = NeumorphicDarkShadow.copy(alpha = 0.45f),
    shadowOffset: Dp = 4.dp,
    blurRadius: Dp = 8.dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = shadowOffset.toPx()
    val blurPx = blurRadius.toPx()

    drawIntoCanvas { canvas ->
        // 1. Top-left light shadow
        val lightPaint = Paint().apply {
            color = lightShadowColor
            asFrameworkPaint().apply {
                isDither = true
                isAntiAlias = true
                if (blurPx > 0f) {
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
            }
        }
        canvas.drawRoundRect(
            left = -offsetPx,
            top = -offsetPx,
            right = size.width - offsetPx,
            bottom = size.height - offsetPx,
            radiusX = cornerPx,
            radiusY = cornerPx,
            paint = lightPaint
        )

        // 2. Bottom-right dark shadow
        val darkPaint = Paint().apply {
            color = darkShadowColor
            asFrameworkPaint().apply {
                isDither = true
                isAntiAlias = true
                if (blurPx > 0f) {
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
            }
        }
        canvas.drawRoundRect(
            left = offsetPx,
            top = offsetPx,
            right = size.width + offsetPx,
            bottom = size.height + offsetPx,
            radiusX = cornerPx,
            radiusY = cornerPx,
            paint = darkPaint
        )
    }
}

/**
 * Standard tactile raised card with rounded corners and surface background.
 */
fun Modifier.neumorphicCard(
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = NeumorphicSurface,
    elevation: Dp = 4.dp,
    blurRadius: Dp = 8.dp
): Modifier = this
    .neumorphicRaised(
        cornerRadius = cornerRadius,
        shadowOffset = elevation,
        blurRadius = blurRadius
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)

/**
 * Applies a die-cut sticker border with subtle drop shadow for food elements.
 */
fun Modifier.dieCutStickerEffect(
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 4.dp,
    strokeColor: Color = StickerDieCutWhite,
    shadowColor: Color = StickerSoftShadow
): Modifier = this
    .drawBehind {
        val cornerPx = cornerRadius.toPx()
        val blurPx = 6.dp.toPx()
        val offsetPx = 3.dp.toPx()

        drawIntoCanvas { canvas ->
            val shadowPaint = Paint().apply {
                color = shadowColor
                asFrameworkPaint().apply {
                    isDither = true
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
            }
            canvas.drawRoundRect(
                left = 0f,
                top = offsetPx,
                right = size.width,
                bottom = size.height + offsetPx,
                radiusX = cornerPx,
                radiusY = cornerPx,
                paint = shadowPaint
            )
        }
    }
    .clip(RoundedCornerShape(cornerRadius))
    .border(strokeWidth, strokeColor, RoundedCornerShape(cornerRadius))
