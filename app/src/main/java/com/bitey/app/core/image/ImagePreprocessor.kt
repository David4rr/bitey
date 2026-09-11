package com.bitey.app.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagePreprocessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exifMetadataExtractor: ExifMetadataExtractor
) {

    companion object {
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
                ImageTransformUtils.decodeDimensions(stream)
            } ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

            val sampleSize = ImageTransformUtils.calculateInSampleSize(
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

            val finalBitmap = ImageTransformUtils.adjustOrientationAndScale(
                bitmap = decodedBitmap,
                rotationDegrees = exif.rotationDegrees,
                isFlipped = exif.isFlipped,
                maxWidth = MAX_WIDTH,
                maxHeight = MAX_HEIGHT
            )

            val targetFile = destinationFile ?: createOutputWebPFile()
            ImageTransformUtils.saveBitmapAsWebP(finalBitmap, targetFile, WEBP_QUALITY)

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

            val sampleSize = ImageTransformUtils.calculateInSampleSize(
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

            val finalBitmap = ImageTransformUtils.adjustOrientationAndScale(
                bitmap = decodedBitmap,
                rotationDegrees = exif.rotationDegrees,
                isFlipped = exif.isFlipped,
                maxWidth = MAX_WIDTH,
                maxHeight = MAX_HEIGHT
            )

            val targetFile = destinationFile ?: createOutputWebPFile()
            ImageTransformUtils.saveBitmapAsWebP(finalBitmap, targetFile, WEBP_QUALITY)

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

    internal fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int = ImageTransformUtils.calculateInSampleSize(rawWidth, rawHeight, reqWidth, reqHeight)

    private fun createOutputWebPFile(): File {
        val storageDir = File(context.filesDir, "bites/media").apply { mkdirs() }
        val filename = "bite_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.webp"
        return File(storageDir, filename)
    }
}
