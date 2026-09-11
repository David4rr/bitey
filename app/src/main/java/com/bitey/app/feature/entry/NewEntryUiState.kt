package com.bitey.app.feature.entry

import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.ProcessedImage

enum class StickerStyle(val label: String) {
    AI_SEGMENTED("AI Subject Cutout"),
    CIRCULAR_BADGE("Circular Plate Badge"),
    ROUNDED_TILE("Polaroid Rounded Tile")
}

data class NewEntryUiState(
    val step: com.bitey.app.feature.camera.CaptureFlowStep = com.bitey.app.feature.camera.CaptureFlowStep.CAMERA,
    val photoMode: com.bitey.app.feature.camera.PhotoMode = com.bitey.app.feature.camera.PhotoMode.ONESHOT,
    val capturedDishes: List<java.io.File> = emptyList(),
    val processingSourceFile: String? = null,
    val processingStickerFile: String? = null,
    val candidates: List<com.bitey.app.feature.camera.CandidateStickerItem> = emptyList(),
    val dishName: String = "Food",
    val mealType: com.bitey.app.core.database.model.MealType = com.bitey.app.core.database.model.MealType.FOOD,
    val showManualCutDialog: Boolean = false,
    val selectedImage: ProcessedImage? = null,
    val isLoading: Boolean = false,
    val isCameraActive: Boolean = false,
    val isSegmenting: Boolean = false,
    val segmentationStatusText: String = "",
    val sticker: CompositedSticker? = null,
    val stickerStyle: StickerStyle = StickerStyle.AI_SEGMENTED,
    val showFallbackDialog: Boolean = false,
    val errorMessage: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
)
