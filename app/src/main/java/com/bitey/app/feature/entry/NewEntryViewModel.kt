package com.bitey.app.feature.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.location.LocationCoordinates
import com.bitey.app.core.location.LocationProvider
import com.bitey.app.feature.camera.PhotoMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val stickerPipeline: StickerPipeline,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()

    private fun withLocationFallback(processed: ProcessedImage, coords: LocationCoordinates?): ProcessedImage {
        return if (processed.exifMetadata.latitude == null && coords != null) {
            processed.copy(exifMetadata = processed.exifMetadata.copy(latitude = coords.latitude, longitude = coords.longitude))
        } else processed
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, sticker = null) }
            val coords = locationProvider.getCurrentLocation()
            imagePreprocessor.processUri(uri).fold(
                onSuccess = { processed ->
                    _uiState.update { it.copy(selectedImage = withLocationFallback(processed, coords), isLoading = false, errorMessage = null) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Failed to process selected image.") }
                }
            )
        }
    }

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCameraActive = false, errorMessage = null, sticker = null) }
            val coords = locationProvider.getCurrentLocation()
            imagePreprocessor.processFile(file).fold(
                onSuccess = { processed ->
                    _uiState.update { it.copy(selectedImage = withLocationFallback(processed, coords), isLoading = false, isCameraActive = false, errorMessage = null) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, isCameraActive = false, errorMessage = error.localizedMessage ?: "Failed to process captured photo.") }
                }
            )
        }
    }

    fun autoProcessAndSave(
        file: File,
        photoMode: PhotoMode = PhotoMode.WHOLE_DISH,
        keepOriginal: Boolean = true,
        onSuccess: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSegmenting = true, segmentationStatusText = "Optimizing photo...", errorMessage = null) }
            val processed = imagePreprocessor.processFile(file).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, isSegmenting = false, errorMessage = error.localizedMessage ?: "Failed to process photo.") }
                return@launch
            }
            _uiState.update { it.copy(segmentationStatusText = "Pinning location...") }
            val coords = locationProvider.getCurrentLocation()
            _uiState.update { it.copy(segmentationStatusText = "Removing background & creating die-cut sticker...") }
            val sticker = stickerPipeline.createStickerWithFallback(processed.file)
            val timestamp = processed.exifMetadata.capturedAtMillis ?: System.currentTimeMillis()
            val latitude = coords?.latitude ?: processed.exifMetadata.latitude
            val longitude = coords?.longitude ?: processed.exifMetadata.longitude
            _uiState.update { it.copy(isLoading = false, isSegmenting = false, selectedImage = processed, sticker = sticker) }
            onSuccess(-1L, processed.file.absolutePath, sticker?.file?.absolutePath, timestamp, latitude, longitude)
        }
    }

    fun autoProcessAndSaveUri(
        uri: Uri,
        photoMode: PhotoMode = PhotoMode.WHOLE_DISH,
        keepOriginal: Boolean = true,
        onSuccess: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSegmenting = true, segmentationStatusText = "Reading gallery photo...", errorMessage = null) }
            val processed = imagePreprocessor.processUri(uri).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, isSegmenting = false, errorMessage = error.localizedMessage ?: "Failed to process photo.") }
                return@launch
            }
            val exifLat = processed.exifMetadata.latitude
            val exifLng = processed.exifMetadata.longitude
            val coords = if (exifLat != null && exifLng != null) LocationCoordinates(exifLat, exifLng) else locationProvider.getCurrentLocation()
            _uiState.update { it.copy(segmentationStatusText = "Removing background & creating die-cut sticker...") }
            val sticker = stickerPipeline.createStickerWithFallback(processed.file)
            val timestamp = processed.exifMetadata.capturedAtMillis ?: System.currentTimeMillis()
            _uiState.update { it.copy(isLoading = false, isSegmenting = false, selectedImage = processed, sticker = sticker) }
            onSuccess(-1L, processed.file.absolutePath, sticker?.file?.absolutePath, timestamp, coords?.latitude, coords?.longitude)
        }
    }

    fun openCamera() { _uiState.update { it.copy(isCameraActive = true, errorMessage = null) } }
    fun closeCamera() { _uiState.update { it.copy(isCameraActive = false) } }
    fun clearSelectedImage() { _uiState.update { it.copy(selectedImage = null, sticker = null, isSegmenting = false, errorMessage = null) } }
    fun clearSticker() { _uiState.update { it.copy(sticker = null, errorMessage = null) } }
    fun openFallbackDialog() { _uiState.update { it.copy(showFallbackDialog = true) } }
    fun dismissFallbackDialog() { _uiState.update { it.copy(showFallbackDialog = false) } }

    fun generateSticker(style: StickerStyle = StickerStyle.AI_SEGMENTED) {
        val image = _uiState.value.selectedImage ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSegmenting = true, segmentationStatusText = "Preparing image buffer...", errorMessage = null) }
            when (val result = stickerPipeline.generateSticker(image.file, style) { msg -> _uiState.update { it.copy(segmentationStatusText = msg) } }) {
                is StickerGenerationResult.Success -> {
                    _uiState.update { it.copy(isSegmenting = false, segmentationStatusText = "", sticker = result.sticker, stickerStyle = style, errorMessage = null, showFallbackDialog = false) }
                }
                is StickerGenerationResult.FallbackNeeded -> {
                    _uiState.update { it.copy(isSegmenting = false, segmentationStatusText = "", showFallbackDialog = true, errorMessage = result.reason) }
                }
                is StickerGenerationResult.Failure -> {
                    _uiState.update { it.copy(isSegmenting = false, segmentationStatusText = "", errorMessage = result.error.localizedMessage ?: "Error during sticker creation.") }
                }
            }
        }
    }
}
