package com.bitey.app.core.image

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SegmentationResult {
    data class Success(val foregroundBitmap: Bitmap) : SegmentationResult
    data object NoSubjectFound : SegmentationResult
    data class Failure(val error: Throwable) : SegmentationResult
}

@Singleton
class FoodSubjectSegmenter @Inject constructor() {

    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .enableForegroundConfidenceMask()
        .build()

    private val segmenter: SubjectSegmenter by lazy {
        SubjectSegmentation.getClient(options)
    }

    suspend fun segment(bitmap: Bitmap): SegmentationResult = withContext(Dispatchers.Default) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = segmenter.process(inputImage).await()
            val foreground = result.foregroundBitmap

            if (foreground != null && hasVisibleContent(foreground)) {
                SegmentationResult.Success(foreground)
            } else {
                SegmentationResult.NoSubjectFound
            }
        } catch (e: Exception) {
            SegmentationResult.Failure(e)
        }
    }

    /**
     * Samples the alpha channel across a grid to ensure the segmentation output
     * is not an empty/invisible bitmap.
     */
    private fun hasVisibleContent(bitmap: Bitmap): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val stepX = (width / 30).coerceAtLeast(1)
        val stepY = (height / 30).coerceAtLeast(1)

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (pixel ushr 24) and 0xFF
                if (alpha > 35) {
                    return true
                }
            }
        }
        return false
    }
}
