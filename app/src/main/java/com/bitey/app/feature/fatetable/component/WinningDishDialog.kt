package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File
import java.util.Locale

@Composable
fun WinningDishDialog(
    entryWithTags: PlateEntryWithTags,
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    val entry = entryWithTags.entry
    val imageFile = File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (entry.latitude != null && entry.longitude != null) {
                Button(
                    onClick = { onNavigate(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate Now")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.inkPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Spin Again")
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Serendipity Decided!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BiteyOrange,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your next meal has been chosen",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Box(
                    modifier = Modifier.size(160.dp).aspectRatio(1f).minimalistCard(cornerRadius = 24.dp, elevation = 2.dp).padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.isStickerMode && entry.stickerImagePath != null) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier.fillMaxSize().dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = theme.inkMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = loc, style = MaterialTheme.typography.bodySmall, color = theme.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "\"$note\"", style = MaterialTheme.typography.bodySmall, color = theme.inkMuted, textAlign = TextAlign.Center)
                }
            }
        },
        containerColor = theme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
