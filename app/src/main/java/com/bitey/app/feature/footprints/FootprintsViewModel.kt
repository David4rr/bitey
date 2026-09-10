package com.bitey.app.feature.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.LocationCoordinates
import com.bitey.app.core.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val deviceLocation: StateFlow<LocationCoordinates?> = _deviceLocation

    val uiState: StateFlow<FootprintsUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        _selectedEntry
    ) { allEntries, selected ->
        val locationEntries = allEntries.filter {
            it.entry.latitude != null && it.entry.longitude != null
        }

        FootprintsUiState(
            entriesWithLocation = locationEntries,
            selectedEntry = selected,
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
}
