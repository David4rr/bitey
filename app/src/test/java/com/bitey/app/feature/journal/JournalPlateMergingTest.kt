package com.bitey.app.feature.journal

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class JournalPlateMergingTest {

    private fun createEntry(
        id: Long,
        title: String,
        locationName: String?,
        timestamp: Long,
        plateSessionId: String? = null,
        mealType: MealType = MealType.FOOD
    ): PlateEntryWithTags {
        return PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = id,
                title = title,
                fullImagePath = "/path/$id.jpg",
                stickerImagePath = "/path/${id}_sticker.png",
                thumbnailPath = "/path/$id.jpg",
                locationName = locationName,
                timestamp = timestamp,
                plateSessionId = plateSessionId,
                mealType = mealType
            ),
            tags = emptyList()
        )
    }

    @Test
    fun clusterIntoPlates_mergesEntriesAtSameVenueAndSameDayWithinTwoHours() {
        val baseCalendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 14, 12, 30, 0)
        }
        val t1 = baseCalendar.timeInMillis
        val t2 = t1 + (25 * 60 * 1000L) // 25 minutes later
        val t3 = t1 + (40 * 60 * 1000L) // 40 minutes later

        val dish1 = createEntry(1L, "Tonkotsu Ramen", "Ichiran Ramen", t1)
        val dish2 = createEntry(2L, "Gyoza", "Ichiran Ramen", t2)
        val dish3 = createEntry(3L, "Matcha Ice Cream", "Ichiran Ramen", t3, mealType = MealType.OTHER)

        val plates = JournalViewModel.clusterIntoPlates(listOf(dish3, dish2, dish1))

        assertEquals(1, plates.size)
        val plate = plates.first()
        assertTrue(plate.isMergedPlate)
        assertEquals(3, plate.entries.size)
        assertEquals("Ichiran Ramen", plate.venueName)
        assertEquals(3, plate.allStickerPaths.size)
    }

    @Test
    fun clusterIntoPlates_doesNotMergeEntriesAtDifferentVenues() {
        val baseCalendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 14, 12, 30, 0)
        }
        val t1 = baseCalendar.timeInMillis
        val t2 = t1 + (30 * 60 * 1000L)

        val dish1 = createEntry(1L, "Burger", "Shake Shack", t1)
        val dish2 = createEntry(2L, "Coffee", "Starbucks", t2, mealType = MealType.DRINK)

        val plates = JournalViewModel.clusterIntoPlates(listOf(dish2, dish1))

        assertEquals(2, plates.size)
        assertFalse(plates[0].isMergedPlate)
        assertFalse(plates[1].isMergedPlate)
    }

    @Test
    fun clusterIntoPlates_doesNotMergeEntriesOnDifferentDays() {
        val day1 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 14, 12, 30, 0)
        }.timeInMillis

        val day2 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15, 12, 30, 0)
        }.timeInMillis

        val dish1 = createEntry(1L, "Ramen Day 1", "Ichiran Ramen", day1)
        val dish2 = createEntry(2L, "Ramen Day 2", "Ichiran Ramen", day2)

        val plates = JournalViewModel.clusterIntoPlates(listOf(dish2, dish1))

        assertEquals(2, plates.size)
    }

    @Test
    fun clusterIntoPlates_doesNotMergeEntriesBeyondTwoHours() {
        val baseCalendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 14, 12, 0, 0)
        }
        val t1 = baseCalendar.timeInMillis
        val t2 = t1 + (3 * 60 * 60 * 1000L) // 3 hours later (e.g. lunch vs late afternoon)

        val dish1 = createEntry(1L, "Lunch Noodles", "Food Hall", t1)
        val dish2 = createEntry(2L, "Afternoon Snack", "Food Hall", t2)

        val plates = JournalViewModel.clusterIntoPlates(listOf(dish2, dish1))

        assertEquals(2, plates.size)
    }

    @Test
    fun clusterIntoPlates_mergesEntriesWithSamePlateSessionIdEvenWithoutVenue() {
        val t1 = System.currentTimeMillis()
        val sessionId = "custom-session-uuid-123"

        val dish1 = createEntry(1L, "Home Dish 1", null, t1, plateSessionId = sessionId)
        val dish2 = createEntry(2L, "Home Dish 2", null, t1 + 100, plateSessionId = sessionId)

        val plates = JournalViewModel.clusterIntoPlates(listOf(dish2, dish1))

        assertEquals(1, plates.size)
        assertEquals(2, plates.first().entries.size)
        assertEquals(sessionId, plates.first().id)
    }
}
