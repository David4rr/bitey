package com.bitey.app.feature.camera

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bitey.app.feature.camera.component.CameraBottomBar
import com.bitey.app.feature.camera.component.CameraFramingOverlay
import com.bitey.app.feature.camera.component.CameraTopBar
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import kotlin.math.min

@Composable
fun CameraViewfinder(
    photoMode: PhotoMode = PhotoMode.ONESHOT,
    onPhotoModeChange: (PhotoMode) -> Unit = {},
    capturedDishes: List<File> = emptyList(),
    onPhotoCaptured: (File) -> Unit,
    onClose: () -> Unit,
    onPickFromFile: (() -> Unit)? = null,
    onDoneDishByDish: () -> Unit = {},
    applyStatusBarPadding: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var selectedRatio by remember { mutableStateOf(CameraRatio.SQUARE_1_1) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var isCapturing by remember { mutableStateOf(false) }
    var keepOriginal by remember { mutableStateOf(true) }
    var focusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setFlashMode(flashMode).build()
    }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(flashMode) { imageCapture.flashMode = flashMode }

    LaunchedEffect(lensFacing, previewViewRef) {
        val pView = previewViewRef ?: return@LaunchedEffect
        val cameraProvider = withContext(Dispatchers.IO) { ProcessCameraProvider.getInstance(context).get() }
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(pView.surfaceProvider) }
        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, lensFacing, preview, imageCapture)
            cameraControl = camera.cameraControl
        } catch (e: Exception) {
            Log.e("CameraViewfinder", "Camera binding failed", e)
        }
    }

    DisposableEffect(context) {
        onDispose {
            runCatching {
                ProcessCameraProvider.getInstance(context.applicationContext).get().unbindAll()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val pView = previewViewRef ?: return@detectTapGestures
                        val point = pView.meteringPointFactory.createPoint(offset.x, offset.y)
                        cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
                        focusPoint = Pair(offset.x, offset.y)
                        showFocusRing = true
                        coroutineScope.launch { delay(1200); showFocusRing = false }
                    }
                },
                update = { pView -> previewViewRef = pView }
            )

            val squareSideDp = with(density) { min(maxWidth.toPx(), maxHeight.toPx()).toDp() }
            CameraFramingOverlay(
                selectedRatio = selectedRatio,
                squareSideDp = squareSideDp,
                verticalMarginDp = (maxHeight - squareSideDp) / 2,
                focusPoint = focusPoint,
                showFocusRing = showFocusRing
            )
        }

        CameraTopBar(
            selectedRatio = selectedRatio,
            onRatioSelected = { selectedRatio = it },
            flashMode = flashMode,
            onFlashToggle = {
                flashMode = when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_OFF
                }
            },
            onClose = onClose,
            applyStatusBarPadding = applyStatusBarPadding,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        CameraBottomBar(
            photoMode = photoMode,
            onPhotoModeChange = onPhotoModeChange,
            keepOriginal = keepOriginal,
            onKeepOriginalToggle = { keepOriginal = !keepOriginal },
            isCapturing = isCapturing,
            onCapture = {
                if (isCapturing) return@CameraBottomBar
                isCapturing = true
                val tempFile = File(context.cacheDir, "raw_capture_${UUID.randomUUID()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val finalFile = if (selectedRatio == CameraRatio.SQUARE_1_1) cropFileToSquare(tempFile) else tempFile
                                withContext(Dispatchers.Main) {
                                    isCapturing = false
                                    onPhotoCaptured(finalFile)
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraViewfinder", "Capture error: ${exception.message}", exception)
                            isCapturing = false
                        }
                    }
                )
            },
            onSwitchCamera = {
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            },
            onPickFromFile = onPickFromFile,
            capturedDishes = capturedDishes,
            onDoneDishByDish = onDoneDishByDish,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
