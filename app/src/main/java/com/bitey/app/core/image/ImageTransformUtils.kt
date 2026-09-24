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
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }
        val safeBitmap = if (scaledBitmap.config == Bitmap.Config.HARDWARE) {
            scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
        } else {
            scaledBitmap
        }
        val width = safeBitmap.width
        val height = safeBitmap.height
        val row = IntArray(width)

        var minY = -1
        for (y in 0 until height) {
            safeBitmap.getPixels(row, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                if (((row[x] ushr 24) and 0xFF) > 12) {
                    minY = y
                    break
                }
            }
            if (minY != -1) break
        }

        if (minY == -1) {
            if (safeBitmap != scaledBitmap && safeBitmap != bitmap) safeBitmap.recycle()
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
            return bitmap
        }

        var maxY = minY
        for (y in height - 1 downTo minY) {
            safeBitmap.getPixels(row, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                if (((row[x] ushr 24) and 0xFF) > 12) {
                    maxY = y
                    break
                }
            }
            if (maxY != minY) break
        }

        var minX = width
        var maxX = -1
        for (y in minY..maxY) {
            safeBitmap.getPixels(row, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                if (((row[x] ushr 24) and 0xFF) > 12) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            if (safeBitmap != scaledBitmap && safeBitmap != bitmap) safeBitmap.recycle()
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
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
        if (safeBitmap != scaledBitmap && safeBitmap != cropped && safeBitmap != bitmap) {
            safeBitmap.recycle()
        }
        if (scaledBitmap != bitmap && scaledBitmap != cropped) {
            scaledBitmap.recycle()
        }
        return cropped
    }
}

/**
 * Coil Transformation that automatically removes empty transparent borders from stickers.
 */
class CropTransparentTransformation(
    val paddingPx: Int = 4
) : coil.transform.Transformation {
    override val cacheKey: String = "${CropTransparentTransformation::class.java.name}-$paddingPx"

    override suspend fun transform(input: Bitmap, size: coil.size.Size): Bitmap {
        return ImageTransformUtils.cropTransparentBounds(input, paddingPx)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is CropTransparentTransformation && paddingPx == other.paddingPx
    }

    override fun hashCode(): Int {
        return javaClass.hashCode() * 31 + paddingPx
    }
}
