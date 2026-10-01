package com.bitey.app.core.location.model

enum class TravelMode(
    val label: String,
    val openStreetMapProfile: String,
    val osrmProfile: String,
    val defaultSpeedKmh: Double
) {
    MOTORCYCLE("Motor", "routed-bike", "bike", 35.0),
    WALKING("Jalan", "routed-foot", "foot", 5.0),
    CAR("Mobil", "routed-car", "driving", 40.0)
}
