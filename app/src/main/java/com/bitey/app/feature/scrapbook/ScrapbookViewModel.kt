package com.bitey.app.feature.scrapbook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.feature.scrapbook.export.CanvasBitmapRenderer
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementOps
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
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
        _elements, _selectedElementId, _aspectRatio,
        _backgroundColorHex, _isFoodStickerSheetOpen,
        _isAccessorySheetOpen, _isExporting, _exportSuccessMessage
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        ScrapbookUiState(
            availableFoodEntries = args[0] as List<PlateEntryWithTags>,
            elements = args[1] as List<CanvasElement>,
            selectedElementId = args[2] as String?,
            aspectRatio = args[3] as CanvasAspectRatio,
            backgroundColorHex = args[4] as Long,
            isFoodStickerSheetOpen = args[5] as Boolean,
            isAccessorySheetOpen = args[6] as Boolean,
            isExporting = args[7] as Boolean,
            exportSuccessMessage = args[8] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ScrapbookUiState())

    fun addFoodSticker(entry: PlateEntryWithTags) {
        val newElement = CanvasElementOps.createFoodSticker(entry, _elements.value)
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
        val newElement = CanvasElementOps.createAccessory(type, text, subtitle, primaryColorHex, secondaryColorHex, _elements.value)
        _elements.update { it + newElement }
        _selectedElementId.value = newElement.id
        _isAccessorySheetOpen.value = false
    }

    fun selectElement(id: String?) { _selectedElementId.value = id }

    fun updateElementTransform(id: String, panX: Float, panY: Float, zoom: Float, rotationDelta: Float) {
        _elements.update { CanvasElementOps.updateTransform(it, id, panX, panY, zoom, rotationDelta) }
    }

    fun bringToFront(id: String) { _elements.update { CanvasElementOps.bringToFront(it, id) } }
    fun sendToBack(id: String) { _elements.update { CanvasElementOps.sendToBack(it, id) } }

    fun duplicateElement(id: String) {
        val (updated, cloned) = CanvasElementOps.duplicate(_elements.value, id)
        _elements.value = updated
        cloned?.let { _selectedElementId.value = it.id }
    }

    fun deleteElement(id: String) {
        _elements.update { list -> list.filterNot { it.id == id } }
        if (_selectedElementId.value == id) _selectedElementId.value = null
    }

    fun setAspectRatio(aspectRatio: CanvasAspectRatio) { _aspectRatio.value = aspectRatio }
    fun setBackgroundColor(colorHex: Long) { _backgroundColorHex.value = colorHex }
    fun openFoodStickerSheet(open: Boolean) { _isFoodStickerSheetOpen.value = open }
    fun openAccessorySheet(open: Boolean) { _isAccessorySheetOpen.value = open }
    fun clearCanvas() {
        _elements.value = emptyList()
        _selectedElementId.value = null
    }

    fun exportToGallery(viewportWidthPx: Float, viewportHeightPx: Float, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val bitmap = canvasBitmapRenderer.renderToBitmap(
                    elements = _elements.value, aspectRatio = _aspectRatio.value,
                    backgroundColorHex = _backgroundColorHex.value,
                    viewportWidthPx = viewportWidthPx, viewportHeightPx = viewportHeightPx
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

    fun exportToShare(viewportWidthPx: Float, viewportHeightPx: Float, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val bitmap = canvasBitmapRenderer.renderToBitmap(
                    elements = _elements.value, aspectRatio = _aspectRatio.value,
                    backgroundColorHex = _backgroundColorHex.value,
                    viewportWidthPx = viewportWidthPx, viewportHeightPx = viewportHeightPx
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

    fun clearExportMessage() { _exportSuccessMessage.value = null }
}
