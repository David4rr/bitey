package com.bitey.app.feature.entry

import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.TagEntity

data class EntryEditorUiState(
    val imagePath: String = "",
    val stickerPath: String? = null,
    val isStickerMode: Boolean = true,
    val dishTitle: String = "Food",
    val notes: String = "",
    val mealType: MealType = MealType.FOOD,
    val rating: Float = 5.0f,
    val priceString: String = "",
    val currency: String = "IDR",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val geocodedAddress: String? = null,
    val isLocating: Boolean = false,
    val selectedTags: Set<TagEntity> = emptySet(),
    val availableTags: List<TagEntity> = emptyList(),
    val isSaving: Boolean = false,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null,
    val isNewTagDialogOpen: Boolean = false
) {
    fun toPlateEntryEntity(): PlateEntryEntity {
        return PlateEntryEntity(
            title = dishTitle.trim().ifBlank { mealType.label },
            note = notes.trim().takeIf { it.isNotBlank() },
            fullImagePath = imagePath,
            stickerImagePath = stickerPath,
            thumbnailPath = stickerPath ?: imagePath,
            isStickerMode = isStickerMode,
            rating = rating,
            price = priceString.toDoubleOrNull(),
            currency = currency,
            isFavorite = isFavorite,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName.trim().takeIf { it.isNotBlank() } ?: geocodedAddress,
            mealType = mealType
        )
    }
}
