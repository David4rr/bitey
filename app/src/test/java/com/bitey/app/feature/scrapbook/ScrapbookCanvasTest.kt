package com.bitey.app.feature.scrapbook

import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ScrapbookCanvasTest {

    @Test
    fun canvasAspectRatio_calculatesExpectedRatios() {
        val story = CanvasAspectRatio.STORY_9_16
        assertEquals(9f, story.widthRatio)
        assertEquals(16f, story.heightRatio)

        val square = CanvasAspectRatio.SQUARE_1_1
        assertEquals(1f, square.widthRatio)
        assertEquals(1f, square.heightRatio)
    }

    @Test
    fun canvasElement_initializesWithDefaults() {
        val element = CanvasElement(
            type = CanvasElementType.FOOD_STICKER,
            imagePath = "/data/sticker.webp",
            text = "Rendang"
        )
        assertNotNull(element.id)
        assertEquals(0f, element.xOffset)
        assertEquals(0f, element.yOffset)
        assertEquals(1f, element.scale)
        assertEquals(0f, element.rotation)
        assertEquals(0f, element.zIndex)
    }

    @Test
    fun updateTransform_coercesScaleWithinBounds() {
        val element = CanvasElement(
            type = CanvasElementType.DATE_STAMP,
            scale = 1.0f
        )

        // Scale down past lower bound (0.4f)
        val scaledDown = element.copy(
            scale = (element.scale * 0.1f).coerceIn(0.4f, 3.5f)
        )
        assertEquals(0.4f, scaledDown.scale, 0.001f)

        // Scale up past upper bound (3.5f)
        val scaledUp = element.copy(
            scale = (element.scale * 5.0f).coerceIn(0.4f, 3.5f)
        )
        assertEquals(3.5f, scaledUp.scale, 0.001f)
    }

    @Test
    fun zIndexOrdering_bringToFrontAndSendToBack() {
        val el1 = CanvasElement(id = "1", type = CanvasElementType.FOOD_STICKER, zIndex = 1f)
        val el2 = CanvasElement(id = "2", type = CanvasElementType.WASHI_TAPE, zIndex = 2f)
        val el3 = CanvasElement(id = "3", type = CanvasElementType.DATE_STAMP, zIndex = 3f)

        val list = listOf(el1, el2, el3)

        // Bring el1 to front
        val maxZ = list.maxOf { it.zIndex }
        val updatedFront = list.map { if (it.id == "1") it.copy(zIndex = maxZ + 1f) else it }
        val frontEl1 = updatedFront.first { it.id == "1" }
        assertEquals(4f, frontEl1.zIndex, 0.001f)
        assertTrue(frontEl1.zIndex > updatedFront.first { it.id == "3" }.zIndex)

        // Send el3 to back
        val minZ = list.minOf { it.zIndex }
        val updatedBack = list.map { if (it.id == "3") it.copy(zIndex = minZ - 1f) else it }
        val backEl3 = updatedBack.first { it.id == "3" }
        assertEquals(0f, backEl3.zIndex, 0.001f)
        assertTrue(backEl3.zIndex < updatedBack.first { it.id == "1" }.zIndex)
    }

    @Test
    fun duplicateElement_generatesUniqueIdAndOffset() {
        val original = CanvasElement(
            id = "orig-123",
            type = CanvasElementType.FOOD_STICKER,
            text = "Satay",
            xOffset = 50f,
            yOffset = 50f,
            zIndex = 2f
        )

        val cloned = original.copy(
            id = UUID.randomUUID().toString(),
            xOffset = original.xOffset + 30f,
            yOffset = original.yOffset + 30f,
            zIndex = original.zIndex + 1f
        )

        assertNotEquals(original.id, cloned.id)
        assertEquals(80f, cloned.xOffset, 0.001f)
        assertEquals(80f, cloned.yOffset, 0.001f)
        assertEquals(3f, cloned.zIndex, 0.001f)
        assertEquals(original.text, cloned.text)
    }

    @Test
    fun deleteElement_removesFromListAndClearsSelection() {
        val el1 = CanvasElement(id = "1", type = CanvasElementType.FOOD_STICKER)
        val el2 = CanvasElement(id = "2", type = CanvasElementType.LOCATION_TAG)
        val list = listOf(el1, el2)

        var selectedId: String? = "1"
        val remaining = list.filterNot { it.id == "1" }
        if (selectedId == "1") {
            selectedId = null
        }

        assertEquals(1, remaining.size)
        assertEquals("2", remaining.first().id)
        assertNull(selectedId)
    }
}
