package com.bitey.app.core.location.model

import com.bitey.app.core.location.LocationCoordinates

data class RouteStep(
    val instruction: String,
    val maneuverType: String,
    val modifier: String? = null,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class NavigationRoute(
    val points: List<LocationCoordinates>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val steps: List<RouteStep>
) {
    val formattedDistance: String
        get() = if (distanceMeters >= 1000) {
            String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }

    val formattedDuration: String
        get() {
            val minutes = (durationSeconds / 60).toInt()
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remMinutes = minutes % 60
                "${hours}h ${remMinutes}m"
            } else {
                "${maxOf(1, minutes)} min"
            }
        }
}
