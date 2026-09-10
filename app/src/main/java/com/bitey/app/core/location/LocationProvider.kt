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
                withTimeoutOrNull(timeoutMillis) {
                    val cancellationTokenSource = CancellationTokenSource()
                    try {
                        val location = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.token
                        ).await()

                        if (location != null) {
                            LocationCoordinates(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        } else {
                            // Fallback to last known location
                            val lastLocation = fusedLocationClient.lastLocation.await()
                            lastLocation?.let {
                                LocationCoordinates(
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            }
                        }
                    } finally {
                        cancellationTokenSource.cancel()
                    }
                }
            } catch (e: SecurityException) {
                null
            } catch (e: Exception) {
                null
            }
        }
    }
}
