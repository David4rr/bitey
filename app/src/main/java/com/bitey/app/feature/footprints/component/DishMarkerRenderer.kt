package com.bitey.app.feature.footprints.component

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.compose.ui.graphics.toArgb
import com.bitey.app.core.image.ImageTransformUtils
import com.bitey.app.core.ui.theme.BiteyOrange
import java.io.File
import kotlin.math.max
import kotlin.math.min

object DishMarkerRenderer {

    private val markerIconCache = LruCache<String, BitmapDrawable>(120)

    fun clearCache() { markerIconCache.evictAll() }

    fun getOrCreateDishMarkerIcon(
        context: Context,
        imagePath: String?,
        secondaryImagePath: String? = null,
        isSticker: Boolean = false,
        isSecondarySticker: Boolean = isSticker,
        count: Int = 1
    ): BitmapDrawable {
        val cacheKey = "${imagePath.orEmpty()}_${secondaryImagePath.orEmpty()}_${isSticker}_${isSecondarySticker}_$count"
        markerIconCache.get(cacheKey)?.let { return it }

        val dishBitmap = loadDishBitmap(imagePath, targetSize = 120)
        val secondBitmap = if (count > 1 && !secondaryImagePath.isNullOrBlank()) loadDishBitmap(secondaryImagePath, targetSize = 120) else null
        val markerDrawable = createDishMarkerIcon(context, dishBitmap, secondBitmap, isSticker, isSecondarySticker, count)
        dishBitmap?.recycle()
        secondBitmap?.recycle()
        markerIconCache.put(cacheKey, markerDrawable)
        return markerDrawable
    }

    fun loadDishBitmap(imagePath: String?, targetSize: Int = 120): Bitmap? {
        val file = imagePath?.let { File(it) } ?: return null
        if (!file.exists() || !file.canRead()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val sample = ImageTransformUtils.calculateInSampleSize(bounds.outWidth, bounds.outHeight, targetSize, targetSize)
            BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            })
        } catch (_: Exception) { null }
    }

    fun createDishMarkerIcon(
        context: Context,
        dishBitmap: Bitmap?,
        secondBitmap: Bitmap? = null,
        isSticker: Boolean = false,
        isSecondarySticker: Boolean = isSticker,
        count: Int = 1
    ): BitmapDrawable {
        val width = 144
        val height = 152
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = 64f
        val cy = 66f
        val r = 42f
        val tipX = 64f
        val tipY = 150f

        val pinPath = Path().apply {
            arcTo(RectF(cx - r, cy - r, cx + r, cy + r), 148f, 244f, false)
            lineTo(tipX, tipY)
            close()
        }
        // Shadow & body for single pin marker
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 0, 0, 0) }
        canvas.drawPath(Path(pinPath).apply { offset(0f, 3.5f) }, shadowPaint)
        canvas.drawPath(pinPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        canvas.drawPath(pinPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb(); style = Paint.Style.STROKE; strokeWidth = 3f })

        // Inner circular window
        val innerR = 36f
        canvas.drawCircle(cx, cy, innerR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF8F0") })

        if (dishBitmap != null && dishBitmap.width > 0 && dishBitmap.height > 0) {
            val save = canvas.save()
            val clipPath = Path().apply { addCircle(cx, cy, innerR, Path.Direction.CW) }
            canvas.clipPath(clipPath)

            fun drawSticker(bmp: Bitmap, offsetX: Float, offsetY: Float, rotationDeg: Float, isStk: Boolean, maxDim: Float, alpha: Int = 255) {
                val sSave = canvas.save()
                canvas.translate(cx + offsetX, cy + offsetY)
                if (rotationDeg != 0f) canvas.rotate(rotationDeg)
                val bW = bmp.width.toFloat()
                val bH = bmp.height.toFloat()
                val scale = if (isStk) min(maxDim / bW, maxDim / bH) else max(maxDim / bW, maxDim / bH)
                val dW = bW * scale
                val dH = bH * scale
                val dstRect = RectF(-dW / 2f, -dH / 2f, dW / 2f, dH / 2f)
                if (offsetX != 0f || offsetY != 0f || rotationDeg != 0f) {
                    val dieCutRect = RectF(dstRect).apply { inset(-2f, -2f) }
                    val shadowRect = RectF(dieCutRect).apply { offset(0f, 2f) }
                    canvas.drawRoundRect(shadowRect, 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 0, 0, 0) })
                    canvas.drawRoundRect(dieCutRect, 6f, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
                }
                val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { this.alpha = alpha }
                canvas.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height), dstRect, bmpPaint)
                canvas.restoreToCount(sSave)
            }

            // When multiple dishes, render stacked stickers with previous dish visible at overlap
            if (count > 1) {
                val backBmp = secondBitmap ?: dishBitmap
                if (count > 2) {
                    drawSticker(backBmp, 7f, -8f, 18f, isSecondarySticker, innerR * 1.40f, alpha = 240)
                }
                // Previous dish placed at top-left, clearly visible where overlapping
                drawSticker(backBmp, -8f, -7f, -16f, isSecondarySticker, innerR * 1.48f, alpha = 245)
                // Front dish slightly translucent where overlapping so previous dish remains visible underneath
                drawSticker(dishBitmap, 4f, 4f, 6f, isSticker, innerR * 1.50f, alpha = 230)
            } else {
                drawSticker(dishBitmap, 0f, 0f, 0f, isSticker, innerR * 2f - 4f, alpha = 255)
            }

            canvas.restoreToCount(save)
            canvas.drawCircle(cx, cy, innerR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 0, 0, 0); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        } else {
            canvas.drawCircle(cx, cy, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb() })
            canvas.drawCircle(cx, cy, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        }
        canvas.drawCircle(tipX, tipY - 8f, 3.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb() })

        // Count badge if stacked
        if (count > 1) {
            val badgeX = cx + r + 2f
            val badgeY = cy - r + 4f
            val badgeR = 13f
            canvas.drawCircle(badgeX, badgeY, badgeR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb() })
            canvas.drawCircle(badgeX, badgeY, badgeR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 2.5f })
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = if (count > 9) 15f else 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(if (count > 99) "99+" else count.toString(), badgeX, badgeY + 5.5f, textPaint)
        }

        return BitmapDrawable(context.resources, bitmap)
    }
}
