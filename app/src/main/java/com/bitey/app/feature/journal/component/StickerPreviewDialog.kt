package com.bitey.app.feature.journal.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import kotlinx.coroutines.launch
import java.io.File

private val PreviewFallbackScaleIn = scaleIn(initialScale = 0.75f)

@Composable
fun StickerPreviewDialog(
    imageFile: File,
    title: String,
    isSticker: Boolean = true,
    sourceBounds: Rect? = null,
    dishKey: String? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var previewBounds by remember { mutableStateOf<Rect?>(null) }
    val progress = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    val transition = remember(sourceBounds, previewBounds) {
        SharedDishTransition.create(sourceBounds, previewBounds)
    }

    LaunchedEffect(previewBounds != null) {
        if (previewBounds != null) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val dismissWithAnimation: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(180)
                )
                onDismiss()
            }
        }
    }

    BackHandler(enabled = true) {
        dismissWithAnimation()
    }

    val imageRequest = remember(imageFile, isSticker) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .apply {
                if (isSticker) {
                    transformations(CropTransparentTransformation())
                }
            }
            .crossfade(true)
            .build()
    }

    Dialog(
        onDismissRequest = dismissWithAnimation,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = (0.94f * progress.value).coerceIn(0f, 0.94f)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissWithAnimation
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.80f)
                    .onGloballyPositioned { coords ->
                        if (previewBounds == null) {
                            previewBounds = coords.boundsInWindow()
                        }
                    }
                    .sharedDishTransform(
                        transition = transition,
                        progress = progress.value,
                        fallbackScale = 0.75f + 0.25f * progress.value,
                        fallbackAlpha = progress.value
                    )
                    .then(if (isSticker) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(20.dp))),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = dismissWithAnimation,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close Preview",
                    tint = Color.White.copy(alpha = (0.75f * progress.value).coerceIn(0f, 0.75f)),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
