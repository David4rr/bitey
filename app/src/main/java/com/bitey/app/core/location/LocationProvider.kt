package com.bitey.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(timeoutMillis: Long = 6000L): LocationCoordinates? {
        if (!hasLocationPermission()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val coords = withTimeoutOrNull(timeoutMillis) {
                    val cancellationTokenSource = CancellationTokenSource()
                    try {
                        val location = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.token
                        ).await() ?: fusedLocationClient.lastLocation.await()

                        location?.let { LocationCoordinates(it.latitude, it.longitude) }
                    } finally {
                        cancellationTokenSource.cancel()
                    }
                }
                if (coords != null) return@withContext coords

                // System LocationManager fallback (reliable on emulators and non-GMS devices)
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                val providers = listOf(
                    android.location.LocationManager.GPS_PROVIDER,
                    android.location.LocationManager.NETWORK_PROVIDER,
                    android.location.LocationManager.PASSIVE_PROVIDER
                )
                for (provider in providers) {
                    try {
                        val loc = lm?.getLastKnownLocation(provider)
                        if (loc != null) return@withContext LocationCoordinates(loc.latitude, loc.longitude)
                    } catch (_: Exception) {}
                }
                null
            } catch (_: Exception) {
                null
            }
        }
    }
}
