package com.bitey.app.feature.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.LocationCoordinates
import com.bitey.app.core.location.LocationProvider
import com.bitey.app.core.location.RouteRepository
import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.feature.footprints.component.LocationFallbackResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FootprintsViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val locationProvider: LocationProvider,
    private val geocoderRepository: com.bitey.app.core.location.GeocoderRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _selectedEntry = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _deviceLocation = MutableStateFlow<LocationCoordinates?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)
    private val _navigationTarget = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _activeRoute = MutableStateFlow<NavigationRoute?>(null)
    private val _currentStepIndex = MutableStateFlow(0)

    val deviceLocation: StateFlow<LocationCoordinates?> = _deviceLocation

    val uiState: StateFlow<FootprintsUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        _selectedEntry,
        _searchQuery,
        _isFavoritesOnly,
        _navigationTarget,
        _activeRoute,
        _currentStepIndex
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allEntries = args[0] as List<PlateEntryWithTags>
        val selected = args[1] as? PlateEntryWithTags
        val query = args[2] as String
        val favOnly = args[3] as Boolean
        val navTarget = args[4] as? PlateEntryWithTags
        val route = args[5] as? NavigationRoute
        val stepIdx = args[6] as Int

        val locationEntries = allEntries.map { item ->
            val e = item.entry
            if (e.latitude != null && e.longitude != null && e.latitude != 0.0 && e.longitude != 0.0) {
                item
            } else {
                val quick = LocationFallbackResolver.resolveQuickCoordinates(e.locationName, e.title, e.id, _deviceLocation.value)
                item.copy(entry = e.copy(latitude = quick.latitude, longitude = quick.longitude))
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
            isLoading = false,
            navigationTarget = navTarget,
            activeRoute = route,
            currentStepIndex = stepIdx,
            isNavigating = navTarget != null && route != null
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
        if (_navigationTarget.value == null) {
            _selectedEntry.value = entry
        }
    }

    fun clearSelection() {
        _selectedEntry.value = null
    }

    fun startNavigation(entry: PlateEntryWithTags) {
        val destLat = entry.entry.latitude ?: return
        val destLng = entry.entry.longitude ?: return
        viewModelScope.launch {
            val startCoords = _deviceLocation.value
                ?: locationProvider.getCurrentLocation(timeoutMillis = 4000L)
                ?: LocationCoordinates(-6.2088, 106.8456)
            _selectedEntry.value = null
            _navigationTarget.value = entry
            val route = routeRepository.getRoute(startCoords, LocationCoordinates(destLat, destLng))
            _activeRoute.value = route
            _currentStepIndex.value = 0
        }
    }

    fun startNavigationForEntryId(entryId: Long) {
        viewModelScope.launch {
            val entry = plateEntryDao.getEntryById(entryId).firstOrNull()
            if (entry != null) {
                startNavigation(entry)
            }
        }
    }

    fun stopNavigation() {
        _navigationTarget.value = null
        _activeRoute.value = null
        _currentStepIndex.value = 0
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
                if (!resolved) stillMissing.add(entry)
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

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun toggleFavoritesOnly() { _isFavoritesOnly.update { !it } }
    fun toggleFavorite(entry: PlateEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plateEntryDao.updateFavoriteStatus(entry.id, !entry.isFavorite)
        }
    }
}
