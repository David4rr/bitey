package com.bitey.app.feature.entry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
        when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
            is SegmentationResult.Success -> {
                stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
            }
            else -> {
                val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                stickerCompositor.createDieCutSticker(circular)
            }
        }
    }

    suspend fun generateSticker(
        file: File,
        style: StickerStyle,
        onStatus: (String) -> Unit = {}
    ): StickerGenerationResult = withContext(Dispatchers.IO) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: return@withContext StickerGenerationResult.Failure(
                IllegalStateException("Unable to read optimized image for segmentation.")
            )

        when (style) {
            StickerStyle.AI_SEGMENTED -> {
                onStatus("Extracting food subject with on-device AI...")
                when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                    is SegmentationResult.Success -> {
                        onStatus("Compositing die-cut outline and shadow...")
                        val sticker = stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                        StickerGenerationResult.Success(sticker)
                    }
                    is SegmentationResult.NoSubjectFound -> {
                        StickerGenerationResult.FallbackNeeded(
                            "No distinct food subject detected. Try a circular plate badge or Polaroid tile."
                        )
                    }
                    is SegmentationResult.Failure -> {
                        StickerGenerationResult.FallbackNeeded(
                            "Segmentation unavailable: ${segResult.error.localizedMessage ?: "Unknown error"}. Try circular crop."
                        )
                    }
                }
            }
            StickerStyle.CIRCULAR_BADGE -> {
                onStatus("Creating circular plate crop...")
                val circularSubject = fallbackStickerCropper.createCircularSubject(bitmap)
                onStatus("Compositing die-cut outline and shadow...")
                val sticker = stickerCompositor.createDieCutSticker(circularSubject)
                StickerGenerationResult.Success(sticker)
            }
            StickerStyle.ROUNDED_TILE -> {
                onStatus("Creating Polaroid tile crop...")
                val roundedSubject = fallbackStickerCropper.createRoundedRectSubject(bitmap)
                onStatus("Compositing die-cut outline and shadow...")
                val sticker = stickerCompositor.createDieCutSticker(roundedSubject)
                StickerGenerationResult.Success(sticker)
            }
        }
    }
}
