package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinningDishBottomSheet(
    entryWithTags: PlateEntryWithTags,
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val entry = entryWithTags.entry
    val stickerPath = entry.stickerImagePath?.takeIf { File(it).exists() }
        ?: entry.getAllStickerPaths().firstOrNull { File(it).exists() }
        ?: entry.fullImagePath
    val isSticker = entry.isStickerMode || stickerPath != entry.fullImagePath
    val imageRequest = remember(stickerPath, isSticker) {
        ImageRequest.Builder(context)
            .data(File(stickerPath))
            .apply {
                if (isSticker) {
                    transformations(CropTransparentTransformation())
                }
            }
            .crossfade(true)
            .build()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Serendipity Decided!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BiteyOrange
            )
            Text(
                text = "Your next meal has been chosen",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = entry.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isSticker) Modifier.dieCutStickerEffect()
                            else Modifier.clip(RoundedCornerShape(20.dp))
                        ),
                    contentScale = if (isSticker) ContentScale.Fit else ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = theme.inkPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.Star, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = String.format(Locale.US, "%.1f", entry.rating), style = MaterialTheme.typography.labelMedium, color = theme.inkPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "•", color = theme.inkMuted)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = BiteyMint)
            }

            entry.locationName?.let { loc ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = theme.inkMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = loc, style = MaterialTheme.typography.bodySmall, color = theme.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "\"$note\"", style = MaterialTheme.typography.bodySmall, color = theme.inkMuted, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.inkPrimary)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Spin Again")
                }

                if (entry.latitude != null && entry.longitude != null) {
                    Button(
                        onClick = { onNavigate(); onDismiss() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite)
                    ) {
                        Icon(imageVector = Icons.Rounded.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate")
                    }
                }
            }
        }
    }
}
