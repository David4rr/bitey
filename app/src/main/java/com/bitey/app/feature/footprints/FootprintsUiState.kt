package com.bitey.app.feature.footprints

import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.core.location.model.RouteStep

data class FootprintSpot(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String?,
    val entries: List<PlateEntryWithTags>
) {
    val primaryEntry: PlateEntryWithTags get() = entries.first()
    val isMultiple: Boolean get() = entries.size > 1
}

data class FootprintsUiState(
    val entriesWithLocation: List<PlateEntryWithTags> = emptyList(),
    val spots: List<FootprintSpot> = emptyList(),
    val selectedSpot: FootprintSpot? = null,
    val selectedEntry: PlateEntryWithTags? = null,
    val searchQuery: String = "",
    val isFavoritesOnly: Boolean = false,
    val allVisitedCount: Int = 0,
    val favoriteSpotsCount: Int = 0,
    val isLoading: Boolean = true,
    val navigationTarget: PlateEntryWithTags? = null,
    val activeRoute: NavigationRoute? = null,
    val currentStepIndex: Int = 0,
    val isNavigating: Boolean = false
) {
    val currentManeuverStep: RouteStep?
        get() = activeRoute?.steps?.getOrNull(currentStepIndex)
            ?: activeRoute?.steps?.firstOrNull()
}
