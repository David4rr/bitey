package com.bitey.app.feature.entry

import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.ProcessedImage

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
