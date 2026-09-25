package com.bitey.app.feature.footprints

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FootprintsClusteringTest {

    private fun createDish(
        id: Long,
        title: String,
        latitude: Double?,
        longitude: Double?,
        locationName: String? = "Cafe",
        isStickerMode: Boolean = true,
        stickerPath: String? = "/path/dish_$id.png"
    ): PlateEntryWithTags {
        return PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = id,
                title = title,
                fullImagePath = "/path/$id.jpg",
                thumbnailPath = "/path/${id}_thumb.jpg",
                stickerImagePath = stickerPath,
                isStickerMode = isStickerMode,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                mealType = MealType.FOOD
            ),
            tags = emptyList()
        )
    }

    @Test
    fun clusterSpots_mergesNearbyDishesIntoSingleMarker() {
        val dish1 = createDish(1L, "Ramen", latitude = 37.7749, longitude = -122.4194)
        val dish2 = createDish(2L, "Gyoza", latitude = 37.7750, longitude = -122.4195) // diff 0.0001 < 0.0003

        val spots = FootprintsViewModel.clusterSpots(listOf(dish1, dish2))

        assertEquals(1, spots.size)
        val spot = spots.first()
        assertEquals(2, spot.entries.size)
        assertTrue(spot.isMultiple)
        assertEquals("Ramen", spot.primaryEntry.entry.title)
    }

    @Test
    fun clusterSpots_mergesThreeNearbyDishesAndMaintainsEntryOrder() {
        val dish1 = createDish(1L, "Matcha Latte", latitude = -6.2088, longitude = 106.8456)
        val dish2 = createDish(2L, "Croissant", latitude = -6.2089, longitude = 106.8457)
        val dish3 = createDish(3L, "Cheesecake", latitude = -6.2087, longitude = 106.8455)

        val spots = FootprintsViewModel.clusterSpots(listOf(dish1, dish2, dish3))

        assertEquals(1, spots.size)
        val spot = spots.first()
        assertEquals(3, spot.entries.size)
        assertTrue(spot.isMultiple)
        assertEquals(listOf(1L, 2L, 3L), spot.entries.map { it.entry.id })
    }

    @Test
    fun clusterSpots_keepsDistantDishesInSeparateMarkers() {
        val dish1 = createDish(1L, "Pizza", latitude = 40.7128, longitude = -74.0060)
        val dish2 = createDish(2L, "Tacos", latitude = 40.7200, longitude = -74.0150) // diff > 0.0003

        val spots = FootprintsViewModel.clusterSpots(listOf(dish1, dish2))

        assertEquals(2, spots.size)
        assertFalse(spots[0].isMultiple)
        assertFalse(spots[1].isMultiple)
        assertEquals(1, spots[0].entries.size)
        assertEquals(1, spots[1].entries.size)
    }

    @Test
    fun clusterSpots_skipsDishesWithoutCoordinates() {
        val validDish = createDish(1L, "Burger", latitude = 51.5074, longitude = -0.1278)
        val noCoordDish = createDish(2L, "Fries", latitude = null, longitude = null)

        val spots = FootprintsViewModel.clusterSpots(listOf(validDish, noCoordDish))

        assertEquals(1, spots.size)
        assertEquals(1, spots.first().entries.size)
        assertEquals(1L, spots.first().entries.first().entry.id)
    }
}
