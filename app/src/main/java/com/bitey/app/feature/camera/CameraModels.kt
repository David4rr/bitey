package com.bitey.app.feature.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.bitey.app.core.database.model.MealType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

enum class CameraRatio(val label: String, val ratioFloat: Float) {
    SQUARE_1_1("1:1", 1.0f),
    STANDARD_4_3("4:3", 3.0f / 4.0f)
}

enum class PhotoMode(val label: String) {
    ONESHOT("Oneshot"),
    DISH_BY_DISH("Dish by dish");

    companion object {
        val WHOLE_DISH get() = ONESHOT
        val PER_DISH get() = DISH_BY_DISH
    }
}

enum class CaptureFlowStep {
    CAMERA,
    PROCESSING,
    REVIEW
}

data class CandidateStickerItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalFilePath: String,
    val stickerFilePath: String,
    val isSelected: Boolean = true,
    val label: String = "",
    val mealType: MealType = MealType.FOOD
)

fun normalizeAndCropCapturedImage(sourceFile: File, ratio: CameraRatio, maxDim: Int = 1920): File {
    return try {
        val exif = ExifInterface(sourceFile.absolutePath)
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

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return sourceFile

        var sampleSize = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while ((largest / sampleSize) > maxDim * 1.5) { sampleSize *= 2 }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOpts) ?: return sourceFile

        // Rotate to upright orientation first matching camera viewfinder
        val matrix = Matrix()
        if (isFlipped) matrix.postScale(-1f, 1f)
        if (rotationDegrees != 0) matrix.postRotate(rotationDegrees.toFloat())

        val orientedBitmap = if (rotationDegrees != 0 || isFlipped) {
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true).also {
                if (it != rawBitmap) rawBitmap.recycle()
            }
        } else {
            rawBitmap
        }

        // Crop square if requested
        val finalBitmap = if (ratio == CameraRatio.SQUARE_1_1) {
            val width = orientedBitmap.width
            val height = orientedBitmap.height
            val minSide = min(width, height)
            val xOffset = (width - minSide) / 2
            val yOffset = (height - minSide) / 2
            Bitmap.createBitmap(orientedBitmap, xOffset, yOffset, minSide, minSide).also {
                if (it != orientedBitmap) orientedBitmap.recycle()
            }
        } else {
            orientedBitmap
        }

        val outputFile = File(sourceFile.parentFile, "oriented_${sourceFile.name}")
        FileOutputStream(outputFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }

        if (finalBitmap != orientedBitmap) {
            orientedBitmap.recycle()
        }
        finalBitmap.recycle()
        try { sourceFile.delete() } catch (_: Exception) {}
        outputFile
    } catch (e: Exception) {
        Log.e("CameraModels", "Normalize and crop image failed", e)
        sourceFile
    }
}

fun cropFileToSquare(sourceFile: File, maxDim: Int = 1280): File {
    return normalizeAndCropCapturedImage(sourceFile, CameraRatio.SQUARE_1_1, maxDim)
}
