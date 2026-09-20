package com.bitey.app.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GeocoderRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double,
        locale: Locale = Locale.getDefault()
    ): GeocodedAddress? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext null
        }

        try {
            val geocoder = Geocoder(context, locale)
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                queryAddressesApi33(geocoder, latitude, longitude)
            } else {
                queryAddressesLegacy(geocoder, latitude, longitude)
            }

            addresses.firstOrNull()?.let { formatAddress(it) }
        } catch (e: IOException) {
            // Offline or geocoder service unavailable
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun forwardGeocode(
        locationName: String,
        locale: Locale = Locale.getDefault()
    ): LocationCoordinates? = withContext(Dispatchers.IO) {
        val trimmed = locationName.trim()
        if (trimmed.isBlank()) return@withContext null

        // 1. Direct coordinate pattern e.g. "-6.2088, 106.8456"
        val match = Regex("""(-?\d+\.\d+)[,\s]+(-?\d+\.\d+)""").find(trimmed)
        if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return@withContext LocationCoordinates(lat, lng)
        }

        // 2. Known culinary area dictionary (offline & emulator safe)
        val lower = trimmed.lowercase(locale)
        for ((key, coords) in KNOWN_AREAS) {
            if (lower.contains(key)) return@withContext coords
        }

        // 3. Android system Geocoder
        if (Geocoder.isPresent()) {
            try {
                val geocoder = Geocoder(context, locale)
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    queryForwardAddressesApi33(geocoder, trimmed)
                } else {
                    queryForwardAddressesLegacy(geocoder, trimmed)
                }
                val first = addresses.firstOrNull()
                if (first != null) {
                    return@withContext LocationCoordinates(first.latitude, first.longitude)
                }
            } catch (_: Exception) {}
        }

        // 4. Deterministic local city fallback around Jakarta hub
        val hash = kotlin.math.abs(trimmed.hashCode())
        val latOffset = ((hash % 1000) / 1000.0 - 0.5) * 0.04
        val lngOffset = (((hash / 1000) % 1000) / 1000.0 - 0.5) * 0.04
        LocationCoordinates(-6.2088 + latOffset, 106.8456 + lngOffset)
    }

    companion object {
        private val KNOWN_AREAS = mapOf(
            "menteng" to LocationCoordinates(-6.1950, 106.8330), "kuningan" to LocationCoordinates(-6.2297, 106.8295),
            "senopati" to LocationCoordinates(-6.2367, 106.8075), "cikini" to LocationCoordinates(-6.1906, 106.8385),
            "tebet" to LocationCoordinates(-6.2300, 106.8520), "kemang" to LocationCoordinates(-6.2750, 106.8150),
            "sudirman" to LocationCoordinates(-6.2150, 106.8200), "thamrin" to LocationCoordinates(-6.1920, 106.8230),
            "jakarta" to LocationCoordinates(-6.2088, 106.8456), "bandung" to LocationCoordinates(-6.9175, 107.6191),
            "surabaya" to LocationCoordinates(-7.2575, 112.7521), "bali" to LocationCoordinates(-8.4095, 115.1889),
            "jogja" to LocationCoordinates(-7.7956, 110.3695), "yogyakarta" to LocationCoordinates(-7.7956, 110.3695)
        )
    }

    private suspend fun queryForwardAddressesApi33(
        geocoder: Geocoder,
        locationName: String
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocationName(locationName, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (continuation.isActive) continuation.resume(addresses)
            }
            override fun onError(errorMessage: String?) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun queryForwardAddressesLegacy(
        geocoder: Geocoder,
        locationName: String
    ): List<Address> {
        return geocoder.getFromLocationName(locationName, 1) ?: emptyList()
    }

    private suspend fun queryAddressesApi33(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (continuation.isActive) {
                    continuation.resume(addresses)
                }
            }

            override fun onError(errorMessage: String?) {
                if (continuation.isActive) {
                    continuation.resume(emptyList())
                }
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun queryAddressesLegacy(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): List<Address> {
        return geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
    }

    fun formatAddress(address: Address): GeocodedAddress {
        val placeName = address.featureName?.takeIf { feature ->
            // Exclude feature names that are just raw house numbers if street is already present
            val thoroughfare = address.thoroughfare
            thoroughfare == null || !feature.all { it.isDigit() }
        }

        val street = listOfNotNull(
            address.thoroughfare,
            address.subThoroughfare
        ).joinToString(" ").takeIf { it.isNotBlank() }

        val subLocality = address.subLocality
        val city = address.locality ?: address.subAdminArea ?: address.adminArea
        val country = address.countryName

        // Construct a clean, concise display name for journal entries
        val components = mutableListOf<String>()
        placeName?.let { if (it.isNotBlank() && it != street) components.add(it) }
        street?.let { if (it.isNotBlank() && !components.contains(it)) components.add(it) }
        subLocality?.let { if (it.isNotBlank() && !components.contains(it)) components.add(it) }
        city?.let { if (it.isNotBlank() && !components.contains(it)) components.add(it) }

        val displayName = if (components.isNotEmpty()) {
            components.take(3).joinToString(", ")
        } else {
            address.getAddressLine(0) ?: "Unknown Location"
        }

        return GeocodedAddress(
            displayName = displayName,
            placeName = placeName,
            street = street,
            subLocality = subLocality,
            city = city,
            country = country
        )
    }
}
