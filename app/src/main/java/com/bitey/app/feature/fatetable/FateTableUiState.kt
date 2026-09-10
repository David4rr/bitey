package com.bitey.app.feature.fatetable

import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity

enum class FateSourceFilter {
    ALL,
    FAVORITES,
    RECENT_30_DAYS
}

data class FateTableUiState(
    val candidates: List<PlateEntryWithTags> = emptyList(),
    val totalEntriesCount: Int = 0,
    val selectedFilter: FateSourceFilter = FateSourceFilter.ALL,
    val selectedTagId: Long? = null,
    val availableTags: List<TagEntity> = emptyList(),
    val isSpinning: Boolean = false,
    val winningEntry: PlateEntryWithTags? = null,
    val showWinningDialog: Boolean = false,
    val currentRotationAngle: Float = 0f
)
