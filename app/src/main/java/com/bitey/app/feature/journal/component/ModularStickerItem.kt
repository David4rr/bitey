package com.bitey.app.feature.journal.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.image.CropTransparentTransformation
import java.io.File

@Composable
internal fun ModularStickerItem(
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
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val stickerPx = with(density) { stickerSizeDp.toPx() }
    val maxDragX = (containerSize.width / 2f - stickerPx * 0.15f).coerceAtLeast(48f * density.density)
    val maxDragY = (containerSize.height / 2f - stickerPx * 0.15f).coerceAtLeast(48f * density.density)
    val maxTiltX = (containerSize.width / 2f - stickerPx * 0.32f).coerceAtLeast(36f * density.density)
    val maxTiltY = (containerSize.height / 2f - stickerPx * 0.32f).coerceAtLeast(36f * density.density)

    val cachedPos = remember(file.absolutePath) { StickerPositionCache.getPosition(file.absolutePath, initialOffset) }
    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(cachedPos.first) }
    var dragY by remember { mutableFloatStateOf(cachedPos.second) }
    var userOffsetX by remember(file.absolutePath) { mutableFloatStateOf(cachedPos.first) }
    var userOffsetY by remember(file.absolutePath) { mutableFloatStateOf(cachedPos.second) }

    val scatterSpread = if (totalCount > 1) (index - (totalCount - 1) / 2f) * 22f * density.density else 0f
    val targetPhysicsX = if (isGravityEnabled) (tiltX * maxTiltX + scatterSpread).coerceIn(-maxDragX, maxDragX) else userOffsetX
    val targetPhysicsY = if (isGravityEnabled) (tiltY * maxTiltY).coerceIn(-maxDragY, maxDragY) else userOffsetY

    val animX by animateFloatAsState(
        targetValue = if (isDragging) dragX else targetPhysicsX,
        animationSpec = spring(dampingRatio = 0.46f, stiffness = 500f), label = "px"
    )
    val physicsX = if (isDragging) dragX else animX

    val animY by animateFloatAsState(
        targetValue = if (isDragging) dragY else targetPhysicsY,
        animationSpec = spring(dampingRatio = 0.46f, stiffness = 500f), label = "py"
    )
    val physicsY = if (isDragging) dragY else animY

    val currentX by rememberUpdatedState(physicsX)
    val currentY by rememberUpdatedState(physicsY)

    SideEffect {
        if (isGravityEnabled && !isDragging) {
            userOffsetX = physicsX
            userOffsetY = physicsY
        }
    }
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.48f, stiffness = 550f), label = "ds"
    )

    val rotation = if (isGravityEnabled) {
        val rot by animateFloatAsState(
            targetValue = if (isDragging) 0f else (tiltX * 18f + (index * 6f - 3f)).coerceIn(-24f, 24f),
            animationSpec = spring(dampingRatio = 0.50f, stiffness = 500f), label = "rot"
        )
        rot
    } else 0f

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
            .pointerInput(maxDragX, maxDragY, isGravityEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var overSlop = Offset.Zero
                    val drag = awaitTouchSlopOrCancellation(down.id) { change, over ->
                        change.consume()
                        overSlop = over
                    }
                    if (drag != null) {
                        isDragging = true
                        dragX = (currentX + overSlop.x).coerceIn(-maxDragX, maxDragX)
                        dragY = (currentY + overSlop.y).coerceIn(-maxDragY, maxDragY)
                        userOffsetX = dragX
                        userOffsetY = dragY
                        while (true) {
                            val event = awaitPointerEvent()
                            val dragEvent = event.changes.firstOrNull { it.id == drag.id } ?: break
                            if (dragEvent.isConsumed) break
                            if (dragEvent.changedToUp()) {
                                break
                            }
                            val change = dragEvent.positionChange()
                            dragX = (dragX + change.x).coerceIn(-maxDragX, maxDragX)
                            dragY = (dragY + change.y).coerceIn(-maxDragY, maxDragY)
                            userOffsetX = dragX
                            userOffsetY = dragY
                            dragEvent.consume()
                        }
                        isDragging = false
                        StickerPositionCache.setPosition(file.absolutePath, dragX, dragY)
                    } else {
                        onClick()
                    }
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
