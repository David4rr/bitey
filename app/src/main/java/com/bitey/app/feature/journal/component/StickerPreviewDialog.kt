package com.bitey.app.feature.journal.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import java.io.File

@Composable
fun StickerPreviewDialog(
    imageFile: File,
    fullPhotoFile: File? = null,
    title: String,
    initialIsSticker: Boolean = true,
    onDismiss: () -> Unit
) {
    var isSticker by remember { mutableStateOf(initialIsSticker) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            val currentFile = if (isSticker && imageFile.exists()) imageFile else (fullPhotoFile ?: imageFile)

            val imageRequest = remember(currentFile, isSticker) {
                ImageRequest.Builder(context)
                    .data(currentFile)
                    .apply {
                        if (isSticker) {
                            transformations(CropTransparentTransformation())
                        }
                    }
                    .crossfade(true)
                    .build()
            }

            AnimatedContent(
                targetState = isSticker,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PreviewImageMorph"
            ) { stickerMode ->
                AsyncImage(
                    model = imageRequest,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.80f)
                        .then(
                            if (stickerMode) {
                                Modifier.dieCutStickerEffect()
                            } else {
                                Modifier.clip(RoundedCornerShape(20.dp))
                            }
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // Seamless toggle between Sticker and Original Photo if both exist
            if (fullPhotoFile != null && fullPhotoFile.exists() && imageFile.exists() && fullPhotoFile.absolutePath != imageFile.absolutePath) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isSticker = !isSticker }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSticker) Icons.Rounded.Photo else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSticker) "View Full Photo" else "View Die-Cut Sticker",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            // Minimalist Close button (top right)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Preview",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
