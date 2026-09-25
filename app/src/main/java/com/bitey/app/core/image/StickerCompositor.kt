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

    suspend fun createDieCutSticker(
        foregroundBitmap: Bitmap,
        strokeWidthPx: Float = 12f,
        shadowBlurPx: Float = 8f,
        shadowOffsetYPx: Float = 6f
    ): CompositedSticker = withContext(Dispatchers.Default) {
        val safeForeground = if (foregroundBitmap.config == Bitmap.Config.HARDWARE) {
            foregroundBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: foregroundBitmap
        } else {
            foregroundBitmap
        }
        val shouldRecycleSafe = safeForeground != foregroundBitmap

        val croppedForeground = ImageTransformUtils.cropTransparentBounds(safeForeground)
        val shouldRecycleCropped = croppedForeground != safeForeground && croppedForeground != foregroundBitmap

        val margin = (strokeWidthPx + shadowBlurPx + shadowOffsetYPx + 8f).toInt()
        val outWidth = croppedForeground.width + (margin * 2)
        val outHeight = croppedForeground.height + (margin * 2)

        val stickerBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(stickerBitmap)

        val alphaMask = createSolidAlphaMask(croppedForeground)

        val outlineBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val outlineCanvas = Canvas(outlineBitmap)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }

        val numAngles = 24
        val maxRadius = strokeWidthPx.toInt().coerceAtLeast(2)
        for (radius in 2..maxRadius step 2) {
            val r = radius.toFloat()
            for (i in 0 until numAngles) {
                val theta = (i * 2.0 * Math.PI / numAngles)
                outlineCanvas.drawBitmap(alphaMask, margin + (cos(theta) * r).toFloat(), margin + (sin(theta) * r).toFloat(), whitePaint)
            }
        }
        outlineCanvas.drawBitmap(alphaMask, margin.toFloat(), margin.toFloat(), whitePaint)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            colorFilter = PorterDuffColorFilter(0x33000000, PorterDuff.Mode.SRC_IN)
            maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(outlineBitmap, 0f, shadowOffsetYPx, shadowPaint)

        val defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        canvas.drawBitmap(outlineBitmap, 0f, 0f, defaultPaint)

        canvas.drawBitmap(croppedForeground, margin.toFloat(), margin.toFloat(), defaultPaint)

        alphaMask.recycle()
        outlineBitmap.recycle()
        if (shouldRecycleCropped) croppedForeground.recycle()
        if (shouldRecycleSafe) safeForeground.recycle()

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
            file = stickerFile, width = outWidth, height = outHeight, sizeBytes = stickerFile.length()
        )
        stickerBitmap.recycle()
        result
    }

    private fun createSolidAlphaMask(cropped: Bitmap): Bitmap {
        val w = cropped.width; val h = cropped.height
        val pixels = IntArray(w * h)
        cropped.getPixels(pixels, 0, w, 0, 0, w, h)
        val solid = BooleanArray(w * h)
        for (i in pixels.indices) solid[i] = ((pixels[i] ushr 24) and 0xFF) > 25

        val maxGap = 32
        for (x in 0 until w) {
            var lastY = -1
            for (y in 0 until h) {
                val idx = y * w + x
                if (solid[idx]) {
                    if (lastY != -1 && (y - lastY) <= maxGap) for (fill in (lastY + 1) until y) solid[fill * w + x] = true
                    lastY = y
                }
            }
        }
        for (y in 0 until h) {
            var lastX = -1; val row = y * w
            for (x in 0 until w) {
                val idx = row + x
                if (solid[idx]) {
                    if (lastX != -1 && (x - lastX) <= maxGap) for (fill in (lastX + 1) until x) solid[row + fill] = true
                    lastX = x
                }
            }
        }

        val pw = w + 2; val ph = h + 2
        val visited = ByteArray(pw * ph)
        for (y in 0 until h) {
            val srcRow = y * w; val dstRow = (y + 1) * pw + 1
            for (x in 0 until w) if (solid[srcRow + x]) visited[dstRow + x] = 1
        }
        val queue = IntArray(pw * ph)
        var head = 0; var tail = 0
        queue[tail++] = 0; visited[0] = 2
        while (head < tail) {
            val idx = queue[head++]; val x = idx % pw; val y = idx / pw
            if (x > 0 && visited[idx - 1].toInt() == 0) { visited[idx - 1] = 2; queue[tail++] = idx - 1 }
            if (x < pw - 1 && visited[idx + 1].toInt() == 0) { visited[idx + 1] = 2; queue[tail++] = idx + 1 }
            if (y > 0 && visited[idx - pw].toInt() == 0) { visited[idx - pw] = 2; queue[tail++] = idx - pw }
            if (y < ph - 1 && visited[idx + pw].toInt() == 0) { visited[idx + pw] = 2; queue[tail++] = idx + pw }
        }

        val solidPixels = IntArray(w * h)
        for (y in 0 until h) {
            val srcRow = (y + 1) * pw + 1; val dstRow = y * w
            for (x in 0 until w) if (visited[srcRow + x].toInt() != 2) solidPixels[dstRow + x] = -0x1000000
        }
        val solidBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        solidBmp.setPixels(solidPixels, 0, w, 0, 0, w, h)
        val alpha = solidBmp.extractAlpha()
        solidBmp.recycle()
        return alpha
    }
}
