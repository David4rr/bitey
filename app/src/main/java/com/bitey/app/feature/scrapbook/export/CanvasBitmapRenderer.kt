package com.bitey.app.feature.scrapbook.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
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
                CanvasElementType.FOOD_STICKER -> CanvasElementDrawer.drawFoodSticker(canvas, element)
                CanvasElementType.DATE_STAMP -> CanvasElementDrawer.drawDateStamp(canvas, element)
                CanvasElementType.WASHI_TAPE -> CanvasElementDrawer.drawWashiTape(canvas, element)
                CanvasElementType.LOCATION_TAG -> CanvasElementDrawer.drawLocationTag(canvas, element)
                CanvasElementType.RATING_BADGE -> CanvasElementDrawer.drawRatingBadge(canvas, element)
                CanvasElementType.MOOD_CHIP -> CanvasElementDrawer.drawMoodChip(canvas, element)
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
