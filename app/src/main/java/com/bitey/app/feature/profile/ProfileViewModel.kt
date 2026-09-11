package com.bitey.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    plateEntryDao: PlateEntryDao,
    tagDao: TagDao
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = plateEntryDao.getAllEntriesWithTags()
        .map { entries ->
            val totalCount = entries.size
            val favCount = entries.count { it.entry.isFavorite }
            val spotsCount = entries.count { it.entry.latitude != null && it.entry.longitude != null }
            val topMeal = entries.groupBy { it.entry.mealType }
                .maxByOrNull { it.value.size }?.key
            val topTags = entries.flatMap { it.tags }
                .groupBy { it.tagName }
                .toList()
                .sortedByDescending { it.second.size }
                .take(5)
                .map { it.first }
            val stickerCount = entries.count { it.entry.isStickerMode }
            val recent = entries.take(6)

            ProfileUiState(
                totalEntriesCount = totalCount,
                favoriteEntriesCount = favCount,
                spotsWithLocationCount = spotsCount,
                topMealType = topMeal,
                favoriteTagNames = topTags,
                stickerModeEntriesCount = stickerCount,
                recentEntries = recent,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}
