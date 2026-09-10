package com.bitey.app.feature.scrapbook.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasBitmapRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun renderToBitmap(
        elements: List<CanvasElement>,
        aspectRatio: CanvasAspectRatio,
        backgroundColorHex: Long,
        viewportWidthPx: Float,
        viewportHeightPx: Float
    ): Bitmap = withContext(Dispatchers.IO) {
        val targetWidth = 1080
        val targetHeight = when (aspectRatio) {
            CanvasAspectRatio.STORY_9_16 -> 1920
            CanvasAspectRatio.SQUARE_1_1 -> 1080
        }

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw canvas background
        val bgPaint = Paint().apply {
            color = backgroundColorHex.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

        // Draw subtle tactile dot grid
        val dotPaint = Paint().apply {
            color = 0x18000000
            style = Paint.Style.FILL
        }
        val gridStep = 48f
        var gx = gridStep
        while (gx < targetWidth) {
            var gy = gridStep
            while (gy < targetHeight) {
                canvas.drawCircle(gx, gy, 1.8f, dotPaint)
                gy += gridStep
            }
            gx += gridStep
        }

        // Scale factor from preview viewport coordinates to offscreen high-res canvas
        val scaleX = targetWidth / viewportWidthPx
        val scaleY = targetHeight / viewportHeightPx
        val uniformScale = (scaleX + scaleY) / 2f

        val centerX = targetWidth / 2f
        val centerY = targetHeight / 2f

        // Draw elements in z-order
        val sortedElements = elements.sortedBy { it.zIndex }
        for (element in sortedElements) {
            canvas.save()

            val elX = centerX + (element.xOffset * uniformScale)
            val elY = centerY + (element.yOffset * uniformScale)

            canvas.translate(elX, elY)
            canvas.rotate(element.rotation)
            val finalElementScale = element.scale * uniformScale
            canvas.scale(finalElementScale, finalElementScale)

            when (element.type) {
                CanvasElementType.FOOD_STICKER -> {
                    drawFoodSticker(canvas, element)
                }
                CanvasElementType.DATE_STAMP -> {
                    drawDateStamp(canvas, element)
                }
                CanvasElementType.WASHI_TAPE -> {
                    drawWashiTape(canvas, element)
                }
                CanvasElementType.LOCATION_TAG -> {
                    drawLocationTag(canvas, element)
                }
                CanvasElementType.RATING_BADGE -> {
                    drawRatingBadge(canvas, element)
                }
                CanvasElementType.MOOD_CHIP -> {
                    drawMoodChip(canvas, element)
                }
            }

            canvas.restore()
        }

        // Bottom subtle brand stamp
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x554A3E3D
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("bitey • food scrapbook", targetWidth / 2f, targetHeight - 40f, watermarkPaint)

        bitmap
    }

    private fun drawFoodSticker(canvas: Canvas, element: CanvasElement) {
        val path = element.imagePath ?: return
        val file = File(path)
        if (!file.exists()) return

        // Decode with downsampling to fit standard sticker bounds (e.g. 260x260 dp)
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

    private fun drawDateStamp(canvas: Canvas, element: CanvasElement) {
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

    private fun drawWashiTape(canvas: Canvas, element: CanvasElement) {
        val width = 280f
        val height = 54f
        val rect = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)

        // Semi-transparent tape body
        val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = (element.primaryColorHex.toInt() and 0x00FFFFFF) or 0xCC000000.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(rect, tapePaint)

        // Tape edges dashed
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x44FFFFFF
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }
        canvas.drawLine(rect.left, rect.top + 6f, rect.right, rect.top + 6f, edgePaint)
        canvas.drawLine(rect.left, rect.bottom - 6f, rect.right, rect.bottom - 6f, edgePaint)

        // Text
        element.text?.let { t ->
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(t, 0f, 8f, textPaint)
        }
    }

    private fun drawLocationTag(canvas: Canvas, element: CanvasElement) {
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

    private fun drawRatingBadge(canvas: Canvas, element: CanvasElement) {
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
            color = android.graphics.Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 0f, 10f, textPaint)
    }

    private fun drawMoodChip(canvas: Canvas, element: CanvasElement) {
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

    suspend fun saveToTemporaryCache(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(sharedDir, "bitey_story_$timeStamp.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }

    suspend fun exportToGallery(bitmap: Bitmap, title: String): Uri? = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = "Bitey_${title.replace(" ", "_")}_$timeStamp.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Bitey")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@withContext null

        try {
            context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null)
            null
        }
    }
}
