package com.bitey.app.feature.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bitey.app.core.ui.neumorphic.neumorphicRaised
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.InkPrimary
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToInt

enum class CameraRatio(val label: String, val ratioFloat: Float) {
    SQUARE_1_1("1:1", 1.0f),
    STANDARD_4_3("4:3", 3.0f / 4.0f)
}

@Composable
fun CameraViewfinder(
    onPhotoCaptured: (File) -> Unit,
    onClose: () -> Unit,
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

    // Tap-to-focus state
    var focusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Update flash mode when toggled
    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview with Tap-to-focus gesture
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val maxWidth = maxWidth
            val maxHeight = maxHeight

            // AndroidView embedding CameraX PreviewView
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val pView = previewViewRef ?: return@detectTapGestures
                            val factory = pView.meteringPointFactory
                            val point = factory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction.Builder(point).build()

                            cameraControl?.startFocusAndMetering(action)
                            focusPoint = Pair(offset.x, offset.y)
                            showFocusRing = true

                            coroutineScope.launch {
                                delay(1200)
                                showFocusRing = false
                            }
                        }
                    },
                update = { pView ->
                    previewViewRef = pView
                }
            )

            // Aspect Ratio Dimmer / Framing Guide
            if (selectedRatio == CameraRatio.SQUARE_1_1) {
                // Dim top and bottom regions to frame 1:1 square in center
                val boxWidthPx = with(density) { maxWidth.toPx() }
                val boxHeightPx = with(density) { maxHeight.toPx() }
                val squareSidePx = min(boxWidthPx, boxHeightPx)
                val squareSideDp = with(density) { squareSidePx.toDp() }
                val verticalMarginDp = (maxHeight - squareSideDp) / 2

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Dimmer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(verticalMarginDp)
                            .background(Color.Black.copy(alpha = 0.65f))
                    )
                    // Center Active Square Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(squareSideDp)
                            .border(1.dp, Color.White.copy(alpha = 0.35f))
                    )
                    // Bottom Dimmer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black.copy(alpha = 0.65f))
                    )
                }
            }

            // Tap-to-focus visual ring indicator
            focusPoint?.let { (fx, fy) ->
                val ringSize = 64.dp
                val ringHalfPx = with(density) { (ringSize / 2).toPx() }

                AnimatedVisibility(
                    visible = showFocusRing,
                    enter = scaleIn(tween(200)) + fadeIn(tween(150)),
                    exit = scaleOut(tween(300)) + fadeOut(tween(300)),
                    modifier = Modifier.offset {
                        IntOffset(
                            (fx - ringHalfPx).roundToInt(),
                            (fy - ringHalfPx).roundToInt()
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(ringSize)
                            .border(2.dp, BiteyWarmYellow, CircleShape)
                    )
                }
            }
        }

        // Camera Provider Binding
        LaunchedEffect(lensFacing) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = withContext(Dispatchers.IO) {
                cameraProviderFuture.get()
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewViewRef?.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    lensFacing,
                    preview,
                    imageCapture
                )
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                Log.e("CameraViewfinder", "Camera binding failed", e)
            }
        }

        // Re-attach surface provider once preview view is ready
        LaunchedEffect(previewViewRef) {
            previewViewRef?.let { pView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                val cameraProvider = withContext(Dispatchers.IO) { cameraProviderFuture.get() }
                cameraProvider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(pView.surfaceProvider)
                }
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    lensFacing,
                    preview,
                    imageCapture
                )
                cameraControl = camera.cameraControl
            }
        }

        // Top Control Overlay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close / Dismiss button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Camera",
                    tint = StickerDieCutWhite
                )
            }

            // Aspect Ratio Selector (1:1 vs 4:3)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CameraRatio.entries.forEach { ratio ->
                    val isSelected = selectedRatio == ratio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BiteyOrange else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .pointerInput(ratio) {
                                detectTapGestures { selectedRatio = ratio }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ratio.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) StickerDieCutWhite else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Flash Mode Toggle
            IconButton(
                onClick = {
                    flashMode = when (flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                        ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                        else -> ImageCapture.FLASH_MODE_OFF
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                val flashIcon = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Rounded.FlashAuto
                    else -> Icons.Rounded.FlashOff
                }
                Icon(
                    imageVector = flashIcon,
                    contentDescription = "Toggle Flash",
                    tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) BiteyWarmYellow else StickerDieCutWhite
                )
            }
        }

        // Bottom Capture Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(48.dp))

                // Tactile Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, StickerDieCutWhite, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = BiteyOrange,
                            modifier = Modifier.size(54.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BiteyOrange)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        if (isCapturing) return@detectTapGestures
                                        isCapturing = true

                                        val tempFile = File(
                                            context.cacheDir,
                                            "raw_capture_${UUID.randomUUID()}.jpg"
                                        )
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                                        imageCapture.takePicture(
                                            outputOptions,
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val finalFile = if (selectedRatio == CameraRatio.SQUARE_1_1) {
                                                            cropFileToSquare(tempFile)
                                                        } else {
                                                            tempFile
                                                        }
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
                                    }
                                }
                        )
                    }
                }

                // Switch Camera Lens (Back / Front)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = StickerDieCutWhite
                    )
                }
            }
        }
    }
}

private fun cropFileToSquare(sourceFile: File): File {
    return try {
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return sourceFile
        val width = originalBitmap.width
        val height = originalBitmap.height
        val minDim = min(width, height)
        val xOffset = (width - minDim) / 2
        val yOffset = (height - minDim) / 2

        val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, minDim, minDim)
        val squareFile = File(sourceFile.parentFile, "square_${sourceFile.name}")

        FileOutputStream(squareFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.flush()
        }

        if (croppedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
        croppedBitmap.recycle()
        sourceFile.delete()
        squareFile
    } catch (e: Exception) {
        Log.e("CameraViewfinder", "Crop to square failed", e)
        sourceFile
    }
}
