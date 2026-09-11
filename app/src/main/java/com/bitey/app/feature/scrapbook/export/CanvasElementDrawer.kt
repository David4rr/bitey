package com.bitey.app.feature.scrapbook.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.bitey.app.feature.scrapbook.model.CanvasElement
import java.io.File

object CanvasElementDrawer {

    fun drawFoodSticker(canvas: Canvas, element: CanvasElement) {
        val path = element.imagePath ?: return
        val file = File(path)
        if (!file.exists()) return

        val targetSize = 300
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var inSampleSize = 1
        while (options.outWidth / (inSampleSize * 2) >= targetSize && options.outHeight / (inSampleSize * 2) >= targetSize) {
            inSampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val stickerBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return

        val halfW = stickerBitmap.width / 2f
        val halfH = stickerBitmap.height / 2f

        canvas.drawBitmap(stickerBitmap, -halfW, -halfH, null)
        stickerBitmap.recycle()
    }

    fun drawDateStamp(canvas: Canvas, element: CanvasElement) {
        val text = element.text ?: "TODAY"
        val subtitle = element.subtitle ?: "FOOD LOG"

        val width = 240f
        val height = 100f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        // Stamp border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

        // Inner dashed line
        val innerRect = RectF(rect.left + 8f, rect.top + 8f, rect.right - 8f, rect.bottom - 8f)
        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawRoundRect(innerRect, 8f, 8f, dashPaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 0f, -4f, textPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, 0f, 26f, subPaint)
    }

    fun drawWashiTape(canvas: Canvas, element: CanvasElement) {
        val width = 280f
        val height = 54f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = (element.primaryColorHex.toInt() and 0x00FFFFFF) or 0xCC000000.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(rect, tapePaint)

        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x44FFFFFF
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }
        canvas.drawLine(rect.left, rect.top + 6f, rect.right, rect.top + 6f, edgePaint)
        canvas.drawLine(rect.left, rect.bottom - 6f, rect.right, rect.bottom - 6f, edgePaint)

        element.text?.let { t ->
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(t, 0f, 8f, textPaint)
        }
    }

    fun drawLocationTag(canvas: Canvas, element: CanvasElement) {
        val text = element.text ?: "Jakarta"
        val width = (text.length * 20f + 80f).coerceAtLeast(180f)
        val height = 64f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
            setShadowLayer(8f, 0f, 4f, 0x22000000)
        }
        canvas.drawRoundRect(rect, height / 2f, height / 2f, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(rect, height / 2f, height / 2f, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2B2120.toInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 0f, 8f, textPaint)
    }

    fun drawRatingBadge(canvas: Canvas, element: CanvasElement) {
        val text = element.text ?: "5.0 ★"
        val width = 160f
        val height = 60f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            style = Paint.Style.FILL
            setShadowLayer(6f, 0f, 3f, 0x33000000)
        }
        canvas.drawRoundRect(rect, 16f, 16f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 0f, 10f, textPaint)
    }

    fun drawMoodChip(canvas: Canvas, element: CanvasElement) {
        val text = element.text ?: "PALATE APPROVED"
        val width = (text.length * 18f + 60f).coerceAtLeast(180f)
        val height = 56f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
            setShadowLayer(6f, 0f, 3f, 0x20000000)
        }
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 0f, 8f, textPaint)
    }
}
