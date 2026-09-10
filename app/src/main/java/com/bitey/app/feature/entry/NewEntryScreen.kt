package com.bitey.app.feature.entry

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CameraViewfinder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewEntryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    // Modern Photo Picker launcher (Permissionless on Android 13+)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImagePicked(uri)
        }
    }

    // Camera & Location permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        if (cameraGranted) {
            viewModel.openCamera()
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to capture photos directly.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun handleCameraClick() {
        val hasCamera = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val hasLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCamera) {
            if (!hasLocation) {
                permissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            viewModel.openCamera()
        } else {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (uiState.isCameraActive) {
        CameraViewfinder(
            onPhotoCaptured = { file ->
                viewModel.onPhotoCaptured(file)
            },
            onClose = {
                viewModel.closeCamera()
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Navigation Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .minimalistCard(cornerRadius = 21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.inkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    val title = when {
                        uiState.sticker != null -> "Die-Cut Sticker Ready"
                        uiState.selectedImage != null -> "Review Your Bite"
                        else -> "Paste Today's Bite"
                    }
                    val subtitle = when {
                        uiState.sticker != null -> "Tactile on-device sticker composited"
                        uiState.selectedImage != null -> "Optimized & ready for AI segmentation"
                        else -> "Select photo or capture live"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.inkPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.inkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message Banner
            uiState.errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(14.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Content Area with Animated Transitions
            AnimatedContent(
                targetState = Triple(
                    uiState.isLoading || uiState.isSegmenting,
                    uiState.selectedImage != null,
                    uiState.sticker != null
                ),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "EntryContent"
            ) { (isBusy, hasImage, hasSticker) ->
                when {
                    isBusy -> {
                        ProcessingCard(
                            title = if (uiState.isSegmenting) "AI Segmentation In Progress" else "Processing Media...",
                            status = if (uiState.isSegmenting) uiState.segmentationStatusText else "Extracting EXIF & downscaling to WebP"
                        )
                    }
                    hasSticker && uiState.sticker != null && uiState.selectedImage != null -> {
                        StickerReviewCard(
                            sticker = uiState.sticker!!,
                            originalImage = uiState.selectedImage!!,
                            style = uiState.stickerStyle,
                            onChangeStyle = { viewModel.openFallbackDialog() },
                            onContinue = {
                                val image = uiState.selectedImage!!
                                val sticker = uiState.sticker!!
                                onNavigateToEditor(
                                    image.file.absolutePath,
                                    sticker.file.absolutePath,
                                    image.exifMetadata.capturedAtMillis ?: System.currentTimeMillis(),
                                    image.exifMetadata.latitude,
                                    image.exifMetadata.longitude
                                )
                            }
                        )
                    }
                    hasImage && uiState.selectedImage != null -> {
                        ImagePreviewCard(
                            processedImage = uiState.selectedImage!!,
                            onGenerateSticker = { viewModel.generateSticker(StickerStyle.AI_SEGMENTED) },
                            onOpenStyleOptions = { viewModel.openFallbackDialog() },
                            onContinueWithoutSticker = {
                                val image = uiState.selectedImage!!
                                onNavigateToEditor(
                                    image.file.absolutePath,
                                    null,
                                    image.exifMetadata.capturedAtMillis ?: System.currentTimeMillis(),
                                    image.exifMetadata.latitude,
                                    image.exifMetadata.longitude
                                )
                            },
                            onRetake = { viewModel.clearSelectedImage() }
                        )
                    }
                    else -> {
                        AcquisitionOptionsCard(
                            onPickFromGallery = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onCaptureLivePhoto = { handleCameraClick() }
                        )
                    }
                }
            }
        }

        // Fallback / Style Selection Dialog
        if (uiState.showFallbackDialog) {
            FallbackStyleDialog(
                onDismiss = { viewModel.dismissFallbackDialog() },
                onSelectStyle = { style ->
                    viewModel.generateSticker(style)
                }
            )
        }
    }
}

@Composable
private fun ProcessingCard(
    title: String,
    status: String
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BiteyOrange,
                modifier = Modifier.size(52.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary
            )
        }
    }
}

@Composable
private fun AcquisitionOptionsCard(
    onPickFromGallery: () -> Unit,
    onCaptureLivePhoto: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "How would you like to add your food?",
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Device album does not require camera access",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gallery Button
            Button(
                onClick = onPickFromGallery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange,
                    contentColor = StickerDieCutWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pick from Device Album",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Camera Button
            OutlinedButton(
                onClick = onCaptureLivePhoto,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = BiteyMint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Take a Live Photo",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.inkPrimary
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewCard(
    processedImage: ProcessedImage,
    onGenerateSticker: () -> Unit,
    onOpenStyleOptions: () -> Unit,
    onContinueWithoutSticker: () -> Unit,
    onRetake: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Preview Image Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = processedImage.file,
                contentDescription = "Processed Food Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // EXIF Metadata Breakdown Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = BiteyMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Memory & EXIF Extracted",
                        style = MaterialTheme.typography.titleSmall,
                        color = theme.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time
                val dateText = processedImage.exifMetadata.capturedAtMillis?.let {
                    SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm", Locale.US).format(Date(it))
                } ?: "Captured Just Now"

                MetadataRow(
                    icon = Icons.Rounded.CalendarToday,
                    title = "Timestamp",
                    value = dateText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location GPS
                val locationText = if (processedImage.exifMetadata.latitude != null && processedImage.exifMetadata.longitude != null) {
                    String.format(
                        Locale.US,
                        "%.4f, %.4f (Ready for Geocoding)",
                        processedImage.exifMetadata.latitude,
                        processedImage.exifMetadata.longitude
                    )
                } else {
                    "Location pinned upon capture"
                }

                MetadataRow(
                    icon = Icons.Rounded.LocationOn,
                    title = "Location",
                    value = locationText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dimensions and Compression
                val sizeKb = processedImage.sizeBytes / 1024
                MetadataRow(
                    icon = Icons.Rounded.Info,
                    title = "Format",
                    value = "${processedImage.width} x ${processedImage.height} px • ${sizeKb} KB WebP"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Button(
            onClick = onGenerateSticker,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteyOrange,
                contentColor = StickerDieCutWhite
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Generate Food Sticker (AI)",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onOpenStyleOptions,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Crop,
                    contentDescription = null,
                    tint = theme.inkSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Crop Styles",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.inkPrimary
                )
            }

            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = theme.inkSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Photo",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.inkPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onContinueWithoutSticker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip sticker & continue with original photo",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary
            )
        }
    }
}

@Composable
private fun StickerReviewCard(
    sticker: CompositedSticker,
    originalImage: ProcessedImage,
    style: StickerStyle,
    onChangeStyle: () -> Unit,
    onContinue: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Tab row to toggle between Sticker and Original Photo
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = theme.surfaceVariant,
            contentColor = BiteyOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BiteyOrange,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sticker View", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Original Photo", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == 0) {
                // Sticker with die-cut effect
                AsyncImage(
                    model = sticker.file,
                    contentDescription = "Die-Cut Food Sticker",
                    modifier = Modifier
                        .fillMaxSize()
                        .dieCutStickerEffect(),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Original photo
                AsyncImage(
                    model = originalImage.file,
                    contentDescription = "Original Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sticker Info Badge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Style,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sticker Style",
                            style = MaterialTheme.typography.titleSmall,
                            color = theme.inkPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BiteyOrange.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = BiteyOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val stickerKb = sticker.sizeBytes / 1024
                MetadataRow(
                    icon = Icons.Rounded.Info,
                    title = "Artifact",
                    value = "${sticker.width} x ${sticker.height} px • ${stickerKb} KB WebP"
                )

                Spacer(modifier = Modifier.height(6.dp))

                MetadataRow(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Outline & Shadow",
                    value = "12px die-cut white border + soft drop shadow"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteyOrange,
                contentColor = StickerDieCutWhite
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Save & Continue to Journal Entry",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onChangeStyle,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = theme.inkSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Change Sticker Style or Crop",
                style = MaterialTheme.typography.labelLarge,
                color = theme.inkPrimary
            )
        }
    }
}

@Composable
private fun FallbackStyleDialog(
    onDismiss: () -> Unit,
    onSelectStyle: (StickerStyle) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Sticker Style",
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose how your food is clipped into a die-cut sticker:",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyleOptionItem(
                    title = "AI Subject Cutout",
                    subtitle = "Automatically extracts the food foreground",
                    onClick = { onSelectStyle(StickerStyle.AI_SEGMENTED) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                StyleOptionItem(
                    title = "Circular Plate Badge",
                    subtitle = "Classic round collectible sticker crop",
                    onClick = { onSelectStyle(StickerStyle.CIRCULAR_BADGE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                StyleOptionItem(
                    title = "Polaroid Rounded Tile",
                    subtitle = "Soft rounded rectangle tile with die-cut border",
                    onClick = { onSelectStyle(StickerStyle.ROUNDED_TILE) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.inkSecondary)
            }
        },
        containerColor = theme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun StyleOptionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted
            )
        }
    }
}

@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    val theme = LocalNeumorphicTheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.inkMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodySmall,
            color = theme.inkSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = theme.inkPrimary
        )
    }
}
