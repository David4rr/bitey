package com.bitey.app.feature.footprints.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
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

    fun clearCache() {
        markerIconCache.evictAll()
    }

    fun getOrCreateDishMarkerIcon(
        context: Context,
        imagePath: String?,
        isSticker: Boolean
    ): BitmapDrawable {
        val cacheKey = "${imagePath.orEmpty()}_$isSticker"
        markerIconCache.get(cacheKey)?.let { return it }

        val dishBitmap = loadDishBitmap(imagePath, targetSize = 120)
        val markerDrawable = createDishMarkerIcon(context, dishBitmap, isSticker)
        markerIconCache.put(cacheKey, markerDrawable)
        return markerDrawable
    }

    fun loadDishBitmap(imagePath: String?, targetSize: Int = 120): Bitmap? {
        if (imagePath.isNullOrBlank()) return null
        val file = File(imagePath)
        if (!file.exists() || !file.canRead()) return null

        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            val sampleSize = ImageTransformUtils.calculateInSampleSize(
                boundsOptions.outWidth,
                boundsOptions.outHeight,
                targetSize,
                targetSize
            )
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    fun createDishMarkerIcon(
        context: Context,
        dishBitmap: Bitmap?,
        isSticker: Boolean = false
    ): BitmapDrawable {
        val width = 120
        val height = 140
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = 60f
        val cy = 54f
        val r = 46f
        val tipX = 60f
        val tipY = 140f

        val pinPath = Path().apply {
            arcTo(
                RectF(cx - r, cy - r, cx + r, cy + r),
                148f,
                244f,
                false
            )
            lineTo(tipX, tipY)
            close()
        }

        // 1. Drop shadow offset
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(45, 0, 0, 0)
        }
        val shadowPath = Path(pinPath).apply {
            offset(0f, 3.5f)
        }
        canvas.drawPath(shadowPath, shadowPaint)

        // 2. White die-cut outer body
        val whiteBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawPath(pinPath, whiteBodyPaint)

        // 3. Orange accent outline
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BiteyOrange.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawPath(pinPath, strokePaint)

        // 4. Inner image cutout circle
        val innerRadius = 38f
        val innerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#FFF8F0")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, innerRadius, innerBgPaint)

        if (dishBitmap != null && dishBitmap.width > 0 && dishBitmap.height > 0) {
            val saveCount = canvas.save()
            val clipPath = Path().apply {
                addCircle(cx, cy, innerRadius, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)

            val bW = dishBitmap.width.toFloat()
            val bH = dishBitmap.height.toFloat()

            if (isSticker) {
                val availableSize = innerRadius * 2f - 6f
                val scale = min(availableSize / bW, availableSize / bH)
                val drawW = bW * scale
                val drawH = bH * scale
                val drawLeft = cx - (drawW / 2f)
                val drawTop = cy - (drawH / 2f)

                val destRect = RectF(drawLeft, drawTop, drawLeft + drawW, drawTop + drawH)
                val srcRect = Rect(0, 0, dishBitmap.width, dishBitmap.height)
                val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(dishBitmap, srcRect, destRect, imagePaint)
            } else {
                val targetDiameter = innerRadius * 2f
                val scale = max(targetDiameter / bW, targetDiameter / bH)
                val scaledW = bW * scale
                val scaledH = bH * scale
                val drawLeft = cx - (scaledW / 2f)
                val drawTop = cy - (scaledH / 2f)

                val destRect = RectF(drawLeft, drawTop, drawLeft + scaledW, drawTop + scaledH)
                val srcRect = Rect(0, 0, dishBitmap.width, dishBitmap.height)
                val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(dishBitmap, srcRect, destRect, imagePaint)
            }
            canvas.restoreToCount(saveCount)

            val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(30, 0, 0, 0)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawCircle(cx, cy, innerRadius, innerBorderPaint)
        } else {
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = BiteyOrange.toArgb()
            }
            canvas.drawCircle(cx, cy, 14f, dotPaint)

            val miniDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
            }
            canvas.drawCircle(cx, cy, 6f, miniDotPaint)
        }

        // 5. Small dot near bottom pointer tip
        val tipDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BiteyOrange.toArgb()
        }
        canvas.drawCircle(tipX, tipY - 8f, 3.5f, tipDotPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
