package com.bitey.app.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.camera.component.*
import com.bitey.app.feature.entry.NewEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun CameraCaptureBottomSheet(
    onDismissRequest: () -> Unit,
    anchorOffset: Offset? = null,
    onEntryCapturedAndPinned: (savedEntryId: Long, imagePath: String, stickerPath: String?, timestamp: Long, lat: Double?, lng: Double?) -> Unit = { _, _, _, _, _, _ -> },
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current
    val coroutineScope = rememberCoroutineScope()

    val animatable = remember { Animatable(0f) }
    var isClosing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
        )
    }

    fun dismissWithAnimation() {
        if (isClosing) return
        isClosing = true
        coroutineScope.launch {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            if (uiState.step != CaptureFlowStep.REVIEW) {
                viewModel.resetState()
            }
            onDismissRequest()
        }
    }

    fun handleDismissOrSave() {
        if (isClosing) return
        if (uiState.step == CaptureFlowStep.REVIEW) {
            viewModel.saveAllSelectedAndClose {
                dismissWithAnimation()
            }
        } else {
            dismissWithAnimation()
        }
    }

    BackHandler(enabled = true) {
        if (!isClosing) {
            if (uiState.showManualCutDialog) {
                viewModel.dismissManualCutDialog()
            } else {
                handleDismissOrSave()
            }
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val progress = animatable.value
                alpha = (progress / 0.08f).coerceIn(0f, 1f)
                clip = progress < 1f
                shape = if (progress < 1f) {
                    CircularRevealShape(progress, anchorOffset)
                } else {
                    RectangleShape
                }
            }
            .background(if (uiState.step == CaptureFlowStep.REVIEW) theme.background else Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
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
                        applyStatusBarPadding = true,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CameraPermissionCard(
                        onRequestPermissions = { permissionsLauncher.launch(permissionsToRequest) },
                        onPickFromGallery = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    )
                }
            }
            CaptureFlowStep.PROCESSING -> {
                InteractiveStickerCutoutView(
                    sourceFilePath = uiState.processingSourceFile,
                    stickerFilePath = uiState.processingStickerFile,
                    isProcessing = true,
                    statusText = uiState.segmentationStatusText,
                    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
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
                    onUpdateCandidateName = { id, name -> viewModel.updateCandidateLabel(id, name) },
                    onUpdateCandidateMealType = { id, type -> viewModel.updateCandidateMealType(id, type) },
                    onCutItMyselfClick = { viewModel.openManualCutDialog() },
                    onSaveAndClose = { handleDismissOrSave() },
                    isStickerMode = uiState.isStickerMode,
                    onStickerModeChange = { viewModel.setStickerMode(it) },
                    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
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
