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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File

/**
 * Ultra-responsive physics sticker.
 * Direct float drag without coroutine thrashing, zero idle frames, downsampled bitmap.
 */
@Composable
fun GravitySticker(
    imageFile: File,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val stickerSizeDp = 150.dp
    val stickerPx = with(density) { stickerSizeDp.toPx() }

    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val dropBounceY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var tiltOffset by remember { mutableFloatStateOf(0f) }

    val imageRequest = remember(imageFile) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .size(450, 450)
            .crossfade(false)
            .build()
    }

    LaunchedEffect(isGravityEnabled) {
        if (isGravityEnabled) {
            dropBounceY.snapTo(-80f)
            dropBounceY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            tiltOffset = 0f
            dropBounceY.snapTo(0f)
        }
    }

    DisposableEffect(context, isGravityEnabled) {
        if (!isGravityEnabled) {
            onDispose { }
        } else {
            val sm = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (sensor == null || sm == null) {
                onDispose { }
            } else {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event == null) return
                        val rawX = -event.values[0]
                        tiltOffset += (rawX * 3.5f - tiltOffset) * 0.15f
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                onDispose { sm.unregisterListener(listener) }
            }
        }
    }

    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.07f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "stickerScale"
    )

    val maxDragX = ((containerSize.width - stickerPx) / 2f).coerceAtLeast(15f)
    val maxDragY = ((containerSize.height - stickerPx) / 2f).coerceAtLeast(25f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it },
        contentAlignment = Alignment.Center
    ) {
        val currentTilt = if (isGravityEnabled) tiltOffset.coerceIn(-maxDragX * 0.35f, maxDragX * 0.35f) else 0f
        val currentRotation = if (isGravityEnabled) (dragX * 0.05f + currentTilt * 0.5f).coerceIn(-14f, 14f) else (dragX * 0.03f).coerceIn(-8f, 8f)

        Box(
            modifier = Modifier
                .size(stickerSizeDp)
                .graphicsLayer {
                    translationX = dragX + currentTilt
                    translationY = dragY + dropBounceY.value
                    rotationZ = currentRotation
                    scaleX = dragScale
                    scaleY = dragScale
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onClick() })
                }
                .pointerInput(maxDragX, maxDragY, isGravityEnabled) {
                    val onRelease = {
                        isDragging = false
                        if (isGravityEnabled) {
                            scope.launch {
                                val startY = dragY
                                dragY = 0f
                                dropBounceY.snapTo(startY)
                                dropBounceY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                            }
                        }
                    }
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { onRelease() },
                        onDragCancel = { onRelease() }
                    ) { change, dragAmount ->
                        change.consume()
                        dragX = (dragX + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                        val maxY = if (isGravityEnabled) 15f else maxDragY
                        dragY = (dragY + dragAmount.y).coerceIn(-maxDragY, maxY)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isStickerMode) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
