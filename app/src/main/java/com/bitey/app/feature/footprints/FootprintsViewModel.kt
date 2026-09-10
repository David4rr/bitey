package com.bitey.app.feature.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.LocationCoordinates
import com.bitey.app.core.location.LocationProvider
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
class FootprintsViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _selectedEntry = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _deviceLocation = MutableStateFlow<LocationCoordinates?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)

    val deviceLocation: StateFlow<LocationCoordinates?> = _deviceLocation

    val uiState: StateFlow<FootprintsUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        _selectedEntry,
        _searchQuery,
        _isFavoritesOnly
    ) { allEntries, selected, query, favOnly ->
        val locationEntries = allEntries.filter {
            it.entry.latitude != null && it.entry.longitude != null
        }

        val trimmedQuery = query.trim()
        val filtered = locationEntries.filter { item ->
            if (favOnly && !item.entry.isFavorite) return@filter false
            if (trimmedQuery.isNotBlank()) {
                val matchesTitle = item.entry.title.contains(trimmedQuery, ignoreCase = true)
                val matchesLocation = item.entry.locationName?.contains(trimmedQuery, ignoreCase = true) == true
                val matchesTag = item.tags.any { it.tagName.contains(trimmedQuery, ignoreCase = true) }
                if (!matchesTitle && !matchesLocation && !matchesTag) return@filter false
            }
            true
        }

        val favCount = locationEntries.count { it.entry.isFavorite }

        FootprintsUiState(
            entriesWithLocation = filtered,
            selectedEntry = selected,
            searchQuery = query,
            isFavoritesOnly = favOnly,
            allVisitedCount = locationEntries.size,
            favoriteSpotsCount = favCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FootprintsUiState()
    )

    init {
        fetchDeviceLocation()
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun selectEntry(entry: PlateEntryWithTags?) {
        _selectedEntry.value = entry
    }

    fun clearSelection() {
        _selectedEntry.value = null
    }

    fun fetchDeviceLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                _deviceLocation.value = location
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _isFavoritesOnly.update { !it }
    }

    fun toggleFavorite(entry: PlateEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateFavoriteStatus(entry.id, !entry.isFavorite)
        }
    }
}
