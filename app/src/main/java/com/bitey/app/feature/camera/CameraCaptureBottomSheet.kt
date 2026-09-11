package com.bitey.app.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.camera.component.CameraPermissionCard
import com.bitey.app.feature.camera.component.CameraProcessingOverlay
import com.bitey.app.feature.entry.NewEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureBottomSheet(
    onDismissRequest: () -> Unit,
    onEntryCapturedAndPinned: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !uiState.isSegmenting }
    )

    val permissionsToRequest = remember {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!hasCameraPermission) {
            Toast.makeText(context, "Camera permission is required to take food photos.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasLocationPermission) {
            permissionsLauncher.launch(permissionsToRequest)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.autoProcessAndSaveUri(
                uri = uri,
                photoMode = PhotoMode.WHOLE_DISH,
                keepOriginal = true,
                onSuccess = { savedId, imagePath, stickerPath, timestamp, lat, lng ->
                    onEntryCapturedAndPinned(savedId, imagePath, stickerPath, timestamp, lat, lng)
                }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!uiState.isSegmenting) onDismissRequest()
        },
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(44.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        },
        containerColor = if (hasCameraPermission) Color(0xFF141416) else theme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        ) {
            if (hasCameraPermission) {
                CameraViewfinder(
                    onPhotoCaptured = {},
                    onClose = onDismissRequest,
                    onPickFromFile = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPhotoCapturedWithConfig = { file, photoMode, keepOriginal ->
                        viewModel.autoProcessAndSave(
                            file = file,
                            photoMode = photoMode,
                            keepOriginal = keepOriginal,
                            onSuccess = { savedId, imagePath, stickerPath, timestamp, lat, lng ->
                                onEntryCapturedAndPinned(savedId, imagePath, stickerPath, timestamp, lat, lng)
                            }
                        )
                    },
                    applyStatusBarPadding = false,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CameraPermissionCard(
                    onRequestPermissions = { permissionsLauncher.launch(permissionsToRequest) },
                    onPickFromGallery = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            if (uiState.isSegmenting || uiState.isLoading) {
                CameraProcessingOverlay(statusText = uiState.segmentationStatusText)
            }
        }
    }
}
