package com.bitey.app.feature.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.location.GeocoderRepository
import com.bitey.app.core.location.LocationProvider
import com.bitey.app.feature.camera.CandidateStickerItem
import com.bitey.app.feature.camera.CaptureFlowStep
import com.bitey.app.feature.camera.PhotoMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val stickerPipeline: StickerPipeline,
    private val locationProvider: LocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()

    fun setPhotoMode(mode: PhotoMode) { _uiState.update { it.copy(photoMode = mode) } }
    fun updateDishName(name: String) { _uiState.update { it.copy(dishName = name) } }
    fun openManualCutDialog() { _uiState.update { it.copy(showManualCutDialog = true) } }
    fun dismissManualCutDialog() { _uiState.update { it.copy(showManualCutDialog = false) } }
    fun openCamera() { _uiState.update { it.copy(isCameraActive = true) } }
    fun closeCamera() { _uiState.update { it.copy(isCameraActive = false) } }
    fun clearSelectedImage() { _uiState.update { it.copy(selectedImage = null, sticker = null) } }
    fun openFallbackDialog() { _uiState.update { it.copy(showFallbackDialog = true) } }
    fun dismissFallbackDialog() { _uiState.update { it.copy(showFallbackDialog = false) } }
    fun generateSticker(style: StickerStyle = StickerStyle.AI_SEGMENTED) { applyManualCut(style) }

    fun updateMealType(mealType: MealType) {
        _uiState.update { current ->
            val newName = if (current.dishName.isBlank() || MealType.entries.any { it.label == current.dishName }) {
                mealType.label
            } else current.dishName
            current.copy(mealType = mealType, dishName = newName)
        }
    }

    fun toggleCandidate(id: String) {
        _uiState.update { current ->
            current.copy(candidates = current.candidates.map {
                if (it.id == id) it.copy(isSelected = !it.isSelected) else it
            })
        }
    }

    fun onPhotoCaptured(file: File) {
        val mode = _uiState.value.photoMode
        if (mode == PhotoMode.DISH_BY_DISH) {
            _uiState.update { it.copy(capturedDishes = it.capturedDishes + file) }
        } else {
            processSingleFile(file)
        }
    }

    fun finishDishByDishCapture() {
        val files = _uiState.value.capturedDishes
        if (files.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureFlowStep.PROCESSING, segmentationStatusText = "Detecting dishes & removing background...") }
            fetchLocation()
            val list = mutableListOf<CandidateStickerItem>()
            files.forEachIndexed { index, file ->
                _uiState.update {
                    it.copy(processingSourceFile = file.absolutePath, processingStickerFile = null, segmentationStatusText = "Cutting dish ${index + 1}/${files.size}...")
                }
                val sticker = stickerPipeline.createStickerWithFallback(file)
                if (sticker != null) {
                    list.add(CandidateStickerItem(originalFilePath = file.absolutePath, stickerFilePath = sticker.file.absolutePath, label = "Dish ${index + 1}"))
                    _uiState.update { it.copy(processingStickerFile = sticker.file.absolutePath) }
                    kotlinx.coroutines.delay(850)
                }
            }
            _uiState.update { it.copy(step = CaptureFlowStep.REVIEW, candidates = list) }
        }
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureFlowStep.PROCESSING, segmentationStatusText = "Reading gallery photo...") }
            imagePreprocessor.processUri(uri).fold(
                onSuccess = { processed -> processSingleFile(processed.file) },
                onFailure = { error -> _uiState.update { it.copy(step = CaptureFlowStep.CAMERA, errorMessage = error.localizedMessage) } }
            )
        }
    }

    private fun processSingleFile(file: File) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(step = CaptureFlowStep.PROCESSING, processingSourceFile = file.absolutePath, processingStickerFile = null, segmentationStatusText = "Detecting dishes & removing background...")
            }
            fetchLocation()
            val stickers = stickerPipeline.createStickersForMultiSubject(file)
            val list = if (stickers.isNotEmpty()) {
                stickers.mapIndexed { idx, s ->
                    CandidateStickerItem(originalFilePath = file.absolutePath, stickerFilePath = s.file.absolutePath, label = "Dish ${idx + 1}")
                }
            } else {
                listOf(CandidateStickerItem(originalFilePath = file.absolutePath, stickerFilePath = file.absolutePath))
            }
            _uiState.update { it.copy(processingStickerFile = list.firstOrNull()?.stickerFilePath, segmentationStatusText = "Voila! Sticker ready ✨") }
            kotlinx.coroutines.delay(950)
            _uiState.update { it.copy(step = CaptureFlowStep.REVIEW, candidates = list) }
        }
    }

    private suspend fun fetchLocation() {
        val coords = locationProvider.getCurrentLocation() ?: return
        val locName = geocoderRepository.reverseGeocode(coords.latitude, coords.longitude)?.displayName
        _uiState.update { it.copy(latitude = coords.latitude, longitude = coords.longitude, locationName = locName) }
    }

    fun applyManualCut(style: StickerStyle) {
        val target = _uiState.value.candidates.firstOrNull { it.isSelected } ?: _uiState.value.candidates.firstOrNull() ?: return
        viewModelScope.launch {
            val file = File(target.originalFilePath)
            val sticker = stickerPipeline.createManualSticker(file, style) ?: return@launch
            val updated = target.copy(stickerFilePath = sticker.file.absolutePath)
            _uiState.update { current ->
                current.copy(
                    candidates = current.candidates.map { if (it.id == target.id) updated else it },
                    showManualCutDialog = false
                )
            }
        }
    }

    fun saveAllSelectedAndClose(onSuccess: () -> Unit) {
        val state = _uiState.value
        val toSave = state.candidates.filter { it.isSelected }
        if (toSave.isEmpty()) {
            resetState()
            onSuccess()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            toSave.forEachIndexed { idx, item ->
                val title = if (toSave.size > 1 && idx > 0) "${state.dishName} #${idx + 1}" else state.dishName
                plateEntryDao.insertEntryWithTags(
                    entry = PlateEntryEntity(
                        title = title, note = null, fullImagePath = item.originalFilePath,
                        stickerImagePath = item.stickerFilePath, thumbnailPath = item.stickerFilePath,
                        isStickerMode = true, rating = 5.0f, price = null, currency = "IDR", isFavorite = false,
                        timestamp = System.currentTimeMillis(), latitude = state.latitude, longitude = state.longitude,
                        locationName = state.locationName, mealType = state.mealType
                    ),
                    tags = emptyList(), tagDao = tagDao
                )
            }
            withContext(Dispatchers.Main) {
                resetState()
                onSuccess()
            }
        }
    }

    fun resetState() {
        _uiState.update { NewEntryUiState() }
    }
}
