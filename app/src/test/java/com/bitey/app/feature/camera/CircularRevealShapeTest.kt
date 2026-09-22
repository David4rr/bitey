package com.bitey.app.feature.camera

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.bitey.app.feature.camera.component.CircularRevealShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CircularRevealShapeTest {

    private val density = Density(1f)
    private val layoutDirection = LayoutDirection.Ltr
    private val size = Size(1000f, 2000f)

    @Test
    fun zeroProgress_returnsZeroRectangle() {
        val shape = CircularRevealShape(progress = 0f)
        val outline = shape.createOutline(size, layoutDirection, density)
        assertTrue(outline is Outline.Rectangle)
        assertEquals(Rect.Zero, (outline as Outline.Rectangle).rect)
    }

    @Test
    fun fullProgress_returnsFullRectangle() {
        val shape = CircularRevealShape(progress = 1f)
        val outline = shape.createOutline(size, layoutDirection, density)
        assertTrue(outline is Outline.Rectangle)
        assertEquals(Rect(Offset.Zero, size), (outline as Outline.Rectangle).rect)
    }

    @Test
    fun partialProgress_returnsRoundedOutlineWithExactRadius() {
        val center = Offset(500f, 1800f)
        val shape = CircularRevealShape(progress = 0.5f, center = center)
        val outline = shape.createOutline(size, layoutDirection, density)
        assertTrue(outline is Outline.Rounded)
        val rounded = outline as Outline.Rounded
        assertEquals(rounded.roundRect.width, rounded.roundRect.height, 0.001f)
        assertEquals(rounded.roundRect.width / 2f, rounded.roundRect.topLeftCornerRadius.x, 0.001f)
    }
}
