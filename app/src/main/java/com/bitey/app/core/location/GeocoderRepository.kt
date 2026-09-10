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
