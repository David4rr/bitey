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
        val bitmap = decodeSampledBitmap(file) ?: return@withContext null
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

    suspend fun createStickersForMultiSubject(file: File): List<CompositedSticker> = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(file) ?: return@withContext emptyList()
        val stickers = mutableListOf<CompositedSticker>()
        try {
            when (val segResult = foodSubjectSegmenter.segment(bitmap)) {
                is SegmentationResult.Success -> {
                    if (segResult.subjectBitmaps.size > 1) {
                        for (subjBmp in segResult.subjectBitmaps) {
                            runCatching {
                                stickers.add(stickerCompositor.createDieCutSticker(subjBmp))
                            }
                        }
                    }
                    runCatching {
                        stickers.add(stickerCompositor.createDieCutSticker(segResult.foregroundBitmap))
                    }
                }
                else -> {
                    val circular = fallbackStickerCropper.createCircularSubject(bitmap)
                    try {
                        stickers.add(stickerCompositor.createDieCutSticker(circular))
                    } finally {
                        circular.recycle()
                    }
                }
            }
        } finally {
            bitmap.recycle()
        }
        stickers
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
        return BitmapFactory.decodeFile(file.absolutePath, opts)
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
