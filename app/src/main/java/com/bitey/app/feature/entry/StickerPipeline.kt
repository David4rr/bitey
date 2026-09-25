package com.bitey.app.feature.entry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.FallbackStickerCropper
import com.bitey.app.core.image.FoodSubjectSegmenter
import com.bitey.app.core.image.SegmentationResult
import com.bitey.app.core.image.StickerCompositor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface StickerGenerationResult {
    data class Success(val sticker: CompositedSticker) : StickerGenerationResult
    data class FallbackNeeded(val reason: String) : StickerGenerationResult
    data class Failure(val error: Throwable) : StickerGenerationResult
}

@Singleton
class StickerPipeline @Inject constructor(
    private val foodSubjectSegmenter: FoodSubjectSegmenter,
    private val stickerCompositor: StickerCompositor,
    private val fallbackStickerCropper: FallbackStickerCropper
) {

    suspend fun createStickerWithFallback(file: File): CompositedSticker? = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(file) ?: return@withContext null
        try {
            when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                is SegmentationResult.Success -> {
                    try {
                        stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                    } catch (e: Exception) {
                        android.util.Log.e("StickerPipeline", "createDieCutSticker failed, trying circular fallback", e)
                        val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                        try {
                            stickerCompositor.createDieCutSticker(circular)
                        } finally {
                            circular.recycle()
                        }
                    } finally {
                        segResult.foregroundBitmap.recycle()
                    }
                }
                else -> {
                    val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                    try {
                        stickerCompositor.createDieCutSticker(circular)
                    } finally {
                        circular.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("StickerPipeline", "Failed to create sticker for file ${file.name}", e)
            null
        } finally {
            bitmap.recycle()
        }
    }

    suspend fun createStickersForMultiSubject(file: File): List<CompositedSticker> = withContext(Dispatchers.IO) {
        val sticker = createStickerWithFallback(file)
        if (sticker != null) listOf(sticker) else emptyList()
    }

    suspend fun createManualSticker(file: File, style: StickerStyle): CompositedSticker? = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(file) ?: return@withContext null
        try {
            val subject = when (style) {
                StickerStyle.CIRCULAR_BADGE -> fallbackStickerCropper.createCircularSubject(bitmap)
                StickerStyle.ROUNDED_TILE -> fallbackStickerCropper.createRoundedRectSubject(bitmap)
                else -> fallbackStickerCropper.createCircularSubject(bitmap)
            }
            try {
                stickerCompositor.createDieCutSticker(subject)
            } finally {
                subject.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    suspend fun createSmartCircleSticker(
        file: File, normCx: Float, normCy: Float, normR: Float
    ): CompositedSticker? = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(file) ?: return@withContext null
        try {
            val cx = (normCx * bitmap.width).toInt().coerceIn(0, bitmap.width)
            val cy = (normCy * bitmap.height).toInt().coerceIn(0, bitmap.height)
            val r = (normR * minOf(bitmap.width, bitmap.height)).toInt().coerceIn(16, maxOf(bitmap.width, bitmap.height))
            val left = (cx - r).coerceAtLeast(0)
            val top = (cy - r).coerceAtLeast(0)
            val w = (cx + r).coerceAtMost(bitmap.width) - left
            val h = (cy + r).coerceAtMost(bitmap.height) - top
            if (w <= 0 || h <= 0) return@withContext null

            val cropped = Bitmap.createBitmap(bitmap, left, top, w, h)
            val subject = when (val seg = foodSubjectSegmenter.segment(cropped)) {
                is SegmentationResult.Success -> seg.foregroundBitmap
                else -> fallbackStickerCropper.createCircularSubject(cropped)
            }
            if (cropped != bitmap && cropped != subject) cropped.recycle()
            try {
                stickerCompositor.createDieCutSticker(subject)
            } finally {
                subject.recycle()
            }
        } catch (e: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampledBitmap(file: File, maxDim: Int = 1024): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while ((largest / sampleSize) > maxDim * 1.5) { sampleSize *= 2 }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null

        val rotationDegrees = runCatching {
            val exif = ExifInterface(file.absolutePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
                ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
                ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
                else -> 0
            }
        }.getOrDefault(0)

        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
                if (it != decoded) decoded.recycle()
            }
        } else {
            decoded
        }
    }

    suspend fun generateSticker(
        file: File,
        style: StickerStyle,
        onStatus: (String) -> Unit = {}
    ): StickerGenerationResult = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(file, maxDim = 1280)
            ?: return@withContext StickerGenerationResult.Failure(IllegalStateException("Unable to read image."))
        try {
            when (style) {
                StickerStyle.AI_SEGMENTED -> {
                    onStatus("Extracting food subject with on-device AI...")
                    when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                        is SegmentationResult.Success -> {
                            onStatus("Compositing die-cut outline and shadow...")
                            val sticker = try { stickerCompositor.createDieCutSticker(segResult.foregroundBitmap) } finally { segResult.foregroundBitmap.recycle() }
                            StickerGenerationResult.Success(sticker)
                        }
                        is SegmentationResult.NoSubjectFound -> StickerGenerationResult.FallbackNeeded("No distinct food subject detected.")
                        is SegmentationResult.Failure -> StickerGenerationResult.FallbackNeeded("Segmentation unavailable: ${segResult.error.localizedMessage ?: "Unknown error"}")
                    }
                }
                StickerStyle.CIRCULAR_BADGE -> {
                    onStatus("Creating circular plate crop...")
                    val circularSubject = fallbackStickerCropper.createCircularSubject(bitmap)
                    onStatus("Compositing die-cut outline and shadow...")
                    val sticker = try { stickerCompositor.createDieCutSticker(circularSubject) } finally { circularSubject.recycle() }
                    StickerGenerationResult.Success(sticker)
                }
                StickerStyle.ROUNDED_TILE -> {
                    onStatus("Creating Polaroid tile crop...")
                    val roundedSubject = fallbackStickerCropper.createRoundedRectSubject(bitmap)
                    onStatus("Compositing die-cut outline and shadow...")
                    val sticker = try { stickerCompositor.createDieCutSticker(roundedSubject) } finally { roundedSubject.recycle() }
                    StickerGenerationResult.Success(sticker)
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
}
