package com.bitey.app.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ProcessedImage(
    val file: File,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val exifMetadata: ExifMetadata
)

@Singleton
class ImagePreprocessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exifMetadataExtractor: ExifMetadataExtractor
) {

    companion object {
        private const val TAG = "ImagePreprocessor"
        const val MAX_WIDTH = 1920
        const val MAX_HEIGHT = 1080
        const val WEBP_QUALITY = 88
    }

    suspend fun processUri(
        uri: Uri,
        destinationFile: File? = null
    ): Result<ProcessedImage> = withContext(Dispatchers.IO) {
        runCatching {
            val exif = exifMetadataExtractor.extract(uri)

            // Pass 1: Measure dimensions without loading pixels into memory
            val (originalWidth, originalHeight) = context.contentResolver.openInputStream(uri)?.use { stream ->
                decodeDimensions(stream)
            } ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

            // Calculate sample size for memory-safe decoding
            val sampleSize = calculateInSampleSize(
                originalWidth,
                originalHeight,
                MAX_WIDTH,
                MAX_HEIGHT
            )

            // Pass 2: Decode subsampled bitmap
            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw IllegalStateException("Failed to decode bitmap from URI: $uri")

            // Correct orientation and scale to exact target bounds
            val finalBitmap = adjustOrientationAndScale(
                bitmap = decodedBitmap,
                rotationDegrees = exif.rotationDegrees,
                isFlipped = exif.isFlipped,
                maxWidth = MAX_WIDTH,
                maxHeight = MAX_HEIGHT
            )

            val targetFile = destinationFile ?: createOutputWebPFile()
            saveBitmapAsWebP(finalBitmap, targetFile, WEBP_QUALITY)

            val width = finalBitmap.width
            val height = finalBitmap.height

            if (finalBitmap != decodedBitmap) {
                decodedBitmap.recycle()
            }
            finalBitmap.recycle()

            ProcessedImage(
                file = targetFile,
                width = width,
                height = height,
                sizeBytes = targetFile.length(),
                exifMetadata = exif
            )
        }
    }

    suspend fun processFile(
        sourceFile: File,
        destinationFile: File? = null
    ): Result<ProcessedImage> = withContext(Dispatchers.IO) {
        runCatching {
            val exif = exifMetadataExtractor.extract(sourceFile)

            // Pass 1: Measure dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                throw IllegalStateException("Invalid image file dimensions: $originalWidth x $originalHeight")
            }

            val sampleSize = calculateInSampleSize(
                originalWidth,
                originalHeight,
                MAX_WIDTH,
                MAX_HEIGHT
            )

            // Pass 2: Decode subsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decodedBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
                ?: throw IllegalStateException("Failed to decode bitmap from file: ${sourceFile.absolutePath}")

            val finalBitmap = adjustOrientationAndScale(
                bitmap = decodedBitmap,
                rotationDegrees = exif.rotationDegrees,
                isFlipped = exif.isFlipped,
                maxWidth = MAX_WIDTH,
                maxHeight = MAX_HEIGHT
            )

            val targetFile = destinationFile ?: createOutputWebPFile()
            saveBitmapAsWebP(finalBitmap, targetFile, WEBP_QUALITY)

            val width = finalBitmap.width
            val height = finalBitmap.height

            if (finalBitmap != decodedBitmap) {
                decodedBitmap.recycle()
            }
            finalBitmap.recycle()

            ProcessedImage(
                file = targetFile,
                width = width,
                height = height,
                sizeBytes = targetFile.length(),
                exifMetadata = exif
            )
        }
    }

    private fun decodeDimensions(inputStream: InputStream): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        return Pair(options.outWidth, options.outHeight)
    }

    internal fun calculateInSampleSize(
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

    private fun adjustOrientationAndScale(
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

    private fun saveBitmapAsWebP(bitmap: Bitmap, outputFile: File, quality: Int) {
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

    private fun createOutputWebPFile(): File {
        val storageDir = File(context.filesDir, "bites/media").apply { mkdirs() }
        val filename = "bite_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.webp"
        return File(storageDir, filename)
    }
}
