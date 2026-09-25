package com.bitey.app.feature.entry

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.image.ImageStorageNaming
import com.bitey.app.core.location.LocationProvider
import com.bitey.app.feature.camera.CandidateStickerItem
import com.bitey.app.feature.camera.CaptureFlowStep
import com.bitey.app.feature.camera.PhotoMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imagePreprocessor: ImagePreprocessor,
    private val stickerPipeline: StickerPipeline,
    private val locationProvider: LocationProvider,
    private val plateEntryDao: PlateEntryDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()
    init { viewModelScope.launch { fetchLocation() } }

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
    fun setStickerMode(enabled: Boolean) { _uiState.update { it.copy(isStickerMode = enabled) } }
    fun toggleStickerMode() { _uiState.update { it.copy(isStickerMode = !it.isStickerMode) } }
    fun toggleCandidate(id: String) { _uiState.update { c -> c.copy(candidates = c.candidates.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }) } }
    fun resetState() { _uiState.update { NewEntryUiState() } }

    fun updateMealType(mealType: MealType) {
        _uiState.update { c ->
            val newName = if (c.dishName.isBlank() || MealType.entries.any { it.label == c.dishName }) mealType.label else c.dishName
            c.copy(mealType = mealType, dishName = newName)
        }
    }

    fun updateCandidateLabel(id: String, name: String) {
        _uiState.update { c -> c.copy(candidates = c.candidates.map { if (it.id == id) it.copy(label = name) else it }) }
    }

    fun updateCandidateMealType(id: String, mealType: MealType) {
        _uiState.update { c ->
            c.copy(candidates = c.candidates.map {
                if (it.id == id) {
                    val newLabel = if (it.label.isBlank() || it.label == "Food" || it.label.startsWith("Dish", ignoreCase = true) || MealType.entries.any { m -> m.label == it.label }) mealType.label else it.label
                    it.copy(mealType = mealType, label = newLabel)
                } else it
            })
        }
    }

    fun onPhotoCaptured(file: File) {
        if (_uiState.value.photoMode == PhotoMode.DISH_BY_DISH) _uiState.update { it.copy(capturedDishes = it.capturedDishes + file) }
        else processSingleFile(file)
    }

    fun finishDishByDishCapture() {
        val files = _uiState.value.capturedDishes
        if (files.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureFlowStep.PROCESSING, segmentationStatusText = "Detecting dishes & removing background...") }
            fetchLocation()
            val list = files.mapIndexed { idx, file ->
                _uiState.update { it.copy(processingSourceFile = file.absolutePath, processingStickerFile = null, segmentationStatusText = "Cutting dish ${idx + 1}/${files.size}...") }
                val sticker = stickerPipeline.createStickerWithFallback(file)
                val stickerPath = sticker?.file?.absolutePath ?: file.absolutePath
                if (sticker != null) _uiState.update { it.copy(processingStickerFile = sticker.file.absolutePath) }
                kotlinx.coroutines.delay(200)
                CandidateStickerItem(originalFilePath = file.absolutePath, stickerFilePath = stickerPath, label = "", mealType = MealType.FOOD)
            }
            _uiState.update { it.copy(step = CaptureFlowStep.REVIEW, candidates = list, dishName = list.firstOrNull()?.label ?: "", processingStickerFile = list.firstOrNull()?.stickerFilePath, isStickerMode = true) }
        }
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureFlowStep.PROCESSING, segmentationStatusText = "Reading gallery photo...") }
            imagePreprocessor.processUri(uri).fold(
                onSuccess = { processed ->
                    if (processed.exifMetadata.latitude != null && processed.exifMetadata.longitude != null) {
                        _uiState.update { it.copy(latitude = processed.exifMetadata.latitude, longitude = processed.exifMetadata.longitude) }
                    }
                    processSingleFile(processed.file)
                },
                onFailure = { error -> _uiState.update { it.copy(step = CaptureFlowStep.CAMERA, errorMessage = error.localizedMessage) } }
            )
        }
    }

    private fun processSingleFile(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureFlowStep.PROCESSING, processingSourceFile = file.absolutePath, processingStickerFile = null, segmentationStatusText = "Detecting dishes & removing background...") }
            if (_uiState.value.latitude == null || _uiState.value.longitude == null) fetchLocation()
            val sticker = stickerPipeline.createStickerWithFallback(file)
            val stickerPath = sticker?.file?.absolutePath ?: file.absolutePath
            val list = listOf(CandidateStickerItem(originalFilePath = file.absolutePath, stickerFilePath = stickerPath, label = "", mealType = MealType.FOOD))
            val hasSticker = list.any { it.stickerFilePath != file.absolutePath }
            _uiState.update { it.copy(step = CaptureFlowStep.REVIEW, candidates = list, dishName = list.firstOrNull()?.label ?: "", processingStickerFile = list.firstOrNull()?.stickerFilePath, isStickerMode = hasSticker) }
        }
    }

    private suspend fun fetchLocation() {
        if (_uiState.value.latitude != null && _uiState.value.longitude != null) return
        val coords = locationProvider.getCurrentLocation() ?: return
        _uiState.update { it.copy(latitude = coords.latitude, longitude = coords.longitude) }
    }

    fun applyManualCut(style: StickerStyle) {
        val target = _uiState.value.candidates.firstOrNull { it.isSelected } ?: _uiState.value.candidates.firstOrNull() ?: return
        viewModelScope.launch {
            val sticker = stickerPipeline.createManualSticker(File(target.originalFilePath), style) ?: return@launch
            val updated = target.copy(stickerFilePath = sticker.file.absolutePath)
            _uiState.update { c -> c.copy(candidates = c.candidates.map { if (it.id == target.id) updated else it }, showManualCutDialog = false) }
        }
    }

    fun applySmartCircleCut(cx: Float, cy: Float, r: Float) {
        val target = _uiState.value.candidates.firstOrNull { it.isSelected } ?: _uiState.value.candidates.firstOrNull() ?: return
        viewModelScope.launch {
            val sticker = stickerPipeline.createSmartCircleSticker(File(target.originalFilePath), cx, cy, r) ?: return@launch
            val updated = target.copy(stickerFilePath = sticker.file.absolutePath)
            _uiState.update { c -> c.copy(candidates = c.candidates.map { if (it.id == target.id) updated else it }, showManualCutDialog = false) }
        }
    }

    fun saveAllSelectedAndClose(onSuccess: () -> Unit) {
        val state = _uiState.value
        val toSave = state.candidates.filter { it.isSelected }
        if (toSave.isEmpty()) { resetState(); onSuccess(); return }
        viewModelScope.launch(Dispatchers.IO) {
            val isSticker = state.isStickerMode
            val storageDir = File(context.filesDir, if (isSticker) "bites/stickers" else "bites/media")
            val currentTime = System.currentTimeMillis()
            val coords = if (state.latitude == null || state.longitude == null) locationProvider.getCurrentLocation(2000L) else null
            val lat = state.latitude ?: coords?.latitude
            val lng = state.longitude ?: coords?.longitude
            val locName = state.locationName?.trim()?.takeIf { it.isNotBlank() }
            val sessionId = java.util.UUID.randomUUID().toString()

            toSave.forEachIndexed { idx, item ->
                val sourcePath = if (isSticker) item.stickerFilePath else item.originalFilePath
                val dishTitle = item.label.ifBlank { item.mealType.label }
                val savedFile = ImageStorageNaming.saveAsNamedImage(File(sourcePath), storageDir, dishTitle)
                plateEntryDao.insertEntryWithTags(
                    entry = PlateEntryEntity(
                        title = dishTitle, note = null, fullImagePath = savedFile.absolutePath,
                        stickerImagePath = if (isSticker) savedFile.absolutePath else null,
                        thumbnailPath = savedFile.absolutePath, isStickerMode = isSticker,
                        rating = 0.0f, price = null, currency = "IDR", isFavorite = false,
                        timestamp = currentTime + idx, latitude = lat, longitude = lng,
                        locationName = locName, mealType = item.mealType, extraStickers = null,
                        plateSessionId = sessionId
                    ),
                    tags = emptyList(), tagDao = tagDao
                )
            }
            state.candidates.forEach { item ->
                if (isSticker && item.originalFilePath != item.stickerFilePath) try { File(item.originalFilePath).delete() } catch (_: Exception) {}
                else if (!isSticker && item.originalFilePath != item.stickerFilePath) try { File(item.stickerFilePath).delete() } catch (_: Exception) {}
                if (!item.isSelected) {
                    try { File(item.originalFilePath).delete() } catch (_: Exception) {}
                    try { File(item.stickerFilePath).delete() } catch (_: Exception) {}
                }
            }
            withContext(Dispatchers.Main) { resetState(); onSuccess() }
        }
    }
}
