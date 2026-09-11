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
    WHOLE_DISH("Whole Dish"),
    PER_DISH("Per Dish")
}

fun cropFileToSquare(sourceFile: File): File {
    return try {
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return sourceFile
        val width = originalBitmap.width
        val height = originalBitmap.height
        val minDim = min(width, height)
        val xOffset = (width - minDim) / 2
        val yOffset = (height - minDim) / 2

        val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, minDim, minDim)
        val squareFile = File(sourceFile.parentFile, "square_${sourceFile.name}")

        FileOutputStream(squareFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
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
