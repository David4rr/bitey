package com.bitey.app.core.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class FallbackStickerCropper @Inject constructor() {

    /**
     * Crops the center square of [bitmap] into a circle with transparent background,
     * suitable for creating a round die-cut plate sticker.
     */
    suspend fun createCircularSubject(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val size = min(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        // Draw antialiased circle mask
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        // Draw source bitmap constrained inside circle
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(xOffset, yOffset, xOffset + size, yOffset + size)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

        output
    }

    /**
     * Crops the center square of [bitmap] into a rounded tile (e.g. Polaroid badge).
     */
    suspend fun createRoundedRectSubject(
        bitmap: Bitmap,
        cornerRadiusRatio: Float = 0.12f
    ): Bitmap = withContext(Dispatchers.Default) {
        val size = min(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val cornerRadius = size * cornerRadiusRatio
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(xOffset, yOffset, xOffset + size, yOffset + size)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

        output
    }
}
