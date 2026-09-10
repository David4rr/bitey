package com.bitey.app.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)
    private val _selectedMealType = MutableStateFlow<MealType?>(null)
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    private val _isGridView = MutableStateFlow(true)
    private val _selectedEntryForDetail = MutableStateFlow<PlateEntryWithTags?>(null)

    private data class FilterState(
        val query: String,
        val isFavoritesOnly: Boolean,
        val selectedMealType: MealType?,
        val selectedTagId: Long?,
        val isGridView: Boolean,
        val selectedEntryForDetail: PlateEntryWithTags?
    )

    private val filterStateFlow = combine(
        _searchQuery,
        _isFavoritesOnly,
        _selectedMealType,
        _selectedTagId,
        _isGridView
    ) { query, favOnly, mealType, tagId, isGrid ->
        FilterState(
            query = query,
            isFavoritesOnly = favOnly,
            selectedMealType = mealType,
            selectedTagId = tagId,
            isGridView = isGrid,
            selectedEntryForDetail = _selectedEntryForDetail.value
        )
    }

    val uiState: StateFlow<JournalUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        tagDao.getAllTags(),
        filterStateFlow,
        _selectedEntryForDetail
    ) { allEntries, tags, filters, detailEntry ->
        val trimmedQuery = filters.query.trim()
        val filtered = allEntries.filter { item ->
            // Filter by favorite
            if (filters.isFavoritesOnly && !item.entry.isFavorite) return@filter false

            // Filter by meal type
            if (filters.selectedMealType != null && item.entry.mealType != filters.selectedMealType) return@filter false

            // Filter by tag
            if (filters.selectedTagId != null && item.tags.none { it.tagId == filters.selectedTagId }) return@filter false

            // Filter by search query
            if (trimmedQuery.isNotBlank()) {
                val matchesTitle = item.entry.title.contains(trimmedQuery, ignoreCase = true)
                val matchesNote = item.entry.note?.contains(trimmedQuery, ignoreCase = true) == true
                val matchesLocation = item.entry.locationName?.contains(trimmedQuery, ignoreCase = true) == true
                val matchesTag = item.tags.any { it.tagName.contains(trimmedQuery, ignoreCase = true) }
                if (!matchesTitle && !matchesNote && !matchesLocation && !matchesTag) {
                    return@filter false
                }
            }

            true
        }
        // Group entries chronologically by calendar date
        val dateGroups = filtered
            .groupBy { item -> formatDateHeader(item.entry.timestamp) }
            .map { (dateLabel, groupEntries) ->
                DateGroup(dateLabel = dateLabel, entries = groupEntries)
            }

        JournalUiState(
            entries = filtered,
            dateGroups = dateGroups,
            totalEntriesCount = allEntries.size,
            searchQuery = filters.query,
            isFavoritesOnly = filters.isFavoritesOnly,
            selectedMealType = filters.selectedMealType,
            selectedTagId = filters.selectedTagId,
            availableTags = tags,
            isGridView = filters.isGridView,
            isLoading = false,
            selectedEntryForDetail = detailEntry
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = JournalUiState()
    )

    private fun formatDateHeader(timestamp: Long): String {
        val entryCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val todayCal = java.util.Calendar.getInstance()
        val isToday = entryCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                entryCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)

        val yesterdayCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = entryCal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
                entryCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR)

        return when {
            isToday -> {
                val dayStr = java.text.SimpleDateFormat("dd MMMM", java.util.Locale.US).format(java.util.Date(timestamp))
                "Today • $dayStr"
            }
            isYesterday -> {
                val dayStr = java.text.SimpleDateFormat("dd MMMM", java.util.Locale.US).format(java.util.Date(timestamp))
                "Yesterday • $dayStr"
            }
            else -> {
                java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.US).format(java.util.Date(timestamp))
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _isFavoritesOnly.update { !it }
    }

    fun selectMealType(mealType: MealType?) {
        _selectedMealType.update { current ->
            if (current == mealType) null else mealType
        }
    }

    fun selectTag(tagId: Long?) {
        _selectedTagId.update { current ->
            if (current == tagId) null else tagId
        }
    }

    fun toggleViewMode() {
        _isGridView.update { !it }
    }

    fun selectEntryForDetail(entry: PlateEntryWithTags?) {
        _selectedEntryForDetail.value = entry
    }

    fun toggleFavorite(entry: PlateEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateFavoriteStatus(entry.id, !entry.isFavorite)
        }
    }

    fun deleteEntry(entry: PlateEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.deleteEntry(entry)
            if (_selectedEntryForDetail.value?.entry?.id == entry.id) {
                _selectedEntryForDetail.value = null
            }
        }
    }
}
