package com.bitey.app.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.camera.component.*
import com.bitey.app.feature.entry.NewEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureBottomSheet(
    onDismissRequest: () -> Unit,
    onEntryCapturedAndPinned: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit = { _, _, _, _, _, _ -> },
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permissionsToRequest = remember {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
        if (!hasCameraPermission) {
            Toast.makeText(context, "Camera permission is required to take food photos.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionsLauncher.launch(permissionsToRequest)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    fun handleDismissOrSave() {
        if (uiState.step == CaptureFlowStep.REVIEW) {
            viewModel.saveAllSelectedAndClose(onDismissRequest)
        } else {
            viewModel.resetState()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { handleDismissOrSave() },
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(44.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(if (uiState.step == CaptureFlowStep.REVIEW) theme.border else Color.White.copy(alpha = 0.5f))
            )
        },
        containerColor = if (uiState.step == CaptureFlowStep.REVIEW) theme.background else Color(0xFF141416),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
            when (uiState.step) {
                CaptureFlowStep.CAMERA -> {
                    if (hasCameraPermission) {
                        CameraViewfinder(
                            photoMode = uiState.photoMode,
                            onPhotoModeChange = { viewModel.setPhotoMode(it) },
                            capturedDishes = uiState.capturedDishes,
                            onPhotoCaptured = { viewModel.onPhotoCaptured(it) },
                            onClose = { handleDismissOrSave() },
                            onPickFromFile = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onDoneDishByDish = { viewModel.finishDishByDishCapture() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CameraPermissionCard(
                            onRequestPermissions = { permissionsLauncher.launch(permissionsToRequest) },
                            onPickFromGallery = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        )
                    }
                }
                CaptureFlowStep.PROCESSING -> {
                    InteractiveStickerCutoutView(
                        sourceFilePath = uiState.processingSourceFile,
                        stickerFilePath = uiState.processingStickerFile,
                        isProcessing = true,
                        statusText = uiState.segmentationStatusText
                    )
                }
                CaptureFlowStep.REVIEW -> {
                    NewEntryReviewView(
                        name = uiState.dishName,
                        onNameChange = { viewModel.updateDishName(it) },
                        mealType = uiState.mealType,
                        onMealTypeChange = { viewModel.updateMealType(it) },
                        candidates = uiState.candidates,
                        onToggleCandidate = { viewModel.toggleCandidate(it) },
                        onCutItMyselfClick = { viewModel.openManualCutDialog() },
                        onSaveAndClose = { viewModel.saveAllSelectedAndClose(onDismissRequest) }
                    )

                    if (uiState.showManualCutDialog) {
                        val original = uiState.candidates.firstOrNull()?.originalFilePath ?: uiState.processingSourceFile ?: ""
                        ManualCutDialog(
                            imageFilePath = original,
                            onDismiss = { viewModel.dismissManualCutDialog() },
                            onApplyCut = { viewModel.applyManualCut(it) }
                        )
                    }
                }
            }
        }
    }
}
