package com.bitey.app.feature.entry

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.FallbackStickerCropper
import com.bitey.app.core.image.FoodSubjectSegmenter
import com.bitey.app.core.image.ImagePreprocessor
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.image.SegmentationResult
import com.bitey.app.core.image.StickerCompositor
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
    private val fallbackStickerCropper: FallbackStickerCropper
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, sticker = null) }
            val result = imagePreprocessor.processUri(uri)
            result.fold(
                onSuccess = { processed ->
                    _uiState.update {
                        it.copy(
                            selectedImage = processed,
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
            val result = imagePreprocessor.processFile(file)
            result.fold(
                onSuccess = { processed ->
                    _uiState.update {
                        it.copy(
                            selectedImage = processed,
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
