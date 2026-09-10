package com.bitey.app.core.image

data class ExifMetadata(
    val capturedAtMillis: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rotationDegrees: Int = 0,
    val isFlipped: Boolean = false,
    val cameraMake: String? = null,
    val cameraModel: String? = null
)
