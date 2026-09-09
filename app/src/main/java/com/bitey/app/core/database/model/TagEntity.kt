package com.bitey.app.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["tagName"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,
    val tagName: String,
    val category: String? = null // e.g. TASTE, AMBIENCE, MOOD, TYPE
)
