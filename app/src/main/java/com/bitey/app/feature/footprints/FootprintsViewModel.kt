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
    private val locationProvider: LocationProvider,
    private val geocoderRepository: com.bitey.app.core.location.GeocoderRepository
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
        val locationEntries = allEntries.map { item ->
            val e = item.entry
            if (e.latitude != null && e.longitude != null && e.latitude != 0.0 && e.longitude != 0.0) {
                item
            } else {
                val loc = e.locationName?.lowercase() ?: ""
                val quickCoords = when {
                    loc.contains("menteng") -> LocationCoordinates(-6.1950, 106.8330)
                    loc.contains("kuningan") -> LocationCoordinates(-6.2297, 106.8295)
                    loc.contains("senopati") -> LocationCoordinates(-6.2367, 106.8075)
                    loc.contains("cikini") -> LocationCoordinates(-6.1906, 106.8385)
                    loc.contains("tebet") -> LocationCoordinates(-6.2300, 106.8520)
                    loc.contains("kemang") -> LocationCoordinates(-6.2750, 106.8150)
                    loc.contains("sudirman") -> LocationCoordinates(-6.2150, 106.8200)
                    loc.contains("bandung") -> LocationCoordinates(-6.9175, 107.6191)
                    loc.contains("bali") -> LocationCoordinates(-8.4095, 115.1889)
                    loc.contains("surabaya") -> LocationCoordinates(-7.2575, 112.7521)
                    loc.contains("jogja") || loc.contains("yogyakarta") -> LocationCoordinates(-7.7956, 110.3695)
                    else -> _deviceLocation.value ?: run {
                        val h = kotlin.math.abs((e.title + loc + e.id).hashCode())
                        LocationCoordinates(-6.2088 + ((h % 1000) / 1000.0 - 0.5) * 0.03, 106.8456 + (((h / 1000) % 1000) / 1000.0 - 0.5) * 0.03)
                    }
                }
                item.copy(entry = e.copy(latitude = quickCoords.latitude, longitude = quickCoords.longitude))
            }
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
        autoResolveMissingLocations()
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
                autoResolveMissingLocations()
            }
        }
    }

    fun autoResolveMissingLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val missing = plateEntryDao.getEntriesWithMissingLocation()
            if (missing.isEmpty()) return@launch

            val stillMissing = mutableListOf<PlateEntryEntity>()
            for (entry in missing) {
                val locName = entry.locationName?.trim()
                var resolved = false
                if (!locName.isNullOrBlank()) {
                    val coords = geocoderRepository.forwardGeocode(locName)
                    if (coords != null) {
                        plateEntryDao.updateEntry(entry.copy(latitude = coords.latitude, longitude = coords.longitude))
                        resolved = true
                    }
                }
                if (!resolved) {
                    stillMissing.add(entry)
                }
            }

            if (stillMissing.isNotEmpty()) {
                val devLoc = _deviceLocation.value ?: locationProvider.getCurrentLocation(timeoutMillis = 4000L)
                val fallbackName = devLoc?.let { geocoderRepository.reverseGeocode(it.latitude, it.longitude)?.displayName } ?: "Food Spot"
                for (entry in stillMissing) {
                    val targetCoords = devLoc ?: run {
                        val h = kotlin.math.abs((entry.title + entry.id).hashCode())
                        LocationCoordinates(-6.2088 + ((h % 1000) / 1000.0 - 0.5) * 0.03, 106.8456 + (((h / 1000) % 1000) / 1000.0 - 0.5) * 0.03)
                    }
                    plateEntryDao.updateEntry(
                        entry.copy(
                            latitude = targetCoords.latitude,
                            longitude = targetCoords.longitude,
                            locationName = entry.locationName ?: fallbackName
                        )
                    )
                }
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
