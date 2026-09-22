package com.bitey.app.feature.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.*
import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.feature.footprints.component.LocationFallbackResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class FootprintsViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val locationProvider: LocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _selectedSpotId = MutableStateFlow<String?>(null)
    private val _selectedEntryId = MutableStateFlow<Long?>(null)
    private val _deviceLocation = MutableStateFlow<LocationCoordinates?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)
    private val _navigationTarget = MutableStateFlow<PlateEntryWithTags?>(null)
    private val _activeRoute = MutableStateFlow<NavigationRoute?>(null)
    private val _currentStepIndex = MutableStateFlow(0)
    private val _favOverrides = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    val deviceLocation: StateFlow<LocationCoordinates?> = _deviceLocation

    val uiState: StateFlow<FootprintsUiState> = combine(
        listOf(
            plateEntryDao.getAllEntriesWithTags(),
            _selectedSpotId, _selectedEntryId, _searchQuery,
            _isFavoritesOnly, _navigationTarget, _activeRoute, _favOverrides
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST") val allEntries = args[0] as List<PlateEntryWithTags>
        val spotId = args[1] as? String
        val entryId = args[2] as? Long
        val query = args[3] as String
        val favOnly = args[4] as Boolean
        val navTarget = args[5] as? PlateEntryWithTags
        val route = args[6] as? NavigationRoute
        @Suppress("UNCHECKED_CAST") val favMap = args[7] as Map<Long, Boolean>

        val locationEntries = allEntries.map { item ->
            val fav = favMap[item.entry.id] ?: item.entry.isFavorite
            val itm = if (fav != item.entry.isFavorite) item.copy(entry = item.entry.copy(isFavorite = fav)) else item
            val fe = itm.entry
            if (fe.latitude != null && fe.longitude != null && fe.latitude != 0.0 && fe.longitude != 0.0) itm
            else {
                val q = LocationFallbackResolver.resolveQuickCoordinates(fe.locationName, fe.title, fe.id, _deviceLocation.value)
                itm.copy(entry = fe.copy(latitude = q.latitude, longitude = q.longitude))
            }
        }

        val trimmed = query.trim()
        val filtered = locationEntries.filter { item ->
            if (favOnly && !item.entry.isFavorite) return@filter false
            if (trimmed.isBlank()) true else (item.entry.title.contains(trimmed, true) ||
                item.entry.locationName?.contains(trimmed, true) == true ||
                item.tags.any { it.tagName.contains(trimmed, true) })
        }

        val spots = clusterSpots(filtered)
        val selectedSpot = spots.find { s -> s.id == spotId || (entryId != null && s.entries.any { it.entry.id == entryId }) }
        val selectedEntry = filtered.find { it.entry.id == entryId } ?: selectedSpot?.primaryEntry

        FootprintsUiState(
            entriesWithLocation = filtered, spots = spots, selectedSpot = selectedSpot,
            selectedEntry = selectedEntry, searchQuery = query, isFavoritesOnly = favOnly,
            allVisitedCount = locationEntries.size, favoriteSpotsCount = locationEntries.count { it.entry.isFavorite },
            isLoading = false, navigationTarget = navTarget, activeRoute = route,
            currentStepIndex = _currentStepIndex.value, isNavigating = navTarget != null && route != null
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), initialValue = FootprintsUiState())

    init {
        fetchDeviceLocation()
        autoResolveMissingLocations()
    }

    private fun clusterSpots(entries: List<PlateEntryWithTags>): List<FootprintSpot> {
        val list = mutableListOf<FootprintSpot>()
        for (item in entries) {
            val lat = item.entry.latitude ?: continue
            val lng = item.entry.longitude ?: continue
            val idx = list.indexOfFirst { abs(it.latitude - lat) < 0.0003 && abs(it.longitude - lng) < 0.0003 }
            if (idx != -1) {
                list[idx] = list[idx].copy(entries = list[idx].entries + item)
            } else {
                val id = "spot_${(lat * 10000).toInt()}_${(lng * 10000).toInt()}"
                list.add(FootprintSpot(id, lat, lng, item.entry.locationName, listOf(item)))
            }
        }
        return list
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun selectSpot(spot: FootprintSpot?) {
        if (_navigationTarget.value == null) {
            _selectedSpotId.value = spot?.id
            _selectedEntryId.value = spot?.primaryEntry?.entry?.id
        }
    }

    fun selectEntry(entry: PlateEntryWithTags?) {
        if (_navigationTarget.value == null) {
            _selectedEntryId.value = entry?.entry?.id
            val lat = entry?.entry?.latitude
            val lng = entry?.entry?.longitude
            if (lat != null && lng != null) {
                _selectedSpotId.value = uiState.value.spots.find { abs(it.latitude - lat) < 0.0003 && abs(it.longitude - lng) < 0.0003 }?.id
            }
        }
    }

    fun clearSelection() {
        _selectedSpotId.value = null
        _selectedEntryId.value = null
    }

    fun startNavigation(entry: PlateEntryWithTags) {
        val lat = entry.entry.latitude ?: return
        val lng = entry.entry.longitude ?: return
        viewModelScope.launch {
            val start = _deviceLocation.value ?: locationProvider.getCurrentLocation(4000L) ?: LocationCoordinates(-6.2088, 106.8456)
            clearSelection()
            _navigationTarget.value = entry
            _activeRoute.value = routeRepository.getRoute(start, LocationCoordinates(lat, lng))
            _currentStepIndex.value = 0
        }
    }

    fun startNavigationForEntryId(id: Long) {
        viewModelScope.launch { plateEntryDao.getEntryById(id).firstOrNull()?.let { startNavigation(it) } }
    }

    fun stopNavigation() {
        _navigationTarget.value = null
        _activeRoute.value = null
        _currentStepIndex.value = 0
    }

    fun fetchDeviceLocation() {
        viewModelScope.launch {
            locationProvider.getCurrentLocation()?.let {
                _deviceLocation.value = it
                autoResolveMissingLocations()
            }
        }
    }

    fun autoResolveMissingLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val missing = plateEntryDao.getEntriesWithMissingLocation()
            if (missing.isEmpty()) return@launch
            for (entry in missing) {
                val loc = entry.locationName?.trim()
                if (!loc.isNullOrBlank()) {
                    geocoderRepository.forwardGeocode(loc)?.let {
                        plateEntryDao.updateEntry(entry.copy(latitude = it.latitude, longitude = it.longitude))
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun toggleFavoritesOnly() { _isFavoritesOnly.update { !it } }

    fun toggleFavorite(entry: PlateEntryEntity) {
        val newFav = !entry.isFavorite
        _favOverrides.update { it + (entry.id to newFav) }
        viewModelScope.launch(Dispatchers.IO) { plateEntryDao.updateFavoriteStatus(entry.id, newFav) }
    }
}
