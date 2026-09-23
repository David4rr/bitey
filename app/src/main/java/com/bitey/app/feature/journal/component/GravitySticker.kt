package com.bitey.app.feature.journal.component

import android.content.Context
import android.hardware.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.dishSharedElement
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import java.io.File
import kotlin.math.*

@Composable
fun GravitySticker(
    imageFiles: List<File>,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dishKey: String? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(1f) }

    DisposableEffect(context, isGravityEnabled) {
        if (!isGravityEnabled) onDispose { }
        else {
            val sm = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent?) {
                    val vals = e?.values ?: return
                    tiltX += ((-vals[0] / 9.8f).coerceIn(-1f, 1f) - tiltX) * 0.15f
                    tiltY += ((vals[1] / 9.8f).coerceIn(-1f, 1f) - tiltY) * 0.15f
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            if (sensor != null) sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
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
                    Pair((40.0 * cos(a) * density.density).toFloat(), (40.0 * sin(a) * density.density).toFloat())
                }
            }
            ModularStickerItem(
                file = file, index = index, totalCount = count, initialOffset = initialOffset,
                stickerSizeDp = stickerSizeDp, containerSize = containerSize,
                tiltX = tiltX, tiltY = tiltY, isGravityEnabled = isGravityEnabled,
                isStickerMode = isStickerMode, contentDescription = contentDescription, onClick = onClick,
                dishKey = if (index == 0) dishKey else null
            )
        }
    }
}

@Composable
private fun ModularStickerItem(
    file: File,
    index: Int,
    totalCount: Int,
    initialOffset: Pair<Float, Float>,
    stickerSizeDp: Dp,
    containerSize: IntSize,
    tiltX: Float,
    tiltY: Float,
    isGravityEnabled: Boolean,
    isStickerMode: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    dishKey: String? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val stickerPx = with(density) { stickerSizeDp.toPx() }
    val maxDragX = ((containerSize.width - stickerPx) / 2f).coerceAtLeast(10f)
    val maxDragY = ((containerSize.height - stickerPx) / 2f).coerceAtLeast(10f)

    val cachedPos = remember(file.absolutePath) { StickerPositionCache.getPosition(file.absolutePath, initialOffset) }
    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(cachedPos.first) }
    var dragY by remember { mutableFloatStateOf(cachedPos.second) }
    var userOffsetX by remember(file.absolutePath) { mutableFloatStateOf(cachedPos.first) }
    var userOffsetY by remember(file.absolutePath) { mutableFloatStateOf(cachedPos.second) }

    val scatterSpread = if (totalCount > 1) (index - (totalCount - 1) / 2f) * 22f * density.density else 0f
    val targetPhysicsX = if (isGravityEnabled) (tiltX * maxDragX + scatterSpread).coerceIn(-maxDragX, maxDragX) else userOffsetX
    val targetPhysicsY = if (isGravityEnabled) (tiltY * maxDragY).coerceIn(-maxDragY, maxDragY) else userOffsetY

    val physicsX by animateFloatAsState(
        targetValue = if (isDragging) dragX else targetPhysicsX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "px"
    )
    val physicsY by animateFloatAsState(
        targetValue = if (isDragging) dragY else targetPhysicsY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "py"
    )
    val dragScale by animateFloatAsState(if (isDragging) 1.08f else 1.0f, spring(stiffness = Spring.StiffnessMediumLow), label = "ds")
    val rotation by animateFloatAsState(
        targetValue = if (isGravityEnabled) (tiltX * 22f + (index * 6f - 3f)).coerceIn(-30f, 30f) else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow), label = "rot"
    )

    val imageRequest = remember(file, isStickerMode) {
        ImageRequest.Builder(context).data(file)
            .apply { if (isStickerMode) transformations(CropTransparentTransformation()) }
            .size(500, 500).crossfade(false).build()
    }

    Box(
        modifier = Modifier
            .size(stickerSizeDp)
            .graphicsLayer {
                translationX = physicsX; translationY = physicsY
                rotationZ = rotation; scaleX = dragScale; scaleY = dragScale
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .pointerInput(maxDragX, maxDragY, isGravityEnabled) {
                detectDragGestures(
                    onDragStart = { isDragging = true; dragX = physicsX; dragY = physicsY },
                    onDragEnd = { isDragging = false; userOffsetX = dragX; userOffsetY = dragY; StickerPositionCache.setPosition(file.absolutePath, dragX, dragY) },
                    onDragCancel = { isDragging = false; userOffsetX = dragX; userOffsetY = dragY; StickerPositionCache.setPosition(file.absolutePath, dragX, dragY) }
                ) { change, dragAmount ->
                    change.consume()
                    dragX = (dragX + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                    dragY = (dragY + dragAmount.y).coerceIn(-maxDragY, maxDragY)
                    userOffsetX = dragX
                    userOffsetY = dragY
                    StickerPositionCache.setPosition(file.absolutePath, dragX, dragY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest, contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().then(if (isStickerMode) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(14.dp))),
            contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop
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
    modifier: Modifier = Modifier,
    dishKey: String? = null
) = GravitySticker(listOf(imageFile), isStickerMode, isGravityEnabled, contentDescription, onClick, modifier, dishKey)
