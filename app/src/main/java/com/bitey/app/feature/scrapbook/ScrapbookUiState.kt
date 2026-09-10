package com.bitey.app.feature.scrapbook

import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement

data class ScrapbookUiState(
    val elements: List<CanvasElement> = emptyList(),
    val selectedElementId: String? = null,
    val aspectRatio: CanvasAspectRatio = CanvasAspectRatio.STORY_9_16,
    val backgroundColorHex: Long = 0xFFF4F1EA, // Soft cream tactile background
    val availableFoodEntries: List<PlateEntryWithTags> = emptyList(),
    val isFoodStickerSheetOpen: Boolean = false,
    val isAccessorySheetOpen: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null
)
