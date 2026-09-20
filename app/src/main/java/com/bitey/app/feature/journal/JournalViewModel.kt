package com.bitey.app.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
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
    private val tagDao: TagDao,
    private val geocoderRepository: com.bitey.app.core.location.GeocoderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)
    private val _selectedMealType = MutableStateFlow<MealType?>(null)
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    private val _isGridView = MutableStateFlow(true)
    private val _selectedPlateForDetail = MutableStateFlow<JournalPlate?>(null)
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
        _selectedEntryForDetail,
        _selectedPlateForDetail
    ) { allEntries, tags, filters, detailEntry, detailPlate ->
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

        val plates = clusterIntoPlates(filtered)

        // Group plates chronologically by calendar date
        val dateGroups = plates
            .groupBy { plate -> formatDateHeader(plate.timestamp) }
            .map { (dateLabel, groupPlates) ->
                DateGroup(
                    dateLabel = dateLabel,
                    entries = groupPlates.flatMap { it.entries },
                    plates = groupPlates
                )
            }

        val currentDetailEntry = detailEntry?.let { current ->
            allEntries.find { it.entry.id == current.entry.id } ?: current
        }

        val currentDetailPlate = detailPlate?.let { current ->
            plates.find { it.id == current.id } ?: current
        }

        JournalUiState(
            entries = filtered,
            plates = plates,
            dateGroups = dateGroups,
            totalEntriesCount = allEntries.size,
            searchQuery = filters.query,
            isFavoritesOnly = filters.isFavoritesOnly,
            selectedMealType = filters.selectedMealType,
            selectedTagId = filters.selectedTagId,
            availableTags = tags,
            isGridView = filters.isGridView,
            isLoading = false,
            selectedEntryForDetail = currentDetailEntry,
            selectedPlateForDetail = currentDetailPlate
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

    fun setGridView(isGrid: Boolean) {
        _isGridView.value = isGrid
    }

    fun selectEntryForDetail(entry: PlateEntryWithTags?) {
        _selectedEntryForDetail.value = entry
    }

    fun selectPlateForDetail(plate: JournalPlate?, selectedDish: PlateEntryWithTags? = null) {
        _selectedPlateForDetail.value = plate
        _selectedEntryForDetail.value = selectedDish ?: plate?.entries?.firstOrNull()
    }

    companion object {
        fun clusterIntoPlates(entries: List<PlateEntryWithTags>): List<JournalPlate> {
            val result = mutableListOf<JournalPlate>()
            val visited = mutableSetOf<Long>()

            for (i in entries.indices) {
                val current = entries[i]
                if (current.entry.id in visited) continue

                val plateMembers = mutableListOf(current)
                visited.add(current.entry.id)

                val currentSessionId = current.entry.plateSessionId
                val currentVenue = current.entry.locationName?.trim()
                val currentTimestamp = current.entry.timestamp

                for (j in i + 1 until entries.size) {
                    val candidate = entries[j]
                    if (candidate.entry.id in visited) continue

                    val candidateSessionId = candidate.entry.plateSessionId
                    val candidateVenue = candidate.entry.locationName?.trim()
                    val candidateTimestamp = candidate.entry.timestamp

                    val isSameSession = currentSessionId != null && candidateSessionId != null && currentSessionId == candidateSessionId
                    val isSameVenueAndDayTime = !currentVenue.isNullOrBlank() &&
                        !candidateVenue.isNullOrBlank() &&
                        currentVenue.equals(candidateVenue, ignoreCase = true) &&
                        isSameDay(currentTimestamp, candidateTimestamp) &&
                        kotlin.math.abs(currentTimestamp - candidateTimestamp) <= 2 * 60 * 60 * 1000L

                    if (isSameSession || isSameVenueAndDayTime) {
                        plateMembers.add(candidate)
                        visited.add(candidate.entry.id)
                    }
                }

                val plateId = currentSessionId ?: "plate_${current.entry.id}"
                val venue = currentVenue ?: plateMembers.firstOrNull { !it.entry.locationName.isNullOrBlank() }?.entry?.locationName
                result.add(
                    JournalPlate(
                        id = plateId,
                        venueName = venue,
                        timestamp = currentTimestamp,
                        entries = plateMembers
                    )
                )
            }

            return result
        }

        fun isSameDay(t1: Long, t2: Long): Boolean {
            val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = t1 }
            val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = t2 }
            return cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA) &&
                   cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                   cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
        }
    }

    fun toggleFavorite(entry: PlateEntryEntity) {
        val newFav = !entry.isFavorite
        _selectedEntryForDetail.update { current ->
            if (current?.entry?.id == entry.id) {
                current.copy(entry = current.entry.copy(isFavorite = newFav))
            } else current
        }
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateFavoriteStatus(entry.id, newFav)
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

    fun updateEntry(entry: PlateEntryEntity, tags: List<TagEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedEntry = if (entry.latitude == null && !entry.locationName.isNullOrBlank()) {
                val coords = geocoderRepository.forwardGeocode(entry.locationName)
                if (coords != null) {
                    entry.copy(latitude = coords.latitude, longitude = coords.longitude)
                } else entry
            } else entry
            plateEntryDao.updateEntryWithTags(resolvedEntry, tags, tagDao)
            _selectedEntryForDetail.value = PlateEntryWithTags(resolvedEntry, tags)
        }
    }
}
