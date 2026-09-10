package com.bitey.app.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.location.GeocoderRepository
import com.bitey.app.core.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Calendar
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
        // Parse navigation arguments
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

        val passedTimestamp = savedStateHandle.get<Long>("timestamp")?.takeIf { it > 0 }
            ?: System.currentTimeMillis()

        val passedLat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
        val passedLng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()

        val inferredMealType = inferMealType(passedTimestamp)

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

        // Initialize tags and seed defaults if empty
        viewModelScope.launch {
            seedDefaultTagsIfEmpty()
            tagDao.getAllTags().collect { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }

        // Resolve location
        if (passedLat != null && passedLng != null) {
            resolveGeocodedLocation(passedLat, passedLng)
        } else {
            fetchDeviceLocation()
        }
    }

    private fun inferMealType(timestamp: Long): MealType {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> MealType.BREAKFAST
            in 11..14 -> MealType.LUNCH
            in 15..17 -> MealType.SNACK
            in 18..21 -> MealType.DINNER
            else -> MealType.LATE_NIGHT
        }
    }

    private suspend fun seedDefaultTagsIfEmpty() = withContext(Dispatchers.IO) {
        val defaultTags = listOf(
            // Taste
            TagEntity(tagName = "Spicy", category = "Taste"),
            TagEntity(tagName = "Savory", category = "Taste"),
            TagEntity(tagName = "Sweet", category = "Taste"),
            TagEntity(tagName = "Umami", category = "Taste"),
            TagEntity(tagName = "Crispy", category = "Taste"),
            TagEntity(tagName = "Smoky", category = "Taste"),
            TagEntity(tagName = "Rich", category = "Taste"),
            // Ambience
            TagEntity(tagName = "Street Food", category = "Ambience"),
            TagEntity(tagName = "Cozy Cafe", category = "Ambience"),
            TagEntity(tagName = "Casual Eatery", category = "Ambience"),
            TagEntity(tagName = "Fine Dining", category = "Ambience"),
            TagEntity(tagName = "Hidden Gem", category = "Ambience"),
            TagEntity(tagName = "Night Market", category = "Ambience"),
            // Diet
            TagEntity(tagName = "Halal", category = "Diet"),
            TagEntity(tagName = "Vegetarian", category = "Diet"),
            TagEntity(tagName = "High Protein", category = "Diet"),
            TagEntity(tagName = "Dessert", category = "Diet")
        )
        tagDao.insertTags(defaultTags)
    }

    fun updateDishTitle(title: String) {
        _uiState.update { it.copy(dishTitle = title, errorMessage = null) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateMealType(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    fun updateRating(rating: Float) {
        _uiState.update { it.copy(rating = rating.coerceIn(1.0f, 5.0f)) }
    }

    fun updatePrice(price: String) {
        val filtered = price.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(priceString = filtered) }
    }

    fun updateCurrency(currency: String) {
        _uiState.update { it.copy(currency = currency) }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun toggleStickerMode() {
        _uiState.update { it.copy(isStickerMode = !it.isStickerMode) }
    }

    fun updateLocationName(name: String) {
        _uiState.update { it.copy(locationName = name) }
    }

    fun toggleTag(tag: TagEntity) {
        _uiState.update { current ->
            val updated = current.selectedTags.toMutableSet()
            if (updated.contains(tag)) {
                updated.remove(tag)
            } else {
                updated.add(tag)
            }
            current.copy(selectedTags = updated)
        }
    }

    fun openNewTagDialog() {
        _uiState.update { it.copy(isNewTagDialogOpen = true) }
    }

    fun dismissNewTagDialog() {
        _uiState.update { it.copy(isNewTagDialogOpen = false) }
    }

    fun createAndSelectTag(name: String, category: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val existing = tagDao.getTagByName(trimmed)
            val tag = if (existing != null) {
                existing
            } else {
                val newId = tagDao.insertTag(TagEntity(tagName = trimmed, category = category))
                TagEntity(tagId = newId, tagName = trimmed, category = category)
            }

            _uiState.update { current ->
                current.copy(
                    selectedTags = current.selectedTags + tag,
                    isNewTagDialogOpen = false
                )
            }
        }
    }

    fun requestCurrentLocation() {
        fetchDeviceLocation()
    }

    private fun fetchDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true) }
            val coords = locationProvider.getCurrentLocation()
            if (coords != null) {
                _uiState.update {
                    it.copy(
                        latitude = coords.latitude,
                        longitude = coords.longitude
                    )
                }
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
            if (geocoded != null && geocoded.displayName.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        locationName = geocoded.displayName,
                        isLocating = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        locationName = if (it.locationName.isBlank()) {
                            String.format(java.util.Locale.US, "%.4f, %.4f", latitude, longitude)
                        } else {
                            it.locationName
                        },
                        isLocating = false
                    )
                }
            }
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        val trimmedTitle = state.dishTitle.trim()

        if (trimmedTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a title for your dish") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val entry = PlateEntryEntity(
                    title = trimmedTitle,
                    note = state.notes.trim().takeIf { it.isNotBlank() },
                    fullImagePath = state.imagePath,
                    stickerImagePath = state.stickerPath,
                    thumbnailPath = state.stickerPath ?: state.imagePath,
                    isStickerMode = state.isStickerMode,
                    rating = state.rating,
                    price = state.priceString.toDoubleOrNull(),
                    currency = state.currency,
                    isFavorite = state.isFavorite,
                    timestamp = state.timestamp,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationName = state.locationName.trim().takeIf { it.isNotBlank() },
                    mealType = state.mealType
                )

                plateEntryDao.insertEntryWithTags(
                    entry = entry,
                    tags = state.selectedTags.toList(),
                    tagDao = tagDao
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccessfully = true
                    )
                }
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
