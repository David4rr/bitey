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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.neumorphicCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.InkMuted
import com.bitey.app.core.ui.theme.InkPrimary
import com.bitey.app.core.ui.theme.InkSecondary
import com.bitey.app.core.ui.theme.NeumorphicSurface
import com.bitey.app.core.ui.theme.SoftBackground
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CameraViewfinder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Modern Photo Picker launcher (Permissionless on Android 13+)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImagePicked(uri)
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
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
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                .background(SoftBackground)
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
                        .neumorphicCard(cornerRadius = 21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = InkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (uiState.selectedImage != null) "Review Your Bite" else "Paste Today's Bite",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary
                    )
                    Text(
                        text = if (uiState.selectedImage != null) "Optimized & ready for AI segmentation" else "Select photo or capture live",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
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
                targetState = Pair(uiState.isLoading, uiState.selectedImage != null),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "EntryContent"
            ) { (isLoading, hasImage) ->
                when {
                    isLoading -> {
                        LoadingCard()
                    }
                    hasImage && uiState.selectedImage != null -> {
                        ImagePreviewCard(
                            processedImage = uiState.selectedImage!!,
                            onRetake = { viewModel.clearSelectedImage() },
                            onContinue = {
                                Toast.makeText(
                                    context,
                                    "Ready for Phase 3: AI Subject Segmentation",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BiteyOrange,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Processing Media...",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Extracting EXIF & downscaling to WebP",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }
    }
}

@Composable
private fun AcquisitionOptionsCard(
    onPickFromGallery: () -> Unit,
    onCaptureLivePhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "How would you like to add your food?",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Device album does not require camera access",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gallery Button (Primary - no camera required!)
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

            // Camera Button (Optional)
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
                    color = InkPrimary
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewCard(
    processedImage: ProcessedImage,
    onRetake: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Preview Image Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
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
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
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
                        color = InkPrimary
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
                    "No GPS in file (will use current location in Phase 4)"
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
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Generate Food Sticker",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetake,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = InkSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Choose Another Photo",
                style = MaterialTheme.typography.labelLarge,
                color = InkPrimary
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = InkPrimary
        )
    }
}
