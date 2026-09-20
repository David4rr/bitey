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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FootprintsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FootprintsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val theme = com.bitey.app.core.ui.theme.LocalNeumorphicTheme.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    var showEmptyHint by remember { mutableStateOf(true) }
    var hasInitialCentered by remember { mutableStateOf(false) }
    val mapView = rememberMapViewWithLifecycle(isDark = theme.isDark)
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) viewModel.fetchDeviceLocation()
    }
    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else viewModel.fetchDeviceLocation()
    }

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

        val markers = withContext(Dispatchers.IO) {
            uiState.entriesWithLocation.mapNotNull { item ->
                val lat = item.entry.latitude ?: return@mapNotNull null
                val lng = item.entry.longitude ?: return@mapNotNull null
                val imagePath = if (item.entry.isStickerMode && !item.entry.stickerImagePath.isNullOrBlank()) {
                    item.entry.stickerImagePath
                } else item.entry.thumbnailPath.ifBlank { item.entry.fullImagePath }
                val icon = DishMarkerRenderer.getOrCreateDishMarkerIcon(context, imagePath, item.entry.isStickerMode)
                Triple(item, GeoPoint(lat, lng), icon)
            }
        }

        markers.forEach { (item, geoPoint, dishIcon) ->
            val marker = Marker(mapView).apply {
                position = geoPoint
                title = item.entry.title
                snippet = item.entry.locationName ?: ""
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = dishIcon
                setOnMarkerClickListener { _, _ ->
                    viewModel.selectEntry(item)
                    mapView.controller.animateTo(geoPoint)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        if (!hasInitialCentered && (uiState.entriesWithLocation.isNotEmpty() || deviceLocation != null)) {
            val targetPoint = when {
                uiState.selectedEntry != null -> GeoPoint(uiState.selectedEntry!!.entry.latitude!!, uiState.selectedEntry!!.entry.longitude!!)
                uiState.entriesWithLocation.isNotEmpty() -> GeoPoint(uiState.entriesWithLocation.first().entry.latitude!!, uiState.entriesWithLocation.first().entry.longitude!!)
                deviceLocation != null -> GeoPoint(deviceLocation!!.latitude, deviceLocation!!.longitude)
                else -> GeoPoint(-6.2088, 106.8456)
            }
            mapView.controller.setZoom(14.5)
            mapView.controller.setCenter(targetPoint)
            mapView.post { mapView.controller.setCenter(targetPoint); mapView.invalidate() }
            hasInitialCentered = true
        } else mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView.onResume(); mapView },
            update = { it.invalidate() },
            modifier = Modifier.fillMaxSize()
        )

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
                val pts = uiState.entriesWithLocation.mapNotNull { it.entry.latitude?.let { lat -> it.entry.longitude?.let { lng -> GeoPoint(lat, lng) } } }
                if (pts.isNotEmpty()) mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts), true, 100)
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
                            val (lat, lng) = selected.entry.latitude to selected.entry.longitude
                            if (lat != null && lng != null) {
                                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(selected.entry.title)})")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    )
                }
            }

            if (uiState.entriesWithLocation.isEmpty() && !uiState.isLoading && showEmptyHint) {
                FootprintsEmptyState(
                    onPinDishes = { viewModel.autoResolveMissingLocations() },
                    onDismiss = { showEmptyHint = false }
                )
            }
        }
    }
}
