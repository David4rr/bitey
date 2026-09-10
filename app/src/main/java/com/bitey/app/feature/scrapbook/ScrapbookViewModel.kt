package com.bitey.app.feature.scrapbook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.feature.scrapbook.export.CanvasBitmapRenderer
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScrapbookViewModel @Inject constructor(
    private val plateEntryDao: PlateEntryDao,
    private val canvasBitmapRenderer: CanvasBitmapRenderer
) : ViewModel() {

    private val _elements = MutableStateFlow<List<CanvasElement>>(emptyList())
    private val _selectedElementId = MutableStateFlow<String?>(null)
    private val _aspectRatio = MutableStateFlow(CanvasAspectRatio.STORY_9_16)
    private val _backgroundColorHex = MutableStateFlow(0xFFF4F1EA)
    private val _isFoodStickerSheetOpen = MutableStateFlow(false)
    private val _isAccessorySheetOpen = MutableStateFlow(false)
    private val _isExporting = MutableStateFlow(false)
    private val _exportSuccessMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ScrapbookUiState> = combine(
        plateEntryDao.getAllEntriesWithTags(),
        _elements,
        _selectedElementId,
        _aspectRatio,
        _backgroundColorHex,
        _isFoodStickerSheetOpen,
        _isAccessorySheetOpen,
        _isExporting,
        _exportSuccessMessage
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val entries = args[0] as List<PlateEntryWithTags>
        @Suppress("UNCHECKED_CAST")
        val elements = args[1] as List<CanvasElement>
        val selectedId = args[2] as String?
        val ratio = args[3] as CanvasAspectRatio
        val bgColor = args[4] as Long
        val isFoodSheet = args[5] as Boolean
        val isAccessorySheet = args[6] as Boolean
        val isExporting = args[7] as Boolean
        val exportMessage = args[8] as String?

        ScrapbookUiState(
            elements = elements,
            selectedElementId = selectedId,
            aspectRatio = ratio,
            backgroundColorHex = bgColor,
            availableFoodEntries = entries,
            isFoodStickerSheetOpen = isFoodSheet,
            isAccessorySheetOpen = isAccessorySheet,
            isExporting = isExporting,
            exportSuccessMessage = exportMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ScrapbookUiState()
    )

    fun addFoodSticker(entry: PlateEntryWithTags) {
        val imagePath = if (entry.entry.isStickerMode && entry.entry.stickerImagePath != null) {
            entry.entry.stickerImagePath
        } else {
            entry.entry.fullImagePath
        }

        val maxZ = _elements.value.maxOfOrNull { it.zIndex } ?: 0f
        val newElement = CanvasElement(
            type = CanvasElementType.FOOD_STICKER,
            imagePath = imagePath,
            text = entry.entry.title,
            zIndex = maxZ + 1f,
            scale = 1.0f
        )

        _elements.update { it + newElement }
        _selectedElementId.value = newElement.id
        _isFoodStickerSheetOpen.value = false
    }

    fun addAccessory(
        type: CanvasElementType,
        text: String,
        subtitle: String? = null,
        primaryColorHex: Long = 0xFFFF6B35,
        secondaryColorHex: Long = 0xFFFFFFFF
    ) {
        val maxZ = _elements.value.maxOfOrNull { it.zIndex } ?: 0f
        val newElement = CanvasElement(
            type = type,
            text = text,
            subtitle = subtitle,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            zIndex = maxZ + 1f,
            scale = 1.0f
        )

        _elements.update { it + newElement }
        _selectedElementId.value = newElement.id
        _isAccessorySheetOpen.value = false
    }

    fun selectElement(id: String?) {
        _selectedElementId.value = id
    }

    fun updateElementTransform(id: String, panX: Float, panY: Float, zoom: Float, rotationDelta: Float) {
        _elements.update { list ->
            list.map { el ->
                if (el.id == id) {
                    el.copy(
                        xOffset = el.xOffset + panX,
                        yOffset = el.yOffset + panY,
                        scale = (el.scale * zoom).coerceIn(0.4f, 3.5f),
                        rotation = (el.rotation + rotationDelta) % 360f
                    )
                } else {
                    el
                }
            }
        }
    }

    fun bringToFront(id: String) {
        val maxZ = _elements.value.maxOfOrNull { it.zIndex } ?: 0f
        _elements.update { list ->
            list.map { el ->
                if (el.id == id) el.copy(zIndex = maxZ + 1f) else el
            }
        }
    }

    fun sendToBack(id: String) {
        val minZ = _elements.value.minOfOrNull { it.zIndex } ?: 0f
        _elements.update { list ->
            list.map { el ->
                if (el.id == id) el.copy(zIndex = minZ - 1f) else el
            }
        }
    }

    fun duplicateElement(id: String) {
        val element = _elements.value.find { it.id == id } ?: return
        val maxZ = _elements.value.maxOfOrNull { it.zIndex } ?: 0f
        val cloned = element.copy(
            id = UUID.randomUUID().toString(),
            xOffset = element.xOffset + 30f,
            yOffset = element.yOffset + 30f,
            zIndex = maxZ + 1f
        )
        _elements.update { it + cloned }
        _selectedElementId.value = cloned.id
    }

    fun deleteElement(id: String) {
        _elements.update { list -> list.filterNot { it.id == id } }
        if (_selectedElementId.value == id) {
            _selectedElementId.value = null
        }
    }

    fun setAspectRatio(aspectRatio: CanvasAspectRatio) {
        _aspectRatio.value = aspectRatio
    }

    fun setBackgroundColor(colorHex: Long) {
        _backgroundColorHex.value = colorHex
    }

    fun openFoodStickerSheet(open: Boolean) {
        _isFoodStickerSheetOpen.value = open
    }

    fun openAccessorySheet(open: Boolean) {
        _isAccessorySheetOpen.value = open
    }

    fun clearCanvas() {
        _elements.value = emptyList()
        _selectedElementId.value = null
    }

    fun exportToGallery(
        viewportWidthPx: Float,
        viewportHeightPx: Float,
        onComplete: (Uri?) -> Unit
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val bitmap = canvasBitmapRenderer.renderToBitmap(
                    elements = _elements.value,
                    aspectRatio = _aspectRatio.value,
                    backgroundColorHex = _backgroundColorHex.value,
                    viewportWidthPx = viewportWidthPx,
                    viewportHeightPx = viewportHeightPx
                )
                val uri = canvasBitmapRenderer.exportToGallery(bitmap, "Scrapbook")
                bitmap.recycle()
                _exportSuccessMessage.value = if (uri != null) "Saved to Pictures/Bitey in your gallery." else "Export failed."
                onComplete(uri)
            } catch (e: Exception) {
                _exportSuccessMessage.value = "Export failed: ${e.message}"
                onComplete(null)
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportToShare(
        viewportWidthPx: Float,
        viewportHeightPx: Float,
        onComplete: (File?) -> Unit
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val bitmap = canvasBitmapRenderer.renderToBitmap(
                    elements = _elements.value,
                    aspectRatio = _aspectRatio.value,
                    backgroundColorHex = _backgroundColorHex.value,
                    viewportWidthPx = viewportWidthPx,
                    viewportHeightPx = viewportHeightPx
                )
                val file = canvasBitmapRenderer.saveToTemporaryCache(bitmap)
                bitmap.recycle()
                onComplete(file)
            } catch (e: Exception) {
                onComplete(null)
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportMessage() {
        _exportSuccessMessage.value = null
    }
}
