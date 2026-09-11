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
        try {
            when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                is SegmentationResult.Success -> {
                    try {
                        stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
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
        } finally {
            bitmap.recycle()
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

        try {
            when (style) {
                StickerStyle.AI_SEGMENTED -> {
                    onStatus("Extracting food subject with on-device AI...")
                    when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                        is SegmentationResult.Success -> {
                            onStatus("Compositing die-cut outline and shadow...")
                            val sticker = try {
                                stickerCompositor.createDieCutSticker(segResult.foregroundBitmap)
                            } finally {
                                segResult.foregroundBitmap.recycle()
                            }
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
                    val sticker = try {
                        stickerCompositor.createDieCutSticker(circularSubject)
                    } finally {
                        circularSubject.recycle()
                    }
                    StickerGenerationResult.Success(sticker)
                }
                StickerStyle.ROUNDED_TILE -> {
                    onStatus("Creating Polaroid tile crop...")
                    val roundedSubject = fallbackStickerCropper.createRoundedRectSubject(bitmap)
                    onStatus("Compositing die-cut outline and shadow...")
                    val sticker = try {
                        stickerCompositor.createDieCutSticker(roundedSubject)
                    } finally {
                        roundedSubject.recycle()
                    }
                    StickerGenerationResult.Success(sticker)
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
}
