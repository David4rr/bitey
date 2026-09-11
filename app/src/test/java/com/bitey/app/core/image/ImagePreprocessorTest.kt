package com.bitey.app.core.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImagePreprocessorTest {

    // Dummy test double for Context and ExifMetadataExtractor when testing pure calculation
    private val testContext = TestContext()
    private val preprocessor = ImagePreprocessor(
        context = testContext,
        exifMetadataExtractor = ExifMetadataExtractor(testContext)
    )

    @Test
    fun calculateInSampleSize_smallerThanTarget_returns1() {
        val sampleSize = preprocessor.calculateInSampleSize(800, 600, 1920, 1080)
        assertEquals(1, sampleSize)
    }

    @Test
    fun calculateInSampleSize_exactTarget_returns1() {
        val sampleSize = preprocessor.calculateInSampleSize(1920, 1080, 1920, 1080)
        assertEquals(1, sampleSize)
    }

    @Test
    fun calculateInSampleSize_12Megapixels_returnsPowerOfTwo() {
        // 4032 x 3024 (standard 12MP smartphone photo)
        val sampleSize = preprocessor.calculateInSampleSize(4032, 3024, 1920, 1080)
        assertEquals(2, sampleSize)
        // 4032 / 2 = 2016, 3024 / 2 = 1512
    }

    @Test
    fun calculateInSampleSize_48Megapixels_returns4() {
        // 8000 x 6000 (standard 48MP smartphone photo)
        val sampleSize = preprocessor.calculateInSampleSize(8000, 6000, 1920, 1080)
        assertEquals(4, sampleSize)
        // 8000 / 4 = 2000, 6000 / 4 = 1500
    }
}
