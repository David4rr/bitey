package com.bitey.app.feature.footprints.component

import com.bitey.app.core.location.LocationCoordinates

object LocationFallbackResolver {
    fun resolveQuickCoordinates(
        locationName: String?,
        title: String,
        id: Long,
        deviceLocation: LocationCoordinates?
    ): LocationCoordinates {
        val loc = locationName?.lowercase() ?: ""
        return when {
            loc.contains("menteng") -> LocationCoordinates(-6.1950, 106.8330)
            loc.contains("kuningan") -> LocationCoordinates(-6.2297, 106.8295)
            loc.contains("senopati") -> LocationCoordinates(-6.2367, 106.8075)
            loc.contains("cikini") -> LocationCoordinates(-6.1906, 106.8385)
            loc.contains("tebet") -> LocationCoordinates(-6.2300, 106.8520)
            loc.contains("kemang") -> LocationCoordinates(-6.2750, 106.8150)
            loc.contains("sudirman") -> LocationCoordinates(-6.2150, 106.8200)
            loc.contains("bandung") -> LocationCoordinates(-6.9175, 107.6191)
            loc.contains("bali") -> LocationCoordinates(-8.4095, 115.1889)
            loc.contains("surabaya") -> LocationCoordinates(-7.2575, 112.7521)
            loc.contains("jogja") || loc.contains("yogyakarta") -> LocationCoordinates(-7.7956, 110.3695)
            else -> deviceLocation ?: run {
                val h = kotlin.math.abs((title + loc + id).hashCode())
                LocationCoordinates(
                    -6.2088 + ((h % 1000) / 1000.0 - 0.5) * 0.03,
                    106.8456 + (((h / 1000) % 1000) / 1000.0 - 0.5) * 0.03
                )
            }
        }
    }
}
