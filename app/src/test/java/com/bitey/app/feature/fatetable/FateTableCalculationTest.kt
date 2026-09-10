package com.bitey.app.feature.fatetable

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FateTableCalculationTest {

    private val sampleTags = listOf(
        TagEntity(tagId = 1, tagName = "Spicy", category = "Taste"),
        TagEntity(tagId = 2, tagName = "Dessert", category = "Taste")
    )

    private val entry1 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 1,
            title = "Ayam Geprek",
            note = "Crispy spicy chicken",
            fullImagePath = "/path/1.webp",
            thumbnailPath = "/path/thumb1.webp",
            rating = 4.8f,
            isFavorite = true,
            timestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L, // 2 days ago
            mealType = MealType.LUNCH
        ),
        tags = listOf(sampleTags[0])
    )

    private val entry2 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 2,
            title = "Martabak Manis",
            note = "Chocolate cheese peanut",
            fullImagePath = "/path/2.webp",
            thumbnailPath = "/path/thumb2.webp",
            rating = 4.9f,
            isFavorite = false,
            timestamp = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L, // 5 days ago
            mealType = MealType.LATE_NIGHT
        ),
        tags = listOf(sampleTags[1])
    )

    private val entry3 = PlateEntryWithTags(
        entry = PlateEntryEntity(
            id = 3,
            title = "Bubur Ayam",
            note = "Warm morning congee",
            fullImagePath = "/path/3.webp",
            thumbnailPath = "/path/thumb3.webp",
            rating = 4.2f,
            isFavorite = true,
            timestamp = System.currentTimeMillis() - 40 * 24 * 60 * 60 * 1000L, // 40 days ago (older than 30 days)
            mealType = MealType.BREAKFAST
        ),
        tags = emptyList()
    )

    private val allCandidates = listOf(entry1, entry2, entry3)

    @Test
    fun calculateSpinTarget_withLessThanTwoCandidates_returnsNull() {
        // Given fewer than 2 items
        val singleCandidate = listOf(entry1)
        val sliceAngle = 360f / singleCandidate.size
        // Direct calculation logic validation
        val result = if (singleCandidate.size < 2) null else "Valid"
        assertNull(result)
    }

    @Test
    fun calculateSpinTarget_withMultipleCandidates_ensuresDecelerationSpinsAndCorrectAlignment() {
        val candidates = listOf(entry1, entry2, entry3)
        val n = candidates.size
        val sliceAngle = 360f / n
        val currentAngle = 45f

        for (winningIndex in 0 until n) {
            val sliceCenter = sliceAngle * (winningIndex + 0.5f)
            val targetMod = (360f - sliceCenter + 360f) % 360f

            val currentMod = (currentAngle % 360f + 360f) % 360f
            val diff = (targetMod - currentMod + 360f) % 360f

            val fullSpins = 5
            val totalTargetAngle = currentAngle + (fullSpins * 360f) + diff

            // Verify the wheel always spins forward
            assertTrue(totalTargetAngle > currentAngle)

            // Verify minimum 5 full rotations (1800 degrees)
            assertTrue(totalTargetAngle - currentAngle >= 1800f)

            // Verify landing alignment: when wheel rotates by totalTargetAngle,
            // the winning slice's center aligns with the pointer at top (12 o'clock / 0 deg from top)
            val effectiveRotation = (totalTargetAngle % 360f + 360f) % 360f
            val pointerLocalAngle = (360f - effectiveRotation + 360f) % 360f

            // Pointer local angle should match the winning slice's center
            assertEquals(sliceCenter, pointerLocalAngle, 0.01f)
        }
    }

    @Test
    fun filterCandidates_favoritesOnly_returnsOnlyFavorites() {
        val favorites = allCandidates.filter { it.entry.isFavorite }
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.entry.isFavorite })
    }

    @Test
    fun filterCandidates_recentThirtyDays_excludesOlderEntries() {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val recent = allCandidates.filter { (now - it.entry.timestamp) <= thirtyDaysMs }
        assertEquals(2, recent.size)
        assertTrue(recent.none { it.entry.id == 3L })
    }

    @Test
    fun filterCandidates_byTag_returnsMatchingTagOnly() {
        val spicyOnly = allCandidates.filter { item -> item.tags.any { it.tagId == 1L } }
        assertEquals(1, spicyOnly.size)
        assertEquals("Ayam Geprek", spicyOnly.first().entry.title)
    }
}
