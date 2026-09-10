package com.bitey.app.feature.footprints

import com.bitey.app.core.database.model.PlateEntryWithTags

data class FootprintsUiState(
    val entriesWithLocation: List<PlateEntryWithTags> = emptyList(),
    val selectedEntry: PlateEntryWithTags? = null,
    val isLoading: Boolean = true
)
