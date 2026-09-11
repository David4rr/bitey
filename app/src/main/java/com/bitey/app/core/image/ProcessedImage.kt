package com.bitey.app.core.image

import java.io.File

data class ProcessedImage(
    val file: File,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val exifMetadata: ExifMetadata
)
