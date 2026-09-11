package com.bitey.app.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.DEFAULT_ENTRY_TAGS
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.location.GeocoderRepository
import com.bitey.app.core.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EntryEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao,
    private val locationProvider: LocationProvider,
    private val geocoderRepository: GeocoderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryEditorUiState())
    val uiState: StateFlow<EntryEditorUiState> = _uiState.asStateFlow()

    init {
        val rawImagePath = savedStateHandle.get<String>("imagePath") ?: ""
        val decodedImagePath = try {
            URLDecoder.decode(rawImagePath, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            rawImagePath
        }

        val rawStickerPath = savedStateHandle.get<String>("stickerPath")
        val decodedStickerPath = try {
            rawStickerPath?.takeIf { it.isNotBlank() }?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }
        } catch (e: Exception) {
            rawStickerPath
        }

        val passedTimestamp = savedStateHandle.get<Long>("timestamp")?.takeIf { it > 0 } ?: System.currentTimeMillis()
        val passedLat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
        val passedLng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()
        val inferredMealType = MealType.fromTimestamp(passedTimestamp)

        _uiState.update { current ->
            current.copy(
                imagePath = decodedImagePath,
                stickerPath = decodedStickerPath,
                isStickerMode = decodedStickerPath != null,
                timestamp = passedTimestamp,
                mealType = inferredMealType,
                latitude = passedLat,
                longitude = passedLng
            )
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) { tagDao.insertTags(DEFAULT_ENTRY_TAGS) }
            tagDao.getAllTags().collect { tags -> _uiState.update { it.copy(availableTags = tags) } }
        }

        if (passedLat != null && passedLng != null) {
            resolveGeocodedLocation(passedLat, passedLng)
        } else {
            fetchDeviceLocation()
        }
    }

    fun updateDishTitle(title: String) { _uiState.update { it.copy(dishTitle = title, errorMessage = null) } }
    fun updateNotes(notes: String) { _uiState.update { it.copy(notes = notes) } }
    fun updateMealType(mealType: MealType) { _uiState.update { it.copy(mealType = mealType) } }
    fun updateRating(rating: Float) { _uiState.update { it.copy(rating = rating.coerceIn(1.0f, 5.0f)) } }
    fun updateCurrency(currency: String) { _uiState.update { it.copy(currency = currency) } }
    fun toggleFavorite() { _uiState.update { it.copy(isFavorite = !it.isFavorite) } }
    fun toggleStickerMode() { _uiState.update { it.copy(isStickerMode = !it.isStickerMode) } }
    fun updateLocationName(name: String) { _uiState.update { it.copy(locationName = name) } }
    fun openNewTagDialog() { _uiState.update { it.copy(isNewTagDialogOpen = true) } }
    fun dismissNewTagDialog() { _uiState.update { it.copy(isNewTagDialogOpen = false) } }
    fun requestCurrentLocation() { fetchDeviceLocation() }

    fun updatePrice(price: String) {
        val filtered = price.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(priceString = filtered) }
    }

    fun updateTimestamp(timestamp: Long) {
        val inferredMeal = MealType.fromTimestamp(timestamp)
        _uiState.update { it.copy(timestamp = timestamp, mealType = inferredMeal) }
    }

    fun toggleTag(tag: TagEntity) {
        _uiState.update { current ->
            val updated = current.selectedTags.toMutableSet()
            if (updated.contains(tag)) updated.remove(tag) else updated.add(tag)
            current.copy(selectedTags = updated)
        }
    }

    fun createAndSelectTag(name: String, category: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val existing = tagDao.getTagByName(trimmed)
            val tag = existing ?: run {
                val newId = tagDao.insertTag(TagEntity(tagName = trimmed, category = category))
                TagEntity(tagId = newId, tagName = trimmed, category = category)
            }
            _uiState.update { current ->
                current.copy(selectedTags = current.selectedTags + tag, isNewTagDialogOpen = false)
            }
        }
    }

    private fun fetchDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true) }
            val coords = locationProvider.getCurrentLocation()
            if (coords != null) {
                _uiState.update { it.copy(latitude = coords.latitude, longitude = coords.longitude) }
                resolveGeocodedLocation(coords.latitude, coords.longitude)
            } else {
                _uiState.update { it.copy(isLocating = false) }
            }
        }
    }

    private fun resolveGeocodedLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true) }
            val geocoded = geocoderRepository.reverseGeocode(latitude, longitude)
            val locName = if (geocoded != null && geocoded.displayName.isNotBlank()) {
                geocoded.displayName
            } else {
                _uiState.value.locationName.ifBlank { String.format(Locale.US, "%.4f, %.4f", latitude, longitude) }
            }
            _uiState.update { it.copy(locationName = locName, isLocating = false) }
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        if (state.dishTitle.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a title for your dish") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                plateEntryDao.insertEntryWithTags(
                    entry = state.toPlateEntryEntity(),
                    tags = state.selectedTags.toList(),
                    tagDao = tagDao
                )
                _uiState.update { it.copy(isSaving = false, isSavedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save journal entry: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }
}
