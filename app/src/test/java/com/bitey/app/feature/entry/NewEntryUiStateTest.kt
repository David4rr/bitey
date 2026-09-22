package com.bitey.app.feature.entry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewEntryUiStateTest {

    @Test
    fun defaultState_hasExpectedDefaults() {
        val state = NewEntryUiState()
        assertNull(state.selectedImage)
        assertNull(state.sticker)
        assertFalse(state.isLoading)
        assertFalse(state.isCameraActive)
        assertFalse(state.isSegmenting)
        assertEquals("", state.segmentationStatusText)
        assertEquals(StickerStyle.AI_SEGMENTED, state.stickerStyle)
        assertFalse(state.showFallbackDialog)
        assertNull(state.errorMessage)
    }

    @Test
    fun stickerStyles_haveCorrectHumanReadableLabels() {
        assertEquals("AI Subject Cutout", StickerStyle.AI_SEGMENTED.label)
        assertEquals("Circular Plate Badge", StickerStyle.CIRCULAR_BADGE.label)
        assertEquals("Polaroid Rounded Tile", StickerStyle.ROUNDED_TILE.label)
    }

    @Test
    fun stateCopy_updatesStickerAndStyleCorrectly() {
        val initial = NewEntryUiState()
        val updated = initial.copy(
            isSegmenting = true,
            segmentationStatusText = "Extracting subject...",
            stickerStyle = StickerStyle.CIRCULAR_BADGE
        )

        assertTrue(updated.isSegmenting)
        assertEquals("Extracting subject...", updated.segmentationStatusText)
        assertEquals(StickerStyle.CIRCULAR_BADGE, updated.stickerStyle)
    }

    @Test
    fun stateCopy_opensAndClosesFallbackDialog() {
        val initial = NewEntryUiState()
        val withDialog = initial.copy(showFallbackDialog = true)
        assertTrue(withDialog.showFallbackDialog)

        val closed = withDialog.copy(showFallbackDialog = false)
        assertFalse(closed.showFallbackDialog)
    }

    @Test
    fun candidateStickerItem_supportsCustomFoodAndMealTypePerImage() {
        val item1 = com.bitey.app.feature.camera.CandidateStickerItem(
            originalFilePath = "/tmp/dish1.jpg",
            stickerFilePath = "/tmp/dish1_sticker.png",
            label = "Tonkotsu Ramen",
            mealType = com.bitey.app.core.database.model.MealType.FOOD
        )
        val item2 = com.bitey.app.feature.camera.CandidateStickerItem(
            originalFilePath = "/tmp/dish2.jpg",
            stickerFilePath = "/tmp/dish2_sticker.png",
            label = "Matcha Latte",
            mealType = com.bitey.app.core.database.model.MealType.DRINK
        )
        val item3 = com.bitey.app.feature.camera.CandidateStickerItem(
            originalFilePath = "/tmp/dish3.jpg",
            stickerFilePath = "/tmp/dish3_sticker.png",
            label = "Matcha Mochi",
            mealType = com.bitey.app.core.database.model.MealType.OTHER
        )

        assertEquals("Tonkotsu Ramen", item1.label)
        assertEquals(com.bitey.app.core.database.model.MealType.FOOD, item1.mealType)

        assertEquals("Matcha Latte", item2.label)
        assertEquals(com.bitey.app.core.database.model.MealType.DRINK, item2.mealType)

        assertEquals("Matcha Mochi", item3.label)
        assertEquals(com.bitey.app.core.database.model.MealType.OTHER, item3.mealType)
    }

    @Test
    fun stateCopy_updatesCandidateListWithIndividualFoodAndMealTypes() {
        val candidate1 = com.bitey.app.feature.camera.CandidateStickerItem(
            id = "item-1",
            originalFilePath = "/path/1.jpg",
            stickerFilePath = "/path/1.png",
            label = "Food",
            mealType = com.bitey.app.core.database.model.MealType.FOOD
        )
        val candidate2 = com.bitey.app.feature.camera.CandidateStickerItem(
            id = "item-2",
            originalFilePath = "/path/2.jpg",
            stickerFilePath = "/path/2.png",
            label = "Drink",
            mealType = com.bitey.app.core.database.model.MealType.DRINK
        )

        val state = NewEntryUiState(candidates = listOf(candidate1, candidate2))

        val updatedCandidates = state.candidates.map {
            if (it.id == "item-1") it.copy(label = "Truffle Fries", mealType = com.bitey.app.core.database.model.MealType.OTHER)
            else it
        }

        val updatedState = state.copy(candidates = updatedCandidates)

        assertEquals("Truffle Fries", updatedState.candidates[0].label)
        assertEquals(com.bitey.app.core.database.model.MealType.OTHER, updatedState.candidates[0].mealType)
        assertEquals("Drink", updatedState.candidates[1].label)
        assertEquals(com.bitey.app.core.database.model.MealType.DRINK, updatedState.candidates[1].mealType)
    }

    @Test
    fun multiDishMode_cropsStickers_withFallback() {
        val files = listOf(java.io.File("/tmp/dish1.jpg"), java.io.File("/tmp/dish2.jpg"))
        val candidates = files.mapIndexed { idx, file ->
            com.bitey.app.feature.camera.CandidateStickerItem(
                originalFilePath = file.absolutePath,
                stickerFilePath = "/tmp/sticker_${idx + 1}.webp",
                label = "Food ${idx + 1}",
                mealType = com.bitey.app.core.database.model.MealType.FOOD
            )
        }
        val state = NewEntryUiState(
            step = com.bitey.app.feature.camera.CaptureFlowStep.REVIEW,
            candidates = candidates,
            isStickerMode = true
        )

        assertEquals(2, state.candidates.size)
        assertTrue(state.isStickerMode)
        assertEquals("/tmp/sticker_1.webp", state.candidates[0].stickerFilePath)
        assertEquals("/tmp/sticker_2.webp", state.candidates[1].stickerFilePath)
    }
}

