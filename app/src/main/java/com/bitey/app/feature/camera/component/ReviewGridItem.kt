package com.bitey.app.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CandidateStickerItem
import java.io.File

@Composable
internal fun ReviewGridItem(
    item: CandidateStickerItem,
    isStickerMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val displayFile = File(if (isStickerMode) item.stickerFilePath else item.originalFilePath)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surface)
            .border(
                width = 1.dp,
                color = if (item.isSelected) BiteyOrange.copy(alpha = 0.35f) else theme.border.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .alpha(if (item.isSelected) 1f else 0.45f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = displayFile,
            contentDescription = "Dish sticker",
            contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (isStickerMode) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(10.dp)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (item.isSelected) BiteyOrange else theme.surface.copy(alpha = 0.8f))
                .border(1.dp, if (item.isSelected) BiteyOrange.copy(alpha = 0.5f) else theme.border.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (item.isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
