package com.bitey.app.feature.footprints

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.LocationCoordinates
import com.bitey.app.core.location.RouteRepository
import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.core.location.model.RouteStep
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FootprintsNavigationTest {

    @Test
    fun formattedDistance_formatsKilometersAndMetersCorrectly() {
        val routeMeters = NavigationRoute(
            points = listOf(LocationCoordinates(-6.2, 106.8), LocationCoordinates(-6.21, 106.81)),
            distanceMeters = 450.0,
            durationSeconds = 120.0,
            steps = emptyList()
        )
        assertEquals("450 m", routeMeters.formattedDistance)

        val routeKm = NavigationRoute(
            points = listOf(LocationCoordinates(-6.2, 106.8), LocationCoordinates(-6.25, 106.85)),
            distanceMeters = 3450.0,
            durationSeconds = 720.0,
            steps = emptyList()
        )
        assertEquals("3.5 km", routeKm.formattedDistance)
    }

    @Test
    fun formattedDuration_formatsMinutesAndHoursCorrectly() {
        val routeMinutes = NavigationRoute(
            points = emptyList(),
            distanceMeters = 1000.0,
            durationSeconds = 750.0, // 12.5 mins
            steps = emptyList()
        )
        assertEquals("12 min", routeMinutes.formattedDuration)

        val routeHours = NavigationRoute(
            points = emptyList(),
            distanceMeters = 50000.0,
            durationSeconds = 4500.0, // 1h 15m
            steps = emptyList()
        )
        assertEquals("1h 15m", routeHours.formattedDuration)
    }

    @Test
    fun routeRepository_generatesFallbackRouteWhenNetworkFails() = runBlocking {
        val repo = RouteRepository()
        val start = LocationCoordinates(-6.2088, 106.8456)
        val dest = LocationCoordinates(-6.2297, 106.8295)

        val route = repo.getRoute(start, dest)
        assertNotNull(route)
        assertTrue(route.points.size >= 2)
        assertTrue(route.distanceMeters > 0.0)
        assertTrue(route.steps.isNotEmpty())
    }

    @Test
    fun footprintsUiState_computesCurrentManeuverStep() {
        val step1 = RouteStep("Turn left onto Jl. Sudirman", "turn", "left", 200.0, 30.0)
        val step2 = RouteStep("Arrived at destination", "arrive", null, 0.0, 0.0)
        val route = NavigationRoute(
            points = listOf(LocationCoordinates(-6.2, 106.8), LocationCoordinates(-6.21, 106.81)),
            distanceMeters = 200.0,
            durationSeconds = 30.0,
            steps = listOf(step1, step2)
        )

        val dummyEntry = PlateEntryWithTags(
            entry = PlateEntryEntity(
                id = 1,
                title = "Bakso Senayan",
                fullImagePath = "/dummy/path.jpg",
                thumbnailPath = "/dummy/thumb.jpg",
                mealType = MealType.FOOD
            ),
            tags = emptyList()
        )

        val state = FootprintsUiState(
            navigationTarget = dummyEntry,
            activeRoute = route,
            currentStepIndex = 0,
            isNavigating = true
        )

        assertEquals("Turn left onto Jl. Sudirman", state.currentManeuverStep?.instruction)
        assertEquals("turn", state.currentManeuverStep?.maneuverType)

        val stateStep2 = state.copy(currentStepIndex = 1)
        assertEquals("Arrived at destination", stateStep2.currentManeuverStep?.instruction)
    }
}
