package com.bitey.app.core.location

import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.core.location.model.RouteStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class RouteRepository @Inject constructor() {

    suspend fun getRoute(
        start: LocationCoordinates,
        destination: LocationCoordinates
    ): NavigationRoute = withContext(Dispatchers.IO) {
        val fetched = fetchOsrmRoute(start, destination)
        fetched ?: createFallbackRoute(start, destination)
    }

    private fun fetchOsrmRoute(
        start: LocationCoordinates,
        destination: LocationCoordinates
    ): NavigationRoute? {
        return try {
            val urlString = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson&steps=true",
                start.longitude, start.latitude, destination.longitude, destination.latitude
            )
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "BiteyApp/1.0 (Android)")
            }
            if (connection.responseCode != 200) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseOsrmJson(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseOsrmJson(jsonString: String): NavigationRoute? {
        val root = JSONObject(jsonString)
        if (root.optString("code") != "Ok") return null
        val routes = root.optJSONArray("routes") ?: return null
        if (routes.length() == 0) return null

        val primaryRoute = routes.getJSONObject(0)
        val totalDistance = primaryRoute.optDouble("distance", 0.0)
        val totalDuration = primaryRoute.optDouble("duration", 0.0)

        // Parse polyline geometry
        val geometry = primaryRoute.optJSONObject("geometry")
        val coordinates = geometry?.optJSONArray("coordinates")
        val points = mutableListOf<LocationCoordinates>()
        if (coordinates != null) {
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lng = coord.getDouble(0)
                val lat = coord.getDouble(1)
                points.add(LocationCoordinates(lat, lng))
            }
        }

        // Parse turn-by-turn steps
        val steps = mutableListOf<RouteStep>()
        val legs = primaryRoute.optJSONArray("legs")
        if (legs != null && legs.length() > 0) {
            val legSteps = legs.getJSONObject(0).optJSONArray("steps")
            if (legSteps != null) {
                for (i in 0 until legSteps.length()) {
                    val stepObj = legSteps.getJSONObject(i)
                    val maneuver = stepObj.optJSONObject("maneuver")
                    val stepType = maneuver?.optString("type") ?: "turn"
                    val modifier = maneuver?.optString("modifier")
                    val name = stepObj.optString("name")
                    val distance = stepObj.optDouble("distance", 0.0)
                    val duration = stepObj.optDouble("duration", 0.0)
                    val mLoc = maneuver?.optJSONArray("location")
                    val stepLng = mLoc?.optDouble(0) ?: 0.0
                    val stepLat = mLoc?.optDouble(1) ?: 0.0

                    steps.add(
                        RouteStep(
                            instruction = formatInstruction(stepType, modifier, name),
                            maneuverType = stepType,
                            modifier = modifier,
                            distanceMeters = distance,
                            durationSeconds = duration,
                            latitude = stepLat,
                            longitude = stepLng
                        )
                    )
                }
            }
        }

        return if (points.isNotEmpty()) {
            NavigationRoute(
                points = points,
                distanceMeters = totalDistance,
                durationSeconds = totalDuration,
                steps = steps
            )
        } else null
    }

    private fun formatInstruction(type: String, modifier: String?, name: String?): String {
        val roadName = if (!name.isNullOrBlank()) " onto $name" else ""
        return when (type) {
            "depart" -> if (!name.isNullOrBlank()) "Head out toward $name" else "Start route"
            "arrive" -> "Arrived at destination"
            "turn" -> when (modifier) {
                "left" -> "Turn left$roadName"
                "right" -> "Turn right$roadName"
                "slight left" -> "Slight left$roadName"
                "slight right" -> "Slight right$roadName"
                "sharp left" -> "Sharp left$roadName"
                "sharp right" -> "Sharp right$roadName"
                "uturn" -> "Make a U-turn$roadName"
                else -> "Turn$roadName"
            }
            "new name", "continue" -> if (!name.isNullOrBlank()) "Continue on $name" else "Continue straight"
            "fork" -> if (modifier?.contains("left") == true) "Keep left$roadName" else "Keep right$roadName"
            "roundabout" -> "At roundabout, take exit$roadName"
            else -> "${type.replaceFirstChar { it.uppercase() }}$roadName"
        }
    }

    private fun createFallbackRoute(
        start: LocationCoordinates,
        dest: LocationCoordinates
    ): NavigationRoute {
        val directDistance = haversineMeters(start, dest) * 1.3
        val duration = directDistance / 8.33 // ~30 km/h average speed in city
        val steps = listOf(
            RouteStep(
                instruction = "Head toward destination",
                maneuverType = "depart",
                distanceMeters = directDistance,
                durationSeconds = duration,
                latitude = start.latitude,
                longitude = start.longitude
            ),
            RouteStep(
                instruction = "Arrived at destination",
                maneuverType = "arrive",
                distanceMeters = 0.0,
                durationSeconds = 0.0,
                latitude = dest.latitude,
                longitude = dest.longitude
            )
        )
        return NavigationRoute(
            points = listOf(start, dest),
            distanceMeters = directDistance,
            durationSeconds = duration,
            steps = steps
        )
    }

    private fun haversineMeters(p1: LocationCoordinates, p2: LocationCoordinates): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLng / 2).pow(2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}
