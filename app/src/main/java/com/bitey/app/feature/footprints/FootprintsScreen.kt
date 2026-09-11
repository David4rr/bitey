package com.bitey.app.feature.footprints

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.feature.footprints.component.*
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@Composable
fun FootprintsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FootprintsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    val mapView = rememberMapViewWithLifecycle()

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.fetchDeviceLocation()
    }

    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            viewModel.fetchDeviceLocation()
        }
    }

    val customMarkerIcon = remember(context) { FootprintsMapUtils.createCustomMarkerIcon(context) }
    val userMarkerIcon = remember(context) { FootprintsMapUtils.createUserLocationMarkerIcon(context) }

    LaunchedEffect(uiState.entriesWithLocation, deviceLocation, mapView) {
        mapView.overlays.clear()
        deviceLocation?.let { loc ->
            val userMarker = Marker(mapView).apply {
                position = GeoPoint(loc.latitude, loc.longitude)
                title = "Your Location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = userMarkerIcon
            }
            mapView.overlays.add(userMarker)
        }

        uiState.entriesWithLocation.forEach { item ->
            val lat = item.entry.latitude ?: return@forEach
            val lng = item.entry.longitude ?: return@forEach
            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lng)
                title = item.entry.title
                snippet = item.entry.locationName ?: ""
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = customMarkerIcon
                setOnMarkerClickListener { _, _ ->
                    viewModel.selectEntry(item)
                    mapView.controller.animateTo(GeoPoint(lat, lng))
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        val targetPoint = when {
            uiState.selectedEntry != null -> {
                val e = uiState.selectedEntry!!.entry
                GeoPoint(e.latitude!!, e.longitude!!)
            }
            uiState.entriesWithLocation.isNotEmpty() -> {
                val first = uiState.entriesWithLocation.first()
                GeoPoint(first.entry.latitude!!, first.entry.longitude!!)
            }
            deviceLocation != null -> GeoPoint(deviceLocation!!.latitude, deviceLocation!!.longitude)
            else -> GeoPoint(-6.2088, 106.8456)
        }
        mapView.controller.setZoom(14.5)
        mapView.controller.setCenter(targetPoint)
        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView.onResume(); mapView }, modifier = Modifier.fillMaxSize())

        FootprintsTopBar(
            isSearchActive = isSearchActive,
            onSearchActiveChange = { isSearchActive = it },
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            spotsCount = uiState.entriesWithLocation.size,
            favoritesCount = uiState.favoriteSpotsCount,
            isFavoritesOnly = uiState.isFavoritesOnly,
            onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        MapControlsColumn(
            onZoomIn = { mapView.controller.zoomIn() },
            onZoomOut = { mapView.controller.zoomOut() },
            onRecenter = {
                if (!viewModel.hasLocationPermission()) {
                    locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                } else {
                    viewModel.fetchDeviceLocation()
                    val target = deviceLocation?.let { GeoPoint(it.latitude, it.longitude) }
                        ?: uiState.entriesWithLocation.firstOrNull()?.let { GeoPoint(it.entry.latitude!!, it.entry.longitude!!) } ?: GeoPoint(-6.2088, 106.8456)
                    mapView.controller.animateTo(target)
                    mapView.controller.setZoom(16.0)
                }
            },
            showFitPins = uiState.entriesWithLocation.size > 1,
            onFitPins = {
                val points = uiState.entriesWithLocation.mapNotNull {
                    val lat = it.entry.latitude
                    val lng = it.entry.longitude
                    if (lat != null && lng != null) GeoPoint(lat, lng) else null
                }
                if (points.isNotEmpty()) {
                    mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 100)
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
            AnimatedVisibility(
                visible = uiState.selectedEntry != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                uiState.selectedEntry?.let { selected ->
                    MarkerPreviewCard(
                        item = selected,
                        onClose = { viewModel.clearSelection() },
                        onToggleFavorite = { viewModel.toggleFavorite(selected.entry) },
                        onNavigate = {
                            val lat = selected.entry.latitude
                            val lng = selected.entry.longitude
                            if (lat != null && lng != null) {
                                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(selected.entry.title)})")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    )
                }
            }

            if (uiState.selectedEntry == null && uiState.entriesWithLocation.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.entriesWithLocation, key = { it.entry.id }) { item ->
                        MapSpotCarouselItem(
                            item = item,
                            onClick = {
                                viewModel.selectEntry(item)
                                val lat = item.entry.latitude
                                val lng = item.entry.longitude
                                if (lat != null && lng != null) mapView.controller.animateTo(GeoPoint(lat, lng))
                            }
                        )
                    }
                }
            }
        }

        if (uiState.entriesWithLocation.isEmpty() && !uiState.isLoading) {
            FootprintsEmptyState(modifier = Modifier.align(Alignment.Center))
        }
    }
}
