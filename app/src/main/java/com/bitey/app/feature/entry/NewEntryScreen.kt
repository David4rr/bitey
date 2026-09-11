package com.bitey.app.feature.entry

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.entry.component.*

@Composable
fun NewEntryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.CAMERA] == true) {
            viewModel.openCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos directly.", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraClick() {
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasCamera) {
            if (!hasLocation) permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            viewModel.openCamera()
        } else {
            permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(42.dp).minimalistCard(cornerRadius = 21.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = theme.inkPrimary)
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
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = theme.inkPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = theme.inkSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(14.dp)
            ) {
                Text(text = error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedContent(
            targetState = Triple(uiState.isLoading || uiState.isSegmenting, uiState.selectedImage != null, uiState.sticker != null),
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
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onCaptureLivePhoto = { handleCameraClick() }
                    )
                }
            }
        }
    }

    if (uiState.showFallbackDialog) {
        FallbackStyleDialog(
            onDismiss = { viewModel.dismissFallbackDialog() },
            onSelectStyle = { style -> viewModel.generateSticker(style) }
        )
    }

    if (uiState.isCameraActive) {
        NewEntryCameraSheet(
            onDismiss = { viewModel.closeCamera() },
            onPhotoCaptured = { file -> viewModel.onPhotoCaptured(file) }
        )
    }
}
