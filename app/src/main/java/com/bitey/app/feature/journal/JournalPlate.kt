package com.bitey.app.feature.journal

import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import java.io.File

/**
 * Represents a consolidated "Plate" or dining session.
 * Automatically merges dishes recorded at the same venue around the same time,
 * or captured together in the same camera session.
 */
data class JournalPlate(
    val id: String,
    val venueName: String?,
    val timestamp: Long,
    val entries: List<PlateEntryWithTags>
) {
    val isMergedPlate: Boolean get() = entries.size > 1
    val primaryEntry: PlateEntryEntity get() = entries.first().entry

    val title: String get() {
        return if (!venueName.isNullOrBlank()) {
            venueName
        } else if (entries.size == 1) {
            entries.first().entry.title
        } else {
            "Dining Plate (${entries.size} dishes)"
        }
    }

    val subtitle: String get() {
        return if (entries.size == 1) {
            entries.first().entry.mealType.label
        } else {
            "${entries.size} dishes • ${entries.joinToString(" • ") { it.entry.title }}"
        }
    }

    val isFavorite: Boolean get() = entries.any { it.entry.isFavorite }
    val isStickerMode: Boolean get() = entries.any { it.entry.isStickerMode }

    val allStickerPaths: List<String> get() = entries.flatMap { it.entry.getIndividualStickerPaths() }.distinct()

    val allImageFiles: List<File> get() {
        return if (isStickerMode && allStickerPaths.isNotEmpty()) {
            allStickerPaths.map { File(it) }
        } else {
            entries.map { File(it.entry.fullImagePath) }
        }
    }

    val averageRating: Float get() {
        val rated = entries.filter { it.entry.rating > 0f }
        return if (rated.isNotEmpty()) rated.map { it.entry.rating }.average().toFloat() else 0f
    }
}
