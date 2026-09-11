package com.bitey.app.feature.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
    val label: String = ""
)

fun cropFileToSquare(sourceFile: File, maxDim: Int = 1280): File {
    return try {
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
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOpts) ?: return sourceFile
        val width = originalBitmap.width
        val height = originalBitmap.height
        val minSide = min(width, height)
        val xOffset = (width - minSide) / 2
        val yOffset = (height - minSide) / 2

        val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, minSide, minSide)
        val squareFile = File(sourceFile.parentFile, "square_${sourceFile.name}")

        FileOutputStream(squareFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }

        if (croppedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
        croppedBitmap.recycle()
        sourceFile.delete()
        squareFile
    } catch (e: Exception) {
        Log.e("CameraModels", "Crop to square failed", e)
        sourceFile
    }
}
