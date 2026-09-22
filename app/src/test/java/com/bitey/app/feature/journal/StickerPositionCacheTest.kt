package com.bitey.app.feature.journal

import com.bitey.app.feature.journal.component.StickerPositionCache
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StickerPositionCacheTest {

    @Before
    @After
    fun setup() {
        StickerPositionCache.clear()
    }

    @Test
    fun getPosition_returnsDefaultWhenNotSet() {
        val defaultPos = Pair(10f, 20f)
        val pos = StickerPositionCache.getPosition("/images/soto.webp", defaultPos)
        assertEquals(defaultPos, pos)
    }

    @Test
    fun setPosition_persistsAcrossInvocations() {
        val path = "/images/ayam_bakar.webp"
        StickerPositionCache.setPosition(path, 45f, -80f)

        val retrieved = StickerPositionCache.getPosition(path, Pair(0f, 0f))
        assertEquals(45f, retrieved.first, 0.001f)
        assertEquals(-80f, retrieved.second, 0.001f)
    }

    @Test
    fun gravityMode_persistsPerPlate() {
        assertFalse(StickerPositionCache.isGravityEnabled("plate_1"))

        StickerPositionCache.setGravityEnabled("plate_1", true)
        assertTrue(StickerPositionCache.isGravityEnabled("plate_1"))

        StickerPositionCache.setGravityEnabled("plate_1", false)
        assertFalse(StickerPositionCache.isGravityEnabled("plate_1"))
    }

    @Test
    fun clear_resetsAllCachedData() {
        StickerPositionCache.setPosition("/path.webp", 12f, 34f)
        StickerPositionCache.setGravityEnabled("plate_99", true)

        StickerPositionCache.clear()

        assertEquals(Pair(0f, 0f), StickerPositionCache.getPosition("/path.webp", Pair(0f, 0f)))
        assertFalse(StickerPositionCache.isGravityEnabled("plate_99"))
    }
}
