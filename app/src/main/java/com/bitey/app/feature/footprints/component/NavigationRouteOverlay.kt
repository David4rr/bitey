package com.bitey.app.feature.footprints.component

import android.graphics.Color
import android.graphics.Paint
import com.bitey.app.core.location.model.NavigationRoute
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

object NavigationRouteOverlay {
    private const val ROUTE_CASING_ID = "nav_route_casing"
    private const val ROUTE_CORE_ID = "nav_route_core"

    fun renderRoute(
        mapView: MapView,
        route: NavigationRoute?,
        autoZoom: Boolean = true
    ) {
        // Remove existing route overlays
        removeRoute(mapView)

        if (route == null || route.points.size < 2) {
            mapView.invalidate()
            return
        }

        val geoPoints = route.points.map { GeoPoint(it.latitude, it.longitude) }

        // 1. Casing polyline (darker outline for contrast on any map background)
        val casingPolyline = Polyline(mapView).apply {
            id = ROUTE_CASING_ID
            setPoints(geoPoints)
            outlinePaint.color = Color.argb(180, 40, 40, 40)
            outlinePaint.strokeWidth = 15f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
        }

        // 2. Core polyline (vibrant BiteyOrange)
        val corePolyline = Polyline(mapView).apply {
            id = ROUTE_CORE_ID
            setPoints(geoPoints)
            outlinePaint.color = Color.parseColor("#FF6F43")
            outlinePaint.strokeWidth = 10f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
        }

        // Add at bottom of overlays so markers appear on top of route
        val idx0 = minOf(0, mapView.overlays.size)
        mapView.overlays.add(idx0, casingPolyline)
        val idx1 = minOf(1, mapView.overlays.size)
        mapView.overlays.add(idx1, corePolyline)

        if (autoZoom && geoPoints.isNotEmpty()) {
            try {
                val bbox = BoundingBox.fromGeoPoints(geoPoints)
                mapView.zoomToBoundingBox(bbox, true, 140)
            } catch (_: Exception) {}
        }

        mapView.invalidate()
    }

    fun removeRoute(mapView: MapView) {
        val toRemove = mapView.overlays.filter { overlay ->
            overlay is Polyline && (overlay.id == ROUTE_CASING_ID || overlay.id == ROUTE_CORE_ID)
        }
        if (toRemove.isNotEmpty()) {
            mapView.overlays.removeAll(toRemove)
        }
        mapView.invalidate()
    }
}
