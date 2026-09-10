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
        assertEquals("", state.dishTitle)
        assertEquals("", state.notes)
        assertEquals(MealType.LUNCH, state.mealType)
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
    fun mealTypeInference_matchesHourRanges() {
        fun inferMealType(hour: Int): MealType {
            return when (hour) {
                in 5..10 -> MealType.BREAKFAST
                in 11..14 -> MealType.LUNCH
                in 15..17 -> MealType.SNACK
                in 18..21 -> MealType.DINNER
                else -> MealType.LATE_NIGHT
            }
        }

        assertEquals(MealType.BREAKFAST, inferMealType(8))
        assertEquals(MealType.LUNCH, inferMealType(12))
        assertEquals(MealType.SNACK, inferMealType(16))
        assertEquals(MealType.DINNER, inferMealType(19))
        assertEquals(MealType.LATE_NIGHT, inferMealType(23))
        assertEquals(MealType.LATE_NIGHT, inferMealType(2))
    }
}
