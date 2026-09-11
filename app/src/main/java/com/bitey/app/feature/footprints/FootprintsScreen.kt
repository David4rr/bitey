package com.bitey.app.feature.footprints

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FootprintsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FootprintsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    val mapView = rememberMapViewWithLifecycle()

    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchDeviceLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.fetchDeviceLocation()
        }
    }

    // Add Markers onto MapView when entries or device location change
    LaunchedEffect(uiState.entriesWithLocation, deviceLocation, mapView) {
        mapView.overlays.clear()

        // Current Device Location Marker
        deviceLocation?.let { loc ->
            val userMarker = Marker(mapView).apply {
                position = GeoPoint(loc.latitude, loc.longitude)
                title = "Your Location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = createUserLocationMarkerIcon(context)
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
                icon = createCustomMarkerIcon(context)
                setOnMarkerClickListener { _, _ ->
                    viewModel.selectEntry(item)
                    mapView.controller.animateTo(GeoPoint(lat, lng))
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        // Auto center map to selected or first entry or device location
        val targetPoint = when {
            uiState.selectedEntry != null -> {
                val e = uiState.selectedEntry!!.entry
                GeoPoint(e.latitude!!, e.longitude!!)
            }
            uiState.entriesWithLocation.isNotEmpty() -> {
                val first = uiState.entriesWithLocation.first()
                GeoPoint(first.entry.latitude!!, first.entry.longitude!!)
            }
            deviceLocation != null -> {
                GeoPoint(deviceLocation!!.latitude, deviceLocation!!.longitude)
            }
            else -> GeoPoint(-6.2088, 106.8456) // Default Jakarta center
        }

        mapView.controller.setZoom(14.5)
        mapView.controller.setCenter(targetPoint)
        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen OpenStreetMap
        AndroidView(
            factory = {
                mapView.onResume()
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Professional Top Floating Header & Filter Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimalistCard(cornerRadius = 22.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Title and Stats Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onNavigateBack != null) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = theme.inkPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Culinary Map",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.inkPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BiteyOrange.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.entriesWithLocation.size} spots",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BiteyOrange
                                )
                            }
                        }
                        Text(
                            text = "${uiState.favoriteSpotsCount} favorite pin markers",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkSecondary
                        )
                    }

                    }
                    // Quick Favorite Pin Filter Toggle Button
                    AnimatedFavoriteButton(
                        isFavorite = uiState.isFavoritesOnly,
                        onToggle = { viewModel.toggleFavoritesOnly() },
                        containerSize = 38.dp,
                        iconSize = 18.dp,
                        withNeumorphicContainer = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Search Input Field
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search visited dish, spot, or tag...", color = theme.inkMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = theme.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = theme.inkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = theme.background,
                        unfocusedContainerColor = theme.background,
                        focusedTextColor = theme.inkPrimary,
                        unfocusedTextColor = theme.inkPrimary
                    ),
                    singleLine = true
                )
            }
        }

        // Professional Right-Side Map Navigation Column
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom In (+)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .minimalistCard(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { mapView.controller.zoomIn() }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Zoom In",
                        tint = theme.inkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom Out (-)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .minimalistCard(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { mapView.controller.zoomOut() }) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Zoom Out",
                        tint = theme.inkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Recenter GPS Location
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .minimalistCard(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (!viewModel.hasLocationPermission()) {
                            locationPermissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            viewModel.fetchDeviceLocation()
                            val target = deviceLocation?.let {
                                GeoPoint(it.latitude, it.longitude)
                            } ?: uiState.entriesWithLocation.firstOrNull()?.let {
                                GeoPoint(it.entry.latitude!!, it.entry.longitude!!)
                            } ?: GeoPoint(-6.2088, 106.8456)
                            mapView.controller.animateTo(target)
                            mapView.controller.setZoom(16.0)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "My Location",
                        tint = BiteyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Fit All Visited Food Pins
            if (uiState.entriesWithLocation.size > 1) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .minimalistCard(cornerRadius = 21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val points = uiState.entriesWithLocation.mapNotNull {
                                val lat = it.entry.latitude
                                val lng = it.entry.longitude
                                if (lat != null && lng != null) GeoPoint(lat, lng) else null
                            }
                            if (points.isNotEmpty()) {
                                val bbox = BoundingBox.fromGeoPoints(points)
                                mapView.zoomToBoundingBox(bbox, true, 100)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CenterFocusStrong,
                            contentDescription = "Fit All Pins",
                            tint = BiteyMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom Content: Carousel of Visited Spots OR Selected Entry Preview Card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            // Selected Pin Preview Card
            AnimatedVisibility(
                visible = uiState.selectedEntry != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
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
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            // Horizontal Carousel of Visited Spots (when no single marker is focused)
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
                                if (lat != null && lng != null) {
                                    mapView.controller.animateTo(GeoPoint(lat, lng))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Empty state hint if no geotagged entries
        if (uiState.entriesWithLocation.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimalistCard(cornerRadius = 24.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BiteyMint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                tint = BiteyMint,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Culinary Footprints",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bites captured with GPS will automatically appear here as collectible food sticker pins.",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact sticker card in the bottom carousel on the map.
 */
@Composable
private fun MapSpotCarouselItem(
    item: PlateEntryWithTags,
    onClick: () -> Unit
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .minimalistCard(cornerRadius = 18.dp)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .dieCutStickerEffect(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            entry.locationName?.let { loc ->
                Text(
                    text = loc,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Clean minimalist preview card for selected map pin.
 */
@Composable
private fun MarkerPreviewCard(
    item: PlateEntryWithTags,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 12.dp, elevation = 0.dp)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sticker Thumbnail Frame
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.isStickerMode && entry.stickerImagePath != null) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(entry.timestamp))
                    Text(
                        text = "$dateStr • ${entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (entry.rating >= i) BiteyOrange else theme.inkMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", entry.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.inkPrimary
                        )

                        entry.price?.let { price ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${entry.currency} ${price.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BiteyMint
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = theme.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedFavoriteButton(
                        isFavorite = entry.isFavorite,
                        onToggle = onToggleFavorite,
                        containerSize = 34.dp,
                        iconSize = 18.dp,
                        withNeumorphicContainer = true
                    )
                }
            }

            // Location Name Row
            entry.locationName?.let { loc ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = loc,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Directions Navigation Action Button
            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Directions,
                    contentDescription = null,
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Navigate with Google Maps",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = StickerDieCutWhite
                )
            }
        }
    }
}

/**
 * Creates custom pin marker icon.
 */
private fun createCustomMarkerIcon(context: Context): BitmapDrawable {
    val sizePx = 100
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(50, 0, 0, 0)
    }
    canvas.drawCircle(sizePx / 2f, sizePx * 0.45f + 4f, sizePx * 0.38f, shadowPaint)

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BiteyOrange.toArgb()
    }
    canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.38f, pinPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.22f, innerPaint)

    val coreDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BiteyOrange.toArgb()
    }
    canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.11f, coreDotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates modern GPS user location pulsing marker icon.
 */
private fun createUserLocationMarkerIcon(context: Context): BitmapDrawable {
    val sizePx = 64
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(60, 33, 150, 243)
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.46f, pulsePaint)

    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.30f, whitePaint)

    val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(33, 150, 243)
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.22f, bluePaint)

    return BitmapDrawable(context.resources, bitmap)
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        mapView.onResume()
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onPause()
        }
    }

    return mapView
}

@Composable
private fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> Unit
            }
        }
    }
