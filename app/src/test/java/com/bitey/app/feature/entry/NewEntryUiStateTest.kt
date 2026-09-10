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
}
