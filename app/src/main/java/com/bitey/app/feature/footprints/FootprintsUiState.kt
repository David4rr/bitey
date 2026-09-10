package com.bitey.app.feature.footprints

import com.bitey.app.core.database.model.PlateEntryWithTags

data class FootprintsUiState(
    val entriesWithLocation: List<PlateEntryWithTags> = emptyList(),
    val selectedEntry: PlateEntryWithTags? = null,
    val searchQuery: String = "",
    val isFavoritesOnly: Boolean = false,
    val allVisitedCount: Int = 0,
    val favoriteSpotsCount: Int = 0,
    val isLoading: Boolean = true
)
