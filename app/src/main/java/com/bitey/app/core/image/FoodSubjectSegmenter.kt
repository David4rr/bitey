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
    data class Success(val foregroundBitmap: Bitmap, val subjectBitmaps: List<Bitmap> = emptyList()) : SegmentationResult
    data object NoSubjectFound : SegmentationResult
    data class Failure(val error: Throwable) : SegmentationResult
}

@Singleton
class FoodSubjectSegmenter @Inject constructor() {

    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()

    private val segmenter: SubjectSegmenter by lazy {
        SubjectSegmentation.getClient(options)
    }

    suspend fun segment(bitmap: Bitmap): SegmentationResult = withContext(Dispatchers.Default) {
        var scaledBitmap: Bitmap? = null
        try {
            val maxDim = kotlin.math.max(bitmap.width, bitmap.height)
            val inputBitmap = if (maxDim > 800) {
                val scale = 800f / maxDim
                val targetW = kotlin.math.max(1, (bitmap.width * scale).toInt())
                val targetH = kotlin.math.max(1, (bitmap.height * scale).toInt())
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true).also { scaledBitmap = it }
            } else {
                bitmap
            }

            val inputImage = InputImage.fromBitmap(inputBitmap, 0)
            val result = segmenter.process(inputImage).await()
            val foreground = result.foregroundBitmap
            val subjectBitmaps = result.subjects.mapNotNull { it.bitmap }.filter { hasVisibleContent(it) }

            if (foreground != null && hasVisibleContent(foreground)) {
                SegmentationResult.Success(foreground, subjectBitmaps)
            } else if (subjectBitmaps.isNotEmpty()) {
                SegmentationResult.Success(subjectBitmaps.first(), subjectBitmaps)
            } else {
                SegmentationResult.NoSubjectFound
            }
        } catch (e: Exception) {
            SegmentationResult.Failure(e)
        } finally {
            scaledBitmap?.recycle()
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
