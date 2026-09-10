package com.bitey.app.feature.journal

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalFilteringTest {

    private val sampleTags = listOf(
        TagEntity(tagId = 1, tagName = "Spicy", category = "Taste"),
        TagEntity(tagId = 2, tagName = "Street Food", category = "Ambience"),
        TagEntity(tagId = 3, tagName = "Sweet", category = "Taste")
    )

    private val entry1 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 1,
            title = "Nasi Goreng Gila",
            note = "Extra spicy with lots of fried shallots",
            fullImagePath = "/path/image1.webp",
            thumbnailPath = "/path/thumb1.webp",
            rating = 4.5f,
            isFavorite = true,
            locationName = "Menteng, Jakarta Pusat",
            mealType = MealType.DINNER
        ),
        tags = listOf(sampleTags[0], sampleTags[1]) // Spicy, Street Food
    )

    private val entry2 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 2,
            title = "Pandan Chiffon Cake",
            note = "Soft and fragrant sponge cake",
            fullImagePath = "/path/image2.webp",
            thumbnailPath = "/path/thumb2.webp",
            rating = 5.0f,
            isFavorite = false,
            locationName = "Senopati, Jakarta Selatan",
            mealType = MealType.SNACK
        ),
        tags = listOf(sampleTags[2]) // Sweet
    )

    private val allEntries = listOf(entry1, entry2)

    @Test
    fun filterByFavorite_returnsOnlyFavorites() {
        val filtered = allEntries.filter { it.entry.isFavorite }
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered.first().entry.id)
    }

    @Test
    fun filterByMealType_returnsMatchingMealType() {
        val filteredDinner = allEntries.filter { it.entry.mealType == MealType.DINNER }
        assertEquals(1, filteredDinner.size)
        assertEquals("Nasi Goreng Gila", filteredDinner.first().entry.title)

        val filteredBreakfast = allEntries.filter { it.entry.mealType == MealType.BREAKFAST }
        assertTrue(filteredBreakfast.isEmpty())
    }

    @Test
    fun filterByTag_returnsMatchingEntries() {
        val spicyEntries = allEntries.filter { item -> item.tags.any { it.tagId == 1L } }
        assertEquals(1, spicyEntries.size)
        assertEquals(1L, spicyEntries.first().entry.id)

        val sweetEntries = allEntries.filter { item -> item.tags.any { it.tagId == 3L } }
        assertEquals(1, sweetEntries.size)
        assertEquals(2L, sweetEntries.first().entry.id)
    }

    @Test
    fun filterBySearchQuery_matchesTitle() {
        val query = "Goreng"
        val filtered = allEntries.filter { it.entry.title.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Nasi Goreng Gila", filtered.first().entry.title)
    }

    @Test
    fun filterBySearchQuery_matchesLocation() {
        val query = "Senopati"
        val filtered = allEntries.filter { it.entry.locationName?.contains(query, ignoreCase = true) == true }
        assertEquals(1, filtered.size)
        assertEquals("Pandan Chiffon Cake", filtered.first().entry.title)
    }

    @Test
    fun filterBySearchQuery_matchesTag() {
        val query = "Street"
        val filtered = allEntries.filter { item -> item.tags.any { it.tagName.contains(query, ignoreCase = true) } }
        assertEquals(1, filtered.size)
        assertEquals("Nasi Goreng Gila", filtered.first().entry.title)
    }

    @Test
    fun defaultJournalUiState_initializesWithDefaults() {
        val state = JournalUiState()
        assertTrue(state.entries.isEmpty())
        assertEquals(0, state.totalEntriesCount)
        assertEquals("", state.searchQuery)
        assertFalse(state.isFavoritesOnly)
        assertTrue(state.isGridView)
        assertTrue(state.isLoading)
    }
}
