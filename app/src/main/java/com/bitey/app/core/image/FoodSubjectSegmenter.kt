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
            val rawForeground = result.foregroundBitmap?.let { ensureSoftwareBitmap(it) }

            if (rawForeground != null && hasVisibleContent(rawForeground)) {
                SegmentationResult.Success(rawForeground)
            } else {
                SegmentationResult.NoSubjectFound
            }
        } catch (e: Exception) {
            android.util.Log.e("FoodSubjectSegmenter", "ML Kit segmentation error: ${e.message}", e)
            SegmentationResult.Failure(e)
        } finally {
            scaledBitmap?.recycle()
        }
    }

    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        } else {
            bitmap
        }
    }

    /**
     * Samples the alpha channel across a grid to ensure the segmentation output
     * is not an empty/invisible bitmap.
     */
    private fun hasVisibleContent(bitmap: Bitmap): Boolean {
        val safe = ensureSoftwareBitmap(bitmap)
        val width = safe.width
        val height = safe.height
        val stepX = (width / 30).coerceAtLeast(1)
        val stepY = (height / 30).coerceAtLeast(1)

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = safe.getPixel(x, y)
                val alpha = (pixel ushr 24) and 0xFF
                if (alpha > 35) {
                    if (safe != bitmap) safe.recycle()
                    return true
                }
            }
        }
        if (safe != bitmap) safe.recycle()
        return false
    }
}
