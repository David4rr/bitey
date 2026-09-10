package com.bitey.app.feature.entry

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.FallbackStickerCropper
import com.bitey.app.core.image.FoodSubjectSegmenter
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.image.SegmentationResult
import com.bitey.app.core.image.StickerCompositor
import com.bitey.app.core.location.GeocoderRepository
import com.bitey.app.core.location.LocationProvider
import com.bitey.app.feature.camera.PhotoMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class StickerStyle(val label: String) {
    AI_SEGMENTED("AI Subject Cutout"),
    CIRCULAR_BADGE("Circular Plate Badge"),
    ROUNDED_TILE("Polaroid Rounded Tile")
}

data class NewEntryUiState(
    val selectedImage: ProcessedImage? = null,
    val isLoading: Boolean = false,
    val isCameraActive: Boolean = false,
    val isSegmenting: Boolean = false,
    val segmentationStatusText: String = "",
    val sticker: CompositedSticker? = null,
    val stickerStyle: StickerStyle = StickerStyle.AI_SEGMENTED,
    val showFallbackDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val imagePreprocessor: ImagePreprocessor,
    private val foodSubjectSegmenter: FoodSubjectSegmenter,
    private val stickerCompositor: StickerCompositor,
    private val fallbackStickerCropper: FallbackStickerCropper,
    private val locationProvider: LocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val plateEntryDao: PlateEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, sticker = null) }
            val coords = locationProvider.getCurrentLocation()
            val result = imagePreprocessor.processUri(uri)
            result.fold(
                onSuccess = { processed ->
                    val finalProcessed = if (processed.exifMetadata.latitude == null && coords != null) {
                        processed.copy(
                            exifMetadata = processed.exifMetadata.copy(
                                latitude = coords.latitude,
                                longitude = coords.longitude
                            )
                        )
                    } else {
                        processed
                    }
                    _uiState.update {
                        it.copy(
                            selectedImage = finalProcessed,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to process selected image."
                        )
                    }
                }
            )
        }
    }

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCameraActive = false, errorMessage = null, sticker = null) }
            val coords = locationProvider.getCurrentLocation()
            val result = imagePreprocessor.processFile(file)
            result.fold(
                onSuccess = { processed ->
                    val finalProcessed = if (processed.exifMetadata.latitude == null && coords != null) {
                        processed.copy(
                            exifMetadata = processed.exifMetadata.copy(
                                latitude = coords.latitude,
                                longitude = coords.longitude
                            )
                        )
                    } else {
                        processed
                    }
                    _uiState.update {
                        it.copy(
                            selectedImage = finalProcessed,
                            isLoading = false,
                            isCameraActive = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCameraActive = false,
                            errorMessage = error.localizedMessage ?: "Failed to process captured photo."
                        )
                    }
                }
            )
        }
    }

    /**
     * Automatically removes background, extracts geolocation, inserts marker into database,
     * and notifies completion with saved entry parameters.
     */
    fun autoProcessAndSave(
        file: File,
        photoMode: PhotoMode = PhotoMode.WHOLE_DISH,
        keepOriginal: Boolean = true,
        onSuccess: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSegmenting = true,
                    segmentationStatusText = "Optimizing photo...",
                    errorMessage = null
                )
            }

            // 1. Preprocess photo
            val processResult = imagePreprocessor.processFile(file)
            val processed = processResult.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSegmenting = false,
                        errorMessage = error.localizedMessage ?: "Failed to process photo."
                    )
                }
                return@launch
            }

            // 2. Fetch geolocation
            _uiState.update { it.copy(segmentationStatusText = "Pinning location...") }
            val coords = locationProvider.getCurrentLocation()
            val geocodedAddress = if (coords != null) {
                geocoderRepository.reverseGeocode(coords.latitude, coords.longitude)
            } else null

            // 3. AI Subject Segmentation for Die-Cut Sticker
            _uiState.update { it.copy(segmentationStatusText = "Removing background & creating die-cut sticker...") }
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(processed.file.absolutePath)
            }

            val sticker = if (bitmap != null) {
                when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                    is SegmentationResult.Success -> {
                        stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                    }
                    else -> {
                        val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                        stickerCompositor.createDieCutSticker(circular)
                    }
                }
            } else null

            val timestamp = processed.exifMetadata.capturedAtMillis ?: System.currentTimeMillis()
            val latitude = coords?.latitude ?: processed.exifMetadata?.latitude
            val longitude = coords?.longitude ?: processed.exifMetadata?.longitude

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSegmenting = false,
                    selectedImage = processed,
                    sticker = sticker
                )
            }

            onSuccess(
                -1L,
                processed.file.absolutePath,
                sticker?.file?.absolutePath,
                timestamp,
                latitude,
                longitude
            )
        }
    }

    fun autoProcessAndSaveUri(
        uri: Uri,
        photoMode: PhotoMode = PhotoMode.WHOLE_DISH,
        keepOriginal: Boolean = true,
        onSuccess: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSegmenting = true,
                    segmentationStatusText = "Reading gallery photo...",
                    errorMessage = null
                )
            }

            val processResult = imagePreprocessor.processUri(uri)
            val processed = processResult.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSegmenting = false,
                        errorMessage = error.localizedMessage ?: "Failed to process photo."
                    )
                }
                return@launch
            }

            // Geolocation from EXIF or current location
            val exifLat = processed.exifMetadata.latitude
            val exifLng = processed.exifMetadata.longitude
            val coords = if (exifLat != null && exifLng != null) {
                com.bitey.app.core.location.LocationCoordinates(exifLat, exifLng)
            } else {
                locationProvider.getCurrentLocation()
            }
            val geocodedAddress = if (coords != null) {
                geocoderRepository.reverseGeocode(coords.latitude, coords.longitude)
            } else null

            // AI Segmentation
            _uiState.update { it.copy(segmentationStatusText = "Removing background & creating die-cut sticker...") }
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(processed.file.absolutePath)
            }

            val sticker = if (bitmap != null) {
                when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                    is SegmentationResult.Success -> {
                        stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                    }
                    else -> {
                        val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                        stickerCompositor.createDieCutSticker(circular)
                    }
                }
            } else null

            val timestamp = processed.exifMetadata.capturedAtMillis ?: System.currentTimeMillis()
            val latitude = coords?.latitude
            val longitude = coords?.longitude

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSegmenting = false,
                    selectedImage = processed,
                    sticker = sticker
                )
            }

            onSuccess(
                -1L,
                processed.file.absolutePath,
                sticker?.file?.absolutePath,
                timestamp,
                latitude,
                longitude
            )
        }
    }

    fun openCamera() {
        _uiState.update { it.copy(isCameraActive = true, errorMessage = null) }
    }

    fun closeCamera() {
        _uiState.update { it.copy(isCameraActive = false) }
    }

    fun clearSelectedImage() {
        _uiState.update {
            it.copy(
                selectedImage = null,
                sticker = null,
                isSegmenting = false,
                errorMessage = null
            )
        }
    }

    fun clearSticker() {
        _uiState.update { it.copy(sticker = null, errorMessage = null) }
    }

    fun openFallbackDialog() {
        _uiState.update { it.copy(showFallbackDialog = true) }
    }

    fun dismissFallbackDialog() {
        _uiState.update { it.copy(showFallbackDialog = false) }
    }

    /**
     * Generates a die-cut sticker using the chosen [style].
     * Defaults to [StickerStyle.AI_SEGMENTED] with automated fallback prompts.
     */
    fun generateSticker(style: StickerStyle = StickerStyle.AI_SEGMENTED) {
        val image = _uiState.value.selectedImage ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSegmenting = true,
                    segmentationStatusText = "Preparing image buffer...",
                    errorMessage = null
                )
            }

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(image.file.absolutePath)
                }

                if (bitmap == null) {
                    _uiState.update {
                        it.copy(
                            isSegmenting = false,
                            errorMessage = "Unable to read optimized image for segmentation."
                        )
                    }
                    return@launch
                }

                when (style) {
                    StickerStyle.AI_SEGMENTED -> {
                        _uiState.update {
                            it.copy(segmentationStatusText = "Extracting food subject with on-device AI...")
                        }
                        when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                            is SegmentationResult.Success -> {
                                _uiState.update {
                                    it.copy(segmentationStatusText = "Compositing die-cut outline and shadow...")
                                }
                                val sticker = stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                                _uiState.update {
                                    it.copy(
                                        isSegmenting = false,
                                        segmentationStatusText = "",
                                        sticker = sticker,
                                        stickerStyle = StickerStyle.AI_SEGMENTED,
                                        errorMessage = null
                                    )
                                }
                            }
                            is SegmentationResult.NoSubjectFound -> {
                                _uiState.update {
                                    it.copy(
                                        isSegmenting = false,
                                        segmentationStatusText = "",
                                        showFallbackDialog = true,
                                        errorMessage = "No distinct food subject detected. Try a circular plate badge or Polaroid tile."
                                    )
                                }
                            }
                            is SegmentationResult.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        isSegmenting = false,
                                        segmentationStatusText = "",
                                        showFallbackDialog = true,
                                        errorMessage = "Segmentation unavailable: ${segResult.error.localizedMessage ?: "Unknown error"}. Try circular crop."
                                    )
                                }
                            }
                        }
                    }
                    StickerStyle.CIRCULAR_BADGE -> {
                        _uiState.update {
                            it.copy(segmentationStatusText = "Creating circular plate crop...")
                        }
                        val circularSubject = fallbackStickerCropper.createCircularSubject(bitmap)
                        _uiState.update {
                            it.copy(segmentationStatusText = "Compositing die-cut outline and shadow...")
                        }
                        val sticker = stickerCompositor.createDieCutSticker(circularSubject)
                        _uiState.update {
                            it.copy(
                                isSegmenting = false,
                                segmentationStatusText = "",
                                sticker = sticker,
                                stickerStyle = StickerStyle.CIRCULAR_BADGE,
                                showFallbackDialog = false,
                                errorMessage = null
                            )
                        }
                    }
                    StickerStyle.ROUNDED_TILE -> {
                        _uiState.update {
                            it.copy(segmentationStatusText = "Creating Polaroid tile crop...")
                        }
                        val roundedSubject = fallbackStickerCropper.createRoundedRectSubject(bitmap)
                        _uiState.update {
                            it.copy(segmentationStatusText = "Compositing die-cut outline and shadow...")
                        }
                        val sticker = stickerCompositor.createDieCutSticker(roundedSubject)
                        _uiState.update {
                            it.copy(
                                isSegmenting = false,
                                segmentationStatusText = "",
                                sticker = sticker,
                                stickerStyle = StickerStyle.ROUNDED_TILE,
                                showFallbackDialog = false,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSegmenting = false,
                        segmentationStatusText = "",
                        errorMessage = e.localizedMessage ?: "Error during sticker creation."
                    )
                }
            }
        }
    }
}
