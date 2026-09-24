package com.bitey.app.feature.footprints

import android.Manifest
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
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.feature.footprints.component.*
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FootprintsScreen(
    targetEntryId: Long? = null,
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
        if (!viewModel.hasLocationPermission()) locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        else viewModel.fetchDeviceLocation()
    }

    LaunchedEffect(targetEntryId) {
        if (targetEntryId != null && targetEntryId != -1L) viewModel.startNavigationForEntryId(targetEntryId)
    }

    LaunchedEffect(uiState.activeRoute, mapView) { NavigationRouteOverlay.renderRoute(mapView, uiState.activeRoute) }

    val userMarkerIcon = remember(context) { FootprintsMapUtils.createUserLocationMarkerIcon(context) }

    LaunchedEffect(uiState.spots, deviceLocation, mapView) {
        mapView.overlays.clear()
        NavigationRouteOverlay.renderRoute(mapView, uiState.activeRoute, autoZoom = false)

        deviceLocation?.let { loc ->
            val userMarker = Marker(mapView).apply {
                position = GeoPoint(loc.latitude, loc.longitude)
                title = "Your Location"
                infoWindow = null
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = userMarkerIcon
            }
            mapView.overlays.add(userMarker)
        }

        val markers = withContext(Dispatchers.IO) {
            uiState.spots.map { spot ->
                val p = spot.primaryEntry
                val s = spot.entries.getOrNull(1)
                fun getImg(e: PlateEntryWithTags?) = e?.let { if (it.entry.isStickerMode && !it.entry.stickerImagePath.isNullOrBlank()) it.entry.stickerImagePath else it.entry.thumbnailPath.ifBlank { it.entry.fullImagePath } }
                val icon = DishMarkerRenderer.getOrCreateDishMarkerIcon(context, getImg(p), getImg(s), p.entry.isStickerMode, spot.entries.size)
                Triple(spot, GeoPoint(spot.latitude, spot.longitude), icon)
            }
        }

        markers.forEach { (spot, geoPoint, dishIcon) ->
            val marker = Marker(mapView).apply {
                position = geoPoint
                title = spot.primaryEntry.entry.title
                snippet = spot.locationName ?: ""
                infoWindow = null
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = dishIcon
                setOnMarkerClickListener { _, _ ->
                    viewModel.selectSpot(spot)
                    mapView.controller.animateTo(geoPoint)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        if (!hasInitialCentered && (uiState.spots.isNotEmpty() || deviceLocation != null)) {
            val targetPoint = when {
                uiState.selectedSpot != null -> GeoPoint(uiState.selectedSpot!!.latitude, uiState.selectedSpot!!.longitude)
                uiState.spots.isNotEmpty() -> GeoPoint(uiState.spots.first().latitude, uiState.spots.first().longitude)
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
        AndroidView(factory = { mapView.onResume(); mapView }, modifier = Modifier.fillMaxSize())

        if (uiState.isNavigating) {
            NavigationTopBanner(currentStep = uiState.currentManeuverStep, modifier = Modifier.align(Alignment.TopCenter))
        } else {
            FootprintsTopBar(
                isSearchActive = isSearchActive, onSearchActiveChange = { isSearchActive = it },
                searchQuery = uiState.searchQuery, onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                spotsCount = uiState.spots.size, favoritesCount = uiState.favoriteSpotsCount,
                isFavoritesOnly = uiState.isFavoritesOnly, onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                onNavigateBack = onNavigateBack, modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        MapControlsColumn(
            onRecenter = {
                if (!viewModel.hasLocationPermission()) locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                else {
                    viewModel.fetchDeviceLocation()
                    val target = deviceLocation?.let { GeoPoint(it.latitude, it.longitude) }
                        ?: uiState.spots.firstOrNull()?.let { GeoPoint(it.latitude, it.longitude) } ?: GeoPoint(-6.2088, 106.8456)
                    mapView.controller.animateTo(target)
                    mapView.controller.setZoom(16.0)
                }
            },
            showFitPins = uiState.spots.size > 1 && !uiState.isNavigating,
            onFitPins = {
                val pts = uiState.spots.map { GeoPoint(it.latitude, it.longitude) }
                if (pts.isNotEmpty()) mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts), true, 100)
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
            if (uiState.isNavigating && uiState.navigationTarget != null && uiState.activeRoute != null) {
                NavigationBottomPanel(
                    targetEntry = uiState.navigationTarget!!, route = uiState.activeRoute!!,
                    onStopNavigation = { viewModel.stopNavigation() },
                    onRecenterRoute = { NavigationRouteOverlay.renderRoute(mapView, uiState.activeRoute, autoZoom = true) }
                )
            } else {
                AnimatedVisibility(
                    visible = uiState.selectedSpot != null,
                    enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    uiState.selectedSpot?.let { spot ->
                        if (spot.entries.size > 1) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(spot.entries, key = { it.entry.id }) { dish ->
                                    MarkerPreviewCard(
                                        item = dish,
                                        onClose = { viewModel.clearSelection() },
                                        onToggleFavorite = { viewModel.toggleFavorite(dish.entry) },
                                        onNavigate = { viewModel.startNavigation(dish) },
                                        modifier = Modifier.width(320.dp)
                                    )
                                }
                            }
                        } else {
                            MarkerPreviewCard(
                                item = spot.primaryEntry,
                                onClose = { viewModel.clearSelection() },
                                onToggleFavorite = { viewModel.toggleFavorite(spot.primaryEntry.entry) },
                                onNavigate = { viewModel.startNavigation(spot.primaryEntry) },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (uiState.entriesWithLocation.isEmpty() && !uiState.isLoading && showEmptyHint) {
                    FootprintsEmptyState(onPinDishes = { viewModel.autoResolveMissingLocations() }, onDismiss = { showEmptyHint = false })
                }
            }
        }
    }
}
