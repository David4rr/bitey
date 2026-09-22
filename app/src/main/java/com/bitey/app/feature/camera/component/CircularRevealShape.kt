package com.bitey.app.feature.camera.component

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max

/**
 * Hardware-accelerated circular reveal shape centered at [center].
 * Uses Outline.Rounded for zero-allocation, GPU-direct clip rendering.
 * Animates from [center] outwards as [progress] moves from 0f to 1f.
 */
class CircularRevealShape(
    val progress: Float,
    val center: Offset? = null
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (progress <= 0f) {
            return Outline.Rectangle(Rect.Zero)
        }
        if (progress >= 1f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }

        val c = center ?: Offset(size.width / 2f, size.height - with(density) { 60.dp.toPx() })
        val maxRadius = hypot(
            max(c.x, size.width - c.x),
            max(c.y, size.height - c.y)
        )
        val currentRadius = maxRadius * progress

        val roundRect = RoundRect(
            left = c.x - currentRadius,
            top = c.y - currentRadius,
            right = c.x + currentRadius,
            bottom = c.y + currentRadius,
            cornerRadius = CornerRadius(currentRadius, currentRadius)
        )
        return Outline.Rounded(roundRect)
    }
}
