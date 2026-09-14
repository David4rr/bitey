package com.bitey.app.feature.journal.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import kotlinx.coroutines.launch
import java.io.File

/**
 * Ultra-responsive modular physics sticker canvas.
 * Allows assembling each sticker separately with independent drag, gravity drop, and accelerometer tilt.
 */
@Composable
fun GravitySticker(
    imageFiles: List<File>,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var tiltOffset by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context, isGravityEnabled) {
        if (!isGravityEnabled) onDispose { }
        else {
            val sm = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent?) { e?.values?.get(0)?.let { tiltOffset += (-it * 3.5f - tiltOffset) * 0.15f } }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            if (sensor != null) sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { if (sensor != null) sm?.unregisterListener(listener) }
        }
    }

    Box(modifier = modifier.fillMaxSize().onSizeChanged { containerSize = it }, contentAlignment = Alignment.Center) {
        val count = imageFiles.size
        val stickerSizeDp = if (count == 1) 195.dp else if (count == 2) 145.dp else if (count == 3) 125.dp else 110.dp
        imageFiles.forEachIndexed { index, file ->
            val initialOffset = when (count) {
                1 -> Pair(0f, 0f)
                2 -> if (index == 0) Pair(-38f * density.density, 10f * density.density) else Pair(38f * density.density, -10f * density.density)
                3 -> when (index) {
                    0 -> Pair(0f, -32f * density.density)
                    1 -> Pair(-42f * density.density, 30f * density.density)
                    else -> Pair(42f * density.density, 26f * density.density)
                }
                else -> {
                    val a = (index * 2 * Math.PI / count) - Math.PI / 2
                    Pair((40.0 * Math.cos(a) * density.density).toFloat(), (40.0 * Math.sin(a) * density.density).toFloat())
                }
            }
            ModularStickerItem(
                file = file, initialOffset = initialOffset, stickerSizeDp = stickerSizeDp,
                containerSize = containerSize, tiltOffset = tiltOffset, isGravityEnabled = isGravityEnabled,
                isStickerMode = isStickerMode, contentDescription = contentDescription, onClick = onClick
            )
        }
    }
}

@Composable
private fun ModularStickerItem(
    file: File,
    initialOffset: Pair<Float, Float>,
    stickerSizeDp: Dp,
    containerSize: IntSize,
    tiltOffset: Float,
    isGravityEnabled: Boolean,
    isStickerMode: Boolean,
    contentDescription: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val stickerPx = with(density) { stickerSizeDp.toPx() }

    var dragX by remember { mutableFloatStateOf(initialOffset.first) }
    var dragY by remember { mutableFloatStateOf(initialOffset.second) }
    val dropBounceY = remember { Animatable(initialOffset.second) }
    var isDragging by remember { mutableStateOf(false) }

    val maxDragX = ((containerSize.width - stickerPx) / 2f).coerceAtLeast(15f)
    val maxDragY = ((containerSize.height - stickerPx) / 2f).coerceAtLeast(25f)
    val floorY = if (containerSize.height > 0) ((containerSize.height - stickerPx) / 2f - with(density) { 8.dp.toPx() }).coerceAtLeast(0f) else 0f

    LaunchedEffect(isGravityEnabled, floorY) {
        if (isGravityEnabled && floorY > 0f) {
            dropBounceY.snapTo(dragY)
            dropBounceY.animateTo(floorY, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            dragY = floorY
        } else if (!isGravityEnabled) {
            dropBounceY.snapTo(dragY)
            dropBounceY.animateTo(initialOffset.second, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
            dragY = initialOffset.second
            dragX = initialOffset.first
        }
    }

    val dragScale by animateFloatAsState(if (isDragging) 1.08f else 1.0f, spring(stiffness = Spring.StiffnessMediumLow), label = "dragScale")
    val currentTilt = if (isGravityEnabled) tiltOffset.coerceIn(-maxDragX * 0.35f, maxDragX * 0.35f) else 0f
    val currentRotation = (dragX * 0.05f + currentTilt * 0.5f).coerceIn(-14f, 14f)

    val imageRequest = remember(file, isStickerMode) {
        ImageRequest.Builder(context).data(file)
            .apply { if (isStickerMode) transformations(CropTransparentTransformation()) }
            .size(500, 500).crossfade(false).build()
    }

    Box(
        modifier = Modifier.size(stickerSizeDp)
            .graphicsLayer {
                translationX = dragX + currentTilt
                translationY = if (isDragging) dragY else dropBounceY.value
                rotationZ = currentRotation
                scaleX = dragScale; scaleY = dragScale
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .pointerInput(maxDragX, maxDragY, isGravityEnabled, floorY) {
                val onRelease = {
                    isDragging = false
                    scope.launch {
                        if (isGravityEnabled) {
                            dropBounceY.snapTo(dragY)
                            dropBounceY.animateTo(floorY, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                            dragY = floorY
                        } else dropBounceY.snapTo(dragY)
                    }
                }
                detectDragGestures(onDragStart = { isDragging = true }, onDragEnd = { onRelease() }, onDragCancel = { onRelease() }) { change, dragAmount ->
                    change.consume()
                    dragX = (dragX + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                    dragY = (dragY + dragAmount.y).coerceIn(-maxDragY, if (isGravityEnabled) floorY else maxDragY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().then(if (isStickerMode) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(14.dp))),
            contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop,
            alignment = if (isGravityEnabled) Alignment.BottomCenter else Alignment.Center
        )
    }
}

@Composable
fun GravitySticker(
    imageFile: File,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = GravitySticker(listOf(imageFile), isStickerMode, isGravityEnabled, contentDescription, onClick, modifier)
