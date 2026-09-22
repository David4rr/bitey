package com.bitey.app.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageTransformUtils {
    private const val TAG = "ImageTransformUtils"

    fun decodeDimensions(inputStream: InputStream): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        return Pair(options.outWidth, options.outHeight)
    }

    fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    fun adjustOrientationAndScale(
        bitmap: Bitmap,
        rotationDegrees: Int,
        isFlipped: Boolean,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        val matrix = Matrix()

        if (isFlipped) {
            matrix.postScale(-1f, 1f)
        }
        if (rotationDegrees != 0) {
            matrix.postRotate(rotationDegrees.toFloat())
        }

        // Apply rotation/flip first if necessary
        val orientedBitmap = if (rotationDegrees != 0 || isFlipped) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        val currentWidth = orientedBitmap.width
        val currentHeight = orientedBitmap.height

        // Determine if scaling down is required
        val scale = min(
            1.0f,
            min(maxWidth.toFloat() / currentWidth, maxHeight.toFloat() / currentHeight)
        )

        val finalBitmap = if (scale < 1.0f) {
            val targetWidth = (currentWidth * scale).roundToInt()
            val targetHeight = (currentHeight * scale).roundToInt()
            Bitmap.createScaledBitmap(orientedBitmap, targetWidth, targetHeight, true)
        } else {
            orientedBitmap
        }

        if (orientedBitmap != bitmap && orientedBitmap != finalBitmap) {
            orientedBitmap.recycle()
        }

        return finalBitmap
    }

    fun saveBitmapAsWebP(bitmap: Bitmap, outputFile: File, quality: Int) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { out ->
            val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            val success = bitmap.compress(compressFormat, quality, out)
            if (!success) {
                Log.w(TAG, "Bitmap compression to WebP failed for file: ${outputFile.name}")
            }
            out.flush()
        }
    }

    /**
     * Finds the tight non-transparent bounding box of an image and crops it,
     * removing all empty transparent margins so the subject fills its layout bounds.
     */
    fun cropTransparentBounds(bitmap: Bitmap, paddingPx: Int = 4): Bitmap {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        } else {
            bitmap
        }
        val width = safeBitmap.width
        val height = safeBitmap.height
        val pixels = IntArray(width * height)
        safeBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val alpha = (pixels[rowOffset + x] ushr 24) and 0xFF
                if (alpha > 12) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            if (safeBitmap != bitmap) safeBitmap.recycle()
            return bitmap
        }

        val cropX = (minX - paddingPx).coerceAtLeast(0)
        val cropY = (minY - paddingPx).coerceAtLeast(0)
        val cropW = (maxX - cropX + 1 + paddingPx).coerceIn(1, width - cropX)
        val cropH = (maxY - cropY + 1 + paddingPx).coerceIn(1, height - cropY)

        val cropped = if (cropX == 0 && cropY == 0 && cropW == width && cropH == height) {
            safeBitmap
        } else {
            Bitmap.createBitmap(safeBitmap, cropX, cropY, cropW, cropH)
        }
        if (safeBitmap != bitmap && safeBitmap != cropped) {
            safeBitmap.recycle()
        }
        return cropped
    }
}

/**
 * Coil Transformation that automatically removes empty transparent borders from stickers.
 */
class CropTransparentTransformation : coil.transform.Transformation {
    override val cacheKey: String = "CropTransparentTransformation"

    override suspend fun transform(input: Bitmap, size: coil.size.Size): Bitmap {
        return ImageTransformUtils.cropTransparentBounds(input)
    }
}
