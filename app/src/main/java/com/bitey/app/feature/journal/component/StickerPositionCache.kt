package com.bitey.app.feature.journal.component

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory cache for book cover sticker positions and gravity modes.
 * Retains custom sticker offsets across page navigation and pager recycling.
 */
object StickerPositionCache {
    private val positions = ConcurrentHashMap<String, Pair<Float, Float>>()
    private val gravityModes = ConcurrentHashMap<String, Boolean>()

    fun getPosition(fileKey: String, default: Pair<Float, Float>): Pair<Float, Float> {
        return positions[fileKey] ?: default
    }

    fun setPosition(fileKey: String, x: Float, y: Float) {
        positions[fileKey] = Pair(x, y)
    }

    fun isGravityEnabled(plateId: String, default: Boolean = false): Boolean {
        return gravityModes[plateId] ?: default
    }

    fun setGravityEnabled(plateId: String, enabled: Boolean) {
        gravityModes[plateId] = enabled
    }

    fun clear() {
        positions.clear()
        gravityModes.clear()
    }
}
