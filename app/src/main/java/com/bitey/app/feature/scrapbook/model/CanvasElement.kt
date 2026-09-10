package com.bitey.app.feature.scrapbook.model

import java.util.UUID

enum class CanvasElementType {
    FOOD_STICKER,
    DATE_STAMP,
    WASHI_TAPE,
    LOCATION_TAG,
    RATING_BADGE,
    MOOD_CHIP
}

enum class CanvasAspectRatio(val widthRatio: Float, val heightRatio: Float, val label: String) {
    STORY_9_16(9f, 16f, "9:16 Story"),
    SQUARE_1_1(1f, 1f, "1:1 Square")
}

data class CanvasElement(
    val id: String = UUID.randomUUID().toString(),
    val type: CanvasElementType,
    val imagePath: String? = null,
    val text: String? = null,
    val subtitle: String? = null,
    val primaryColorHex: Long = 0xFFFF6B35,
    val secondaryColorHex: Long = 0xFFFFFFFF,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val zIndex: Float = 0f
)
