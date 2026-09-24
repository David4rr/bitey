package com.bitey.app.feature.journal.component

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import kotlinx.coroutines.launch
import java.io.File

fun LayoutCoordinates.boundsOnScreen(view: View): Rect {
    if (!isAttached) return Rect.Zero
    val bounds = boundsInRoot()
    val loc = IntArray(2)
    view.getLocationOnScreen(loc)
    return Rect(
        left = loc[0].toFloat() + bounds.left,
        top = loc[1].toFloat() + bounds.top,
        right = loc[0].toFloat() + bounds.right,
        bottom = loc[1].toFloat() + bounds.bottom
    )
}

@Composable
fun StickerPreviewDialog(
    imageFile: File,
    title: String,
    isSticker: Boolean = true,
    sourceRect: Rect? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val progress = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    val dismissWithAnimation: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
                onDismiss()
            }
        }
    }

    BackHandler(enabled = true, onBack = dismissWithAnimation)

    val thumbnailRequest = remember(imageFile, isSticker) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .size(500, 500)
            .apply {
                if (isSticker) {
                    transformations(CropTransparentTransformation())
                }
            }
            .build()
    }

    val fullImageRequest = remember(imageFile, isSticker) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .size(1080, 1080)
            .apply {
                if (isSticker) {
                    transformations(CropTransparentTransformation())
                }
            }
            .crossfade(true)
            .build()
    }

    val placeholderPainter = rememberAsyncImagePainter(model = thumbnailRequest)

    Dialog(
        onDismissRequest = dismissWithAnimation,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            var parent = view.parent
            while (parent != null) {
                if (parent is DialogWindowProvider) {
                    parent.window.setWindowAnimations(0)
                    break
                }
                parent = parent.parent
            }
            onDispose { }
        }

        var dialogScreenOffset by remember { mutableStateOf(Offset.Zero) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    if (coords.isAttached) {
                        val loc = IntArray(2)
                        view.getLocationOnScreen(loc)
                        dialogScreenOffset = Offset(loc[0].toFloat(), loc[1].toFloat())
                    }
                }
                .drawBehind {
                    val alpha = (progress.value * 0.94f).coerceIn(0f, 0.94f)
                    drawRect(Color.Black.copy(alpha = alpha))
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissWithAnimation
                ),
            contentAlignment = Alignment.Center
        ) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()
            val targetCenterX = screenWidthPx / 2f
            val targetCenterY = screenHeightPx / 2f
            val targetWidthPx = if (isSticker) screenWidthPx * 0.88f else screenWidthPx * 0.92f

            AsyncImage(
                model = fullImageRequest,
                placeholder = placeholderPainter,
                contentDescription = title,
                modifier = Modifier
                    .then(
                        if (isSticker) {
                            Modifier
                                .fillMaxWidth(0.88f)
                                .aspectRatio(1f)
                                .dieCutStickerEffect()
                        } else {
                            Modifier
                                .fillMaxWidth(0.92f)
                                .fillMaxHeight(0.80f)
                                .clip(RoundedCornerShape(20.dp))
                        }
                    )
                    .graphicsLayer {
                        val p = progress.value
                        if (sourceRect != null && targetWidthPx > 0f) {
                            val localSourceLeft = sourceRect.left - dialogScreenOffset.x
                            val localSourceTop = sourceRect.top - dialogScreenOffset.y
                            val localSourceCenterX = localSourceLeft + sourceRect.width / 2f
                            val localSourceCenterY = localSourceTop + sourceRect.height / 2f

                            val deltaX = localSourceCenterX - targetCenterX
                            val deltaY = localSourceCenterY - targetCenterY
                            val startScale = (sourceRect.width / targetWidthPx).coerceIn(0.08f, 0.95f)

                            translationX = lerp(deltaX, 0f, p)
                            translationY = lerp(deltaY, 0f, p)
                            val s = lerp(startScale, 1f, p)
                            scaleX = s
                            scaleY = s
                        } else {
                            val s = lerp(0.55f, 1f, p)
                            scaleX = s
                            scaleY = s
                        }
                    },
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = dismissWithAnimation,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 20.dp)
                    .size(40.dp)
                    .graphicsLayer {
                        val p = progress.value
                        val alpha = ((p - 0.40f) / 0.60f).coerceIn(0f, 1f)
                        this.alpha = alpha
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close Preview",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
