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
import kotlinx.coroutines.flow.flowOn
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

    private data class DetailSelection(
        val plate: JournalPlate?,
        val dish: PlateEntryWithTags?
    )
    private val _detailSelection = MutableStateFlow<DetailSelection?>(null)

    private data class FilterState(
        val query: String,
        val isFavoritesOnly: Boolean,
        val selectedMealType: MealType?,
        val selectedTagId: Long?,
        val isGridView: Boolean
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
            isGridView = isGrid
        )
    }

    private data class BaseJournalData(
        val allEntries: List<PlateEntryWithTags>,
        val filtered: List<PlateEntryWithTags>,
        val plates: List<JournalPlate>,
        val dateGroups: List<DateGroup>,
        val tags: List<TagEntity>,
        val filters: FilterState
    )

    private val baseJournalData = combine(
        plateEntryDao.getAllEntriesWithTags(),
        tagDao.getAllTags(),
        filterStateFlow
    ) { allEntries, tags, filters ->
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
        val now = System.currentTimeMillis()
        val dateGroups = plates
            .groupBy { plate -> formatDateHeader(plate.timestamp, now) }
            .map { (dateLabel, groupPlates) ->
                DateGroup(
                    dateLabel = dateLabel,
                    entries = groupPlates.flatMap { it.entries },
                    plates = groupPlates
                )
            }

        BaseJournalData(allEntries, filtered, plates, dateGroups, tags, filters)
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<JournalUiState> = combine(
        baseJournalData,
        _detailSelection
    ) { base, selection ->
        val currentDetailEntry = selection?.dish?.let { current ->
            base.allEntries.find { it.entry.id == current.entry.id } ?: current
        }
        val currentDetailPlate = selection?.plate?.let { current ->
            base.plates.find { it.id == current.id } ?: current
        }

        JournalUiState(
            entries = base.filtered,
            plates = base.plates,
            dateGroups = base.dateGroups,
            totalEntriesCount = base.allEntries.size,
            searchQuery = base.filters.query,
            isFavoritesOnly = base.filters.isFavoritesOnly,
            selectedMealType = base.filters.selectedMealType,
            selectedTagId = base.filters.selectedTagId,
            availableTags = base.tags,
            isGridView = base.filters.isGridView,
            isLoading = false,
            selectedEntryForDetail = currentDetailEntry,
            selectedPlateForDetail = currentDetailPlate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = JournalUiState()
    )

    private val dayMonthFormat = java.text.SimpleDateFormat("dd MMMM", java.util.Locale.US)
    private val fullDateFormat = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.US)

    private fun formatDateHeader(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val tz = java.util.TimeZone.getDefault()
        val entryDay = (timestamp + tz.getOffset(timestamp)) / 86_400_000L
        val nowDay = (now + tz.getOffset(now)) / 86_400_000L

        return when (entryDay) {
            nowDay -> "Today • " + synchronized(dayMonthFormat) { dayMonthFormat.format(java.util.Date(timestamp)) }
            nowDay - 1L -> "Yesterday • " + synchronized(dayMonthFormat) { dayMonthFormat.format(java.util.Date(timestamp)) }
            else -> synchronized(fullDateFormat) { fullDateFormat.format(java.util.Date(timestamp)) }
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
        _detailSelection.update { current ->
            if (entry != null) {
                val plate = current?.plate ?: JournalPlate(
                    id = entry.entry.plateSessionId ?: "plate_${entry.entry.id}",
                    venueName = entry.entry.locationName,
                    timestamp = entry.entry.timestamp,
                    entries = listOf(entry)
                )
                DetailSelection(plate, entry)
            } else {
                null
            }
        }
    }

    fun selectPlateForDetail(plate: JournalPlate?, selectedDish: PlateEntryWithTags? = null) {
        _detailSelection.value = if (plate != null) {
            DetailSelection(plate, selectedDish ?: plate.entries.firstOrNull())
        } else {
            null
        }
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
            val tz = java.util.TimeZone.getDefault()
            val day1 = (t1 + tz.getOffset(t1)) / 86_400_000L
            val day2 = (t2 + tz.getOffset(t2)) / 86_400_000L
            return day1 == day2
        }
    }

    fun toggleFavorite(entry: PlateEntryEntity) {
        val newFav = !entry.isFavorite
        _detailSelection.update { current ->
            if (current?.dish?.entry?.id == entry.id) {
                current.copy(dish = current.dish.copy(entry = current.dish.entry.copy(isFavorite = newFav)))
            } else current
        }
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateFavoriteStatus(entry.id, newFav)
        }
    }

    fun deleteEntry(entry: PlateEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.deleteEntry(entry)
            if (_detailSelection.value?.dish?.entry?.id == entry.id) {
                _detailSelection.value = null
            }
        }
    }

    fun updateEntry(entry: PlateEntryEntity, tags: List<TagEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateEntryWithTags(entry, tags, tagDao)
            _detailSelection.update { current ->
                if (current?.dish?.entry?.id == entry.id) {
                    current.copy(dish = PlateEntryWithTags(entry, tags))
                } else current
            }

            if (entry.latitude == null && !entry.locationName.isNullOrBlank()) {
                val coords = geocoderRepository.forwardGeocode(entry.locationName)
                if (coords != null) {
                    val withCoords = entry.copy(latitude = coords.latitude, longitude = coords.longitude)
                    plateEntryDao.updateEntryWithTags(withCoords, tags, tagDao)
                    _detailSelection.update { current ->
                        if (current?.dish?.entry?.id == withCoords.id) {
                            current.copy(dish = PlateEntryWithTags(withCoords, tags))
                        } else current
                    }
                }
            }
        }
    }
}
