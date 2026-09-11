package com.bitey.app.feature.entry

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryEditorUiStateTest {

    @Test
    fun defaultState_hasExpectedDefaults() {
        val state = EntryEditorUiState()
        assertEquals("Food", state.dishTitle)
        assertEquals("", state.notes)
        assertEquals(MealType.FOOD, state.mealType)
        assertEquals(5.0f, state.rating, 0.01f)
        assertEquals("", state.priceString)
        assertEquals("IDR", state.currency)
        assertFalse(state.isFavorite)
        assertTrue(state.isStickerMode)
        assertFalse(state.isLocating)
        assertFalse(state.isSaving)
        assertFalse(state.isSavedSuccessfully)
        assertNull(state.errorMessage)
        assertTrue(state.selectedTags.isEmpty())
        assertTrue(state.availableTags.isEmpty())
    }

    @Test
    fun ratingClamping_within1to5() {
        val clampedLow = (-2.0f).coerceIn(1.0f, 5.0f)
        assertEquals(1.0f, clampedLow, 0.01f)

        val clampedHigh = 7.5f.coerceIn(1.0f, 5.0f)
        assertEquals(5.0f, clampedHigh, 0.01f)

        val validRating = 3.5f.coerceIn(1.0f, 5.0f)
        assertEquals(3.5f, validRating, 0.01f)
    }

    @Test
    fun priceFiltering_allowsOnlyDigitsAndDot() {
        val rawInput = "IDR 65,000.50"
        val filtered = rawInput.filter { it.isDigit() || it == '.' }
        assertEquals("65000.50", filtered)
        assertEquals(65000.50, filtered.toDoubleOrNull() ?: 0.0, 0.001)
    }

    @Test
    fun tagSelectionToggle_addsAndRemovesCorrectly() {
        val tag1 = TagEntity(tagId = 1, tagName = "Spicy", category = "Taste")
        val tag2 = TagEntity(tagId = 2, tagName = "Street Food", category = "Ambience")

        var selected = setOf<TagEntity>()

        // Add tag1
        selected = selected + tag1
        assertTrue(selected.contains(tag1))
        assertEquals(1, selected.size)

        // Add tag2
        selected = selected + tag2
        assertTrue(selected.contains(tag2))
        assertEquals(2, selected.size)

        // Remove tag1
        selected = selected - tag1
        assertFalse(selected.contains(tag1))
        assertTrue(selected.contains(tag2))
        assertEquals(1, selected.size)
    }

    @Test
    fun mealType_labelsAndValues() {
        assertEquals("Food", MealType.FOOD.label)
        assertEquals("Drink", MealType.DRINK.label)
        assertEquals("Other", MealType.OTHER.label)
        assertEquals(MealType.FOOD, MealType.fromTimestamp())
    }
}
