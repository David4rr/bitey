package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File

@Composable
fun MediaPreviewCard(
    imagePath: String,
    stickerPath: String?,
    isStickerMode: Boolean,
    onToggleStickerMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val previewModel = remember(isStickerMode, stickerPath, imagePath) {
        val path = if (isStickerMode && stickerPath != null) stickerPath else imagePath
        path.takeIf { it.isNotBlank() }?.let { File(it) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isStickerMode && stickerPath != null) {
            AsyncImage(
                model = previewModel,
                contentDescription = "Food Sticker",
                modifier = Modifier
                    .fillMaxSize()
                    .dieCutStickerEffect(),
                contentScale = ContentScale.Fit
            )
        } else {
            AsyncImage(
                model = previewModel,
                contentDescription = "Food Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Mode toggle chip if sticker is present
        if (stickerPath != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surface.copy(alpha = 0.92f))
                    .clickable { onToggleStickerMode() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isStickerMode) Icons.Rounded.AutoAwesome else Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStickerMode) "Sticker" else "Photo",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.inkPrimary
                    )
                }
            }
        }
    }
}
