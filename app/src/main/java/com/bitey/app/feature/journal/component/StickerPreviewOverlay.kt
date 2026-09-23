package com.bitey.app.feature.journal.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.LocalAnimatedVisibilityScope
import com.bitey.app.core.ui.dishSharedElement
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import java.io.File

@Composable
fun StickerPreviewOverlay(
    visible: Boolean,
    imageFile: File?,
    title: String,
    dishKey: String,
    isSticker: Boolean = true,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible && imageFile != null,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(180)),
        modifier = Modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            BackHandler(enabled = true) {
                onDismiss()
            }

            val context = LocalContext.current
            val imageRequest = remember(imageFile, isSticker) {
                if (imageFile == null) null
                else ImageRequest.Builder(context)
                    .data(imageFile)
                    .apply {
                        if (isSticker) transformations(CropTransparentTransformation())
                    }
                    .crossfade(true)
                    .build()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.80f)
                            .dishSharedElement(dishKey)
                            .then(if (isSticker) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(20.dp))),
                        contentScale = ContentScale.Fit
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 20.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close Preview",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
