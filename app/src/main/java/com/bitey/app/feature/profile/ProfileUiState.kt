package com.bitey.app.feature.profile

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryWithTags

data class ProfileUiState(
    val totalEntriesCount: Int = 0,
    val favoriteEntriesCount: Int = 0,
    val spotsWithLocationCount: Int = 0,
    val topMealType: MealType? = null,
    val favoriteTagNames: List<String> = emptyList(),
    val stickerModeEntriesCount: Int = 0,
    val recentEntries: List<PlateEntryWithTags> = emptyList(),
    val isLoading: Boolean = false
)
