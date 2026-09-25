package com.bitey.app.feature.scrapbook.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.bitey.app.feature.scrapbook.model.CanvasElement
import java.io.File

object CanvasElementDrawer {

    fun drawFoodSticker(
        canvas: Canvas,
        element: CanvasElement,
        dpPx: Float,
        backgroundColorHex: Long = 0xFFF4F1EAL,
        customTypeface: Typeface? = null
    ) {
        val path = element.imagePath ?: return
        val file = File(path)
        if (!file.exists()) return

        val boxSize = 140f * dpPx
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return

        var sampleSize = 1
        while (options.outWidth / (sampleSize * 2) >= boxSize && options.outHeight / (sampleSize * 2) >= boxSize) sampleSize *= 2
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return

        val fitScale = minOf(boxSize / bitmap.width.toFloat(), boxSize / bitmap.height.toFloat())
        val drawW = bitmap.width.toFloat() * fitScale
        val drawH = bitmap.height.toFloat() * fitScale

        val hasTitle = !element.text.isNullOrBlank()
        val titleH = if (hasTitle) 18f * dpPx else 0f
        val spacerH = if (hasTitle) 2f * dpPx else 0f
        val totalH = boxSize + spacerH + titleH
        val boxCenterY = -totalH / 2f + boxSize / 2f

        val destRect = RectF(-drawW / 2f, boxCenterY - drawH / 2f, drawW / 2f, boxCenterY + drawH / 2f)
        canvas.drawBitmap(bitmap, null, destRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        bitmap.recycle()

        if (hasTitle) {
            val isDark = ((backgroundColorHex shr 16 and 0xFF) * 0.299f + (backgroundColorHex shr 8 and 0xFF) * 0.587f + (backgroundColorHex and 0xFF) * 0.114f) / 255f < 0.5f
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDark) Color.WHITE else 0xFF261C1A.toInt()
                textSize = 13f * dpPx
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(element.text!!, 0f, boxCenterY + boxSize / 2f + spacerH + 13f * dpPx, textPaint)
        }
    }

    fun drawDateStamp(canvas: Canvas, element: CanvasElement, dpPx: Float) {
        val text = element.text ?: "TODAY"
        val subtitle = element.subtitle
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt(); textSize = 14f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.primaryColorHex.toInt(); textSize = 11f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textAlign = Paint.Align.CENTER
        }
        val padX = 14f * dpPx; val padY = 8f * dpPx; val titleH = 14f * dpPx; val subH = if (subtitle != null) 11f * dpPx else 0f
        val totalW = maxOf(titlePaint.measureText(text), subtitle?.let { subPaint.measureText(it) } ?: 0f) + padX * 2f
        val totalH = titleH + (if (subtitle != null) subH + 4f * dpPx else 0f) + padY * 2f
        val rect = RectF(-totalW / 2f, -totalH / 2f, totalW / 2f, totalH / 2f)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.primaryColorHex.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f * dpPx }
        canvas.drawRoundRect(rect, 10f * dpPx, 10f * dpPx, borderPaint)

        if (subtitle != null) {
            val titleY = -totalH / 2f + padY + titleH * 0.85f
            canvas.drawText(text, 0f, titleY, titlePaint)
            canvas.drawText(subtitle, 0f, titleY + subH + 4f * dpPx, subPaint)
        } else {
            canvas.drawText(text, 0f, titleH * 0.35f, titlePaint)
        }
    }

    fun drawWashiTape(canvas: Canvas, element: CanvasElement, dpPx: Float) {
        val text = element.text ?: ""
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 12f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val padX = 20f * dpPx; val padY = 6f * dpPx; val textH = 12f * dpPx
        val totalW = textPaint.measureText(text) + padX * 2f; val totalH = textH + padY * 2f
        val rect = RectF(-totalW / 2f, -totalH / 2f, totalW / 2f, totalH / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = (element.primaryColorHex.toInt() and 0x00FFFFFF) or 0xD9000000.toInt(); style = Paint.Style.FILL }
        canvas.drawRect(rect, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF; style = Paint.Style.STROKE; strokeWidth = 1f * dpPx }
        canvas.drawRect(rect, borderPaint)
        canvas.drawText(text, 0f, textH * 0.35f, textPaint)
    }

    fun drawLocationTag(canvas: Canvas, element: CanvasElement, dpPx: Float) {
        val text = element.text ?: "Location"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1E293B.toInt(); textSize = 12f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val padX = 14f * dpPx; val padY = 6f * dpPx; val textH = 12f * dpPx
        val totalW = textPaint.measureText(text) + padX * 2f; val totalH = textH + padY * 2f
        val rect = RectF(-totalW / 2f, -totalH / 2f, totalW / 2f, totalH / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFAFAFA.toInt(); style = Paint.Style.FILL }
        canvas.drawRoundRect(rect, 16f * dpPx, 16f * dpPx, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.primaryColorHex.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f * dpPx }
        canvas.drawRoundRect(rect, 16f * dpPx, 16f * dpPx, borderPaint)
        canvas.drawText(text, 0f, textH * 0.35f, textPaint)
    }

    fun drawRatingBadge(canvas: Canvas, element: CanvasElement, dpPx: Float) {
        val text = element.text ?: "5.0 ★"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFAFAFA.toInt(); textSize = 12f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val padX = 12f * dpPx; val padY = 6f * dpPx; val textH = 12f * dpPx
        val totalW = textPaint.measureText(text) + padX * 2f; val totalH = textH + padY * 2f
        val rect = RectF(-totalW / 2f, -totalH / 2f, totalW / 2f, totalH / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.primaryColorHex.toInt(); style = Paint.Style.FILL }
        canvas.drawRoundRect(rect, 12f * dpPx, 12f * dpPx, bgPaint)
        canvas.drawText(text, 0f, textH * 0.35f, textPaint)
    }

    fun drawMoodChip(canvas: Canvas, element: CanvasElement, dpPx: Float) {
        val text = element.text ?: "CHEF'S KISS"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.primaryColorHex.toInt(); textSize = 11f * dpPx; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val padX = 12f * dpPx; val padY = 6f * dpPx; val textH = 11f * dpPx
        val totalW = textPaint.measureText(text) + padX * 2f; val totalH = textH + padY * 2f
        val rect = RectF(-totalW / 2f, -totalH / 2f, totalW / 2f, totalH / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFAFAFA.toInt(); style = Paint.Style.FILL }
        canvas.drawRoundRect(rect, 12f * dpPx, 12f * dpPx, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.primaryColorHex.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.5f * dpPx }
        canvas.drawRoundRect(rect, 12f * dpPx, 12f * dpPx, borderPaint)
        canvas.drawText(text, 0f, textH * 0.35f, textPaint)
    }
}
