package com.bitey.app.feature.profile

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileNavigationTest {

    @Test
    fun bottomNavItems_containsOnlyJournalAndProfileInPill() {
        val items = Screen.bottomNavItems

        // The navbar pill contains only Journal and Profile
        assertEquals(2, items.size)
        assertEquals(Screen.Journal, items[0])
        assertEquals(Screen.Profile, items[1])

        // Camera (NewEntry), Footprints, FateTable, Scrapbook are not inside the pill
        assertFalse(items.contains(Screen.NewEntry))
        assertFalse(items.contains(Screen.Footprints))
        assertFalse(items.contains(Screen.FateTable))
        assertFalse(items.contains(Screen.Scrapbook))
    }

    @Test
    fun profileScreen_routesAndPropertiesConfigured() {
        assertEquals("profile", Screen.Profile.route)
        assertEquals("Profile", Screen.Profile.title)
    }

    @Test
    fun profileUiState_computesTasteStatsAccurately() {
        val tagSpicy = TagEntity(tagId = 1, tagName = "Spicy", category = "Taste")
        val tagNoodles = TagEntity(tagId = 2, tagName = "Noodles", category = "Type")

        val entry1 = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 1,
                title = "Dan Dan Noodles",
                fullImagePath = "/path1.webp",
                thumbnailPath = "/path1.webp",
                mealType = MealType.FOOD,
                isFavorite = true,
                latitude = -6.2,
                longitude = 106.8,
                isStickerMode = true
            ),
            tags = listOf(tagSpicy, tagNoodles)
        )

        val entry2 = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 2,
                title = "Spicy Wontons",
                fullImagePath = "/path2.webp",
                thumbnailPath = "/path2.webp",
                mealType = MealType.FOOD,
                isFavorite = false,
                latitude = null,
                longitude = null,
                isStickerMode = false
            ),
            tags = listOf(tagSpicy)
        )

        val entry3 = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 3,
                title = "Matcha Croissant",
                fullImagePath = "/path3.webp",
                thumbnailPath = "/path3.webp",
                mealType = MealType.DRINK,
                isFavorite = true,
                latitude = -6.19,
                longitude = 106.82,
                isStickerMode = true
            ),
            tags = emptyList()
        )

        val entries = listOf(entry1, entry2, entry3)

        val totalCount = entries.size
        val favCount = entries.count { it.entry.isFavorite }
        val spotsCount = entries.count { it.entry.latitude != null && it.entry.longitude != null }
        val topMeal = entries.groupBy { it.entry.mealType }.maxByOrNull { it.value.size }?.key
        val topTags = entries.flatMap { it.tags }
            .groupBy { it.tagName }
            .toList()
            .sortedByDescending { it.second.size }
            .map { it.first }
        val stickerCount = entries.count { it.entry.isStickerMode }

        val state = ProfileUiState(
            totalEntriesCount = totalCount,
            favoriteEntriesCount = favCount,
            spotsWithLocationCount = spotsCount,
            topMealType = topMeal,
            favoriteTagNames = topTags,
            stickerModeEntriesCount = stickerCount,
            recentEntries = entries.take(6)
        )

        assertEquals(3, state.totalEntriesCount)
        assertEquals(2, state.favoriteEntriesCount)
        assertEquals(2, state.spotsWithLocationCount)
        assertEquals(MealType.FOOD, state.topMealType)
        assertEquals(listOf("Spicy", "Noodles"), state.favoriteTagNames)
        assertEquals(2, state.stickerModeEntriesCount)
        assertEquals(3, state.recentEntries.size)
    }
}
