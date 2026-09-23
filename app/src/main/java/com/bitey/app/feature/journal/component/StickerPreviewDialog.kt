package com.bitey.app.feature.journal.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StickerPreviewDialog(
    imageFile: File,
    title: String,
    isSticker: Boolean = true,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dismissWithAnimation: () -> Unit = {
        isVisible = false
        coroutineScope.launch {
            delay(150)
            onDismiss()
        }
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
                .background(Color.Black.copy(alpha = if (isVisible) 0.94f else 0f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissWithAnimation
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(180)) + scaleIn(
                    initialScale = 0.75f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(tween(150)) + scaleOut(
                    targetScale = 0.75f,
                    animationSpec = tween(150)
                )
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.80f)
                        .then(if (isSticker) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(20.dp))),
                    contentScale = ContentScale.Fit
                )
            }

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
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
