package com.bitey.app.feature.scrapbook.model

import com.bitey.app.core.database.model.PlateEntryWithTags
import java.util.UUID

object CanvasElementOps {

    fun createFoodSticker(entry: PlateEntryWithTags, currentElements: List<CanvasElement>): CanvasElement {
        val imagePath = if (entry.entry.isStickerMode && entry.entry.stickerImagePath != null) {
            entry.entry.stickerImagePath
        } else {
            entry.entry.fullImagePath
        }

        val maxZ = currentElements.maxOfOrNull { it.zIndex } ?: 0f
        return CanvasElement(
            type = CanvasElementType.FOOD_STICKER,
            imagePath = imagePath,
            text = entry.entry.title,
            zIndex = maxZ + 1f,
            scale = 1.0f
        )
    }

    fun createAccessory(
        type: CanvasElementType,
        text: String,
        subtitle: String?,
        primaryColorHex: Long,
        secondaryColorHex: Long,
        currentElements: List<CanvasElement>
    ): CanvasElement {
        val maxZ = currentElements.maxOfOrNull { it.zIndex } ?: 0f
        return CanvasElement(
            type = type,
            text = text,
            subtitle = subtitle,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            zIndex = maxZ + 1f,
            scale = 1.0f
        )
    }

    fun updateTransform(
        elements: List<CanvasElement>,
        id: String,
        panX: Float,
        panY: Float,
        zoom: Float,
        rotationDelta: Float
    ): List<CanvasElement> {
        return elements.map { el ->
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

    fun bringToFront(elements: List<CanvasElement>, id: String): List<CanvasElement> {
        val maxZ = elements.maxOfOrNull { it.zIndex } ?: 0f
        return elements.map { el ->
            if (el.id == id) el.copy(zIndex = maxZ + 1f) else el
        }
    }

    fun sendToBack(elements: List<CanvasElement>, id: String): List<CanvasElement> {
        val minZ = elements.minOfOrNull { it.zIndex } ?: 0f
        return elements.map { el ->
            if (el.id == id) el.copy(zIndex = minZ - 1f) else el
        }
    }

    fun duplicate(elements: List<CanvasElement>, id: String): Pair<List<CanvasElement>, CanvasElement?> {
        val element = elements.find { it.id == id } ?: return Pair(elements, null)
        val maxZ = elements.maxOfOrNull { it.zIndex } ?: 0f
        val cloned = element.copy(
            id = UUID.randomUUID().toString(),
            xOffset = element.xOffset + 30f,
            yOffset = element.yOffset + 30f,
            zIndex = maxZ + 1f
        )
        return Pair(elements + cloned, cloned)
    }
}
