package com.bitey.app.feature.journal

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity

data class DateGroup(
    val dateLabel: String,
    val entries: List<PlateEntryWithTags>,
    val plates: List<JournalPlate> = emptyList()
)

data class JournalUiState(
    val entries: List<PlateEntryWithTags> = emptyList(),
    val plates: List<JournalPlate> = emptyList(),
    val dateGroups: List<DateGroup> = emptyList(),
    val totalEntriesCount: Int = 0,
    val searchQuery: String = "",
    val isFavoritesOnly: Boolean = false,
    val selectedMealType: MealType? = null,
    val selectedTagId: Long? = null,
    val availableTags: List<TagEntity> = emptyList(),
    val isGridView: Boolean = true,
    val isLoading: Boolean = true,
    val selectedEntryForDetail: PlateEntryWithTags? = null,
    val selectedPlateForDetail: JournalPlate? = null
)
