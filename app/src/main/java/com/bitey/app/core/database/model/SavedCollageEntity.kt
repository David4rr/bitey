package com.bitey.app.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_collages")
data class SavedCollageEntity(
    @PrimaryKey(autoGenerate = true)
    val collageId: Long = 0,
    val title: String,
    val canvasDataJson: String,
    val previewImagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
