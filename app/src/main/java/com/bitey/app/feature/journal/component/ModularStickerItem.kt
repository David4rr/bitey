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

    val isAnimating = isDragging || isGravityEnabled
    val physicsX = if (isAnimating) {
        val animX by animateFloatAsState(
            targetValue = if (isDragging) dragX else targetPhysicsX,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "px"
        )
        animX
    } else userOffsetX

    val physicsY = if (isAnimating) {
        val animY by animateFloatAsState(
            targetValue = if (isDragging) dragY else targetPhysicsY,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "py"
        )
        animY
    } else userOffsetY

    val dragScale = if (isDragging) {
        val scale by animateFloatAsState(1.08f, spring(stiffness = Spring.StiffnessMediumLow), label = "ds")
        scale
    } else 1.0f

    val rotation = if (isGravityEnabled) {
        val rot by animateFloatAsState(
            targetValue = (tiltX * 22f + (index * 6f - 3f)).coerceIn(-30f, 30f),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "rot"
        )
        rot
    } else 0f

    val imageRequest = remember(file) {
        ImageRequest.Builder(context).data(file).size(500, 500).crossfade(false).build()
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
                        dragX = (physicsX + overSlop.x).coerceIn(-maxDragX, maxDragX)
                        dragY = (physicsY + overSlop.y).coerceIn(-maxDragY, maxDragY)
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
