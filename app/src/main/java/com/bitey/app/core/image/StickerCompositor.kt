package com.bitey.app.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin

data class CompositedSticker(
    val file: File,
    val width: Int,
    val height: Int,
    val sizeBytes: Long
)

@Singleton
class StickerCompositor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val stickersDir: File by lazy {
        File(context.filesDir, "bites/stickers").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Composites a realistic die-cut sticker:
     * 1. Dilated solid white outline (8-12px) behind foreground subject.
     * 2. Soft 4-8px drop shadow behind the white outline.
     * 3. Foreground subject drawn cleanly on top.
     * 4. Exports to transparent WebP in internal storage.
     */
    suspend fun createDieCutSticker(
        foregroundBitmap: Bitmap,
        strokeWidthPx: Float = 12f,
        shadowBlurPx: Float = 8f,
        shadowOffsetYPx: Float = 6f
    ): CompositedSticker = withContext(Dispatchers.Default) {
        val margin = (strokeWidthPx + shadowBlurPx + shadowOffsetYPx + 8f).toInt()
        val outWidth = foregroundBitmap.width + (margin * 2)
        val outHeight = foregroundBitmap.height + (margin * 2)

        // 1. Target transparent sticker canvas
        val stickerBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(stickerBitmap)

        // 2. Extract alpha channel from foreground for morphological dilation
        val alphaMask = foregroundBitmap.extractAlpha()

        // 3. Create intermediate dilated white outline bitmap
        val outlineBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val outlineCanvas = Canvas(outlineBitmap)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }

        // Circular multi-pass dilation: stamp alpha mask in radial pattern
        val numAngles = 24
        val maxRadius = strokeWidthPx.toInt().coerceAtLeast(2)
        for (radius in 2..maxRadius step 2) {
            val r = radius.toFloat()
            for (i in 0 until numAngles) {
                val theta = (i * 2.0 * Math.PI / numAngles)
                val ox = (cos(theta) * r).toFloat()
                val oy = (sin(theta) * r).toFloat()
                outlineCanvas.drawBitmap(alphaMask, margin + ox, margin + oy, whitePaint)
            }
        }
        // Center stamp
        outlineCanvas.drawBitmap(alphaMask, margin.toFloat(), margin.toFloat(), whitePaint)

        // 4. Draw soft drop shadow behind the white outline
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            colorFilter = PorterDuffColorFilter(0x33000000, PorterDuff.Mode.SRC_IN)
            maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(outlineBitmap, 0f, shadowOffsetYPx, shadowPaint)

        // 5. Draw solid white die-cut outline over shadow
        val defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }
        canvas.drawBitmap(outlineBitmap, 0f, 0f, defaultPaint)

        // 6. Draw foreground food subject over white outline
        canvas.drawBitmap(foregroundBitmap, margin.toFloat(), margin.toFloat(), defaultPaint)

        // Clean up temporary bitmaps
        alphaMask.recycle()
        outlineBitmap.recycle()

        // 7. Save to internal WebP file
        val stickerFile = File(stickersDir, "sticker_${UUID.randomUUID()}.webp")
        FileOutputStream(stickerFile).use { out ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                stickerBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
            } else {
                @Suppress("DEPRECATION")
                stickerBitmap.compress(Bitmap.CompressFormat.WEBP, 95, out)
            }
        }

        val result = CompositedSticker(
            file = stickerFile,
            width = outWidth,
            height = outHeight,
            sizeBytes = stickerFile.length()
        )

        stickerBitmap.recycle()
        result
    }
}
