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
        count: Int = 1
    ): BitmapDrawable {
        val cacheKey = "${imagePath.orEmpty()}_${secondaryImagePath.orEmpty()}_${isSticker}_$count"
        markerIconCache.get(cacheKey)?.let { return it }

        val dishBitmap = loadDishBitmap(imagePath, targetSize = 120)
        val secondBitmap = if (count > 1 && !secondaryImagePath.isNullOrBlank()) loadDishBitmap(secondaryImagePath, targetSize = 120) else null
        val markerDrawable = createDishMarkerIcon(context, dishBitmap, secondBitmap, isSticker, count)
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

        // When multiple dishes, render back stacked image circle offset to top-right
        if (count > 1) {
            val backCx = cx + 22f
            val backCy = cy - 14f
            val backR = 34f

            canvas.drawCircle(backCx, backCy + 3f, backR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(40, 0, 0, 0) })
            canvas.drawCircle(backCx, backCy, backR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })

            val bmpToDraw = secondBitmap ?: dishBitmap
            if (bmpToDraw != null && bmpToDraw.width > 0 && bmpToDraw.height > 0) {
                val saveBack = canvas.save()
                canvas.clipPath(Path().apply { addCircle(backCx, backCy, backR - 2f, Path.Direction.CW) })
                val bW = bmpToDraw.width.toFloat()
                val bH = bmpToDraw.height.toFloat()
                val scale = if (isSticker) min((backR * 2f - 4f) / bW, (backR * 2f - 4f) / bH) else max((backR * 2f) / bW, (backR * 2f) / bH)
                val dW = bW * scale
                val dH = bH * scale
                canvas.drawBitmap(bmpToDraw, Rect(0, 0, bmpToDraw.width, bmpToDraw.height),
                    RectF(backCx - dW / 2f, backCy - dH / 2f, backCx + dW / 2f, backCy + dH / 2f),
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                canvas.restoreToCount(saveBack)
            } else {
                canvas.drawCircle(backCx, backCy, backR - 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFE8D6") })
            }
            canvas.drawCircle(backCx, backCy, backR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb(); style = Paint.Style.STROKE; strokeWidth = 2.5f })
        }

        // Shadow & body for front pin
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 0, 0, 0) }
        canvas.drawPath(Path(pinPath).apply { offset(0f, 3.5f) }, shadowPaint)
        canvas.drawPath(pinPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        canvas.drawPath(pinPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BiteyOrange.toArgb(); style = Paint.Style.STROKE; strokeWidth = 3f })

        // Inner circle
        val innerR = 34f
        canvas.drawCircle(cx, cy, innerR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF8F0") })

        if (dishBitmap != null && dishBitmap.width > 0 && dishBitmap.height > 0) {
            val save = canvas.save()
            val clipPath = Path().apply { addCircle(cx, cy, innerR, Path.Direction.CW) }
            canvas.clipPath(clipPath)
            val bW = dishBitmap.width.toFloat()
            val bH = dishBitmap.height.toFloat()
            val scale = if (isSticker) min((innerR * 2f - 4f) / bW, (innerR * 2f - 4f) / bH) else max((innerR * 2f) / bW, (innerR * 2f) / bH)
            val dW = bW * scale
            val dH = bH * scale
            canvas.drawBitmap(dishBitmap, Rect(0, 0, dishBitmap.width, dishBitmap.height),
                RectF(cx - dW / 2f, cy - dH / 2f, cx + dW / 2f, cy + dH / 2f),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
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
