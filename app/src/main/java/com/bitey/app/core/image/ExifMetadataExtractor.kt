package com.bitey.app.core.image

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExifMetadataExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ExifMetadataExtractor"
        private val EXIF_DATE_FORMAT = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
    }

    fun extract(uri: Uri): ExifMetadata {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                extractFromStream(inputStream)
            } ?: ExifMetadata()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract EXIF from URI: $uri", e)
            ExifMetadata()
        }
    }

    fun extract(file: File): ExifMetadata {
        return try {
            if (!file.exists()) return ExifMetadata()
            val exif = ExifInterface(file)
            parseExif(exif)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract EXIF from file: ${file.absolutePath}", e)
            ExifMetadata()
        }
    }

    fun extractFromStream(inputStream: InputStream): ExifMetadata {
        return try {
            val exif = ExifInterface(inputStream)
            parseExif(exif)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse EXIF from stream", e)
            ExifMetadata()
        }
    }

    private fun parseExif(exif: ExifInterface): ExifMetadata {
        val latLong = exif.latLong
        val latitude = latLong?.getOrNull(0)
        val longitude = latLong?.getOrNull(1)

        val timestamp = parseTimestamp(exif)

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
        val isFlipped = orientation in listOf(
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE
        )

        val cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim()
        val cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim()

        return ExifMetadata(
            capturedAtMillis = timestamp,
            latitude = latitude,
            longitude = longitude,
            rotationDegrees = rotationDegrees,
            isFlipped = isFlipped,
            cameraMake = cameraMake,
            cameraModel = cameraModel
        )
    }

    private fun parseTimestamp(exif: ExifInterface): Long? {
        val dateTimeString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

        if (dateTimeString != null) {
            try {
                val parsed = EXIF_DATE_FORMAT.parse(dateTimeString)
                if (parsed != null) {
                    return parsed.time
                }
            } catch (ignored: Exception) {
                // Ignore parse errors, fallback to null
            }
        }
        return null
    }
}
