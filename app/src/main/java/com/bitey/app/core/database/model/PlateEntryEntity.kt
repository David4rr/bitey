package com.bitey.app.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plate_entries",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["isFavorite"])
    ]
)
data class PlateEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String? = null,
    val fullImagePath: String,
    val stickerImagePath: String? = null,
    val thumbnailPath: String,
    val isStickerMode: Boolean = true,
    val rating: Float = 5.0f,
    val price: Double? = null,
    val currency: String = "IDR",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val mealType: MealType = MealType.FOOD
)
