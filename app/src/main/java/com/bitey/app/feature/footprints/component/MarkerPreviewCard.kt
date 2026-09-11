package com.bitey.app.feature.footprints.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MarkerPreviewCard(
    item: PlateEntryWithTags,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 12.dp, elevation = 0.dp)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(86.dp).clip(RoundedCornerShape(18.dp)),
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

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(entry.timestamp))
                    Text(
                        text = "$dateStr • ${entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (entry.rating >= i) BiteyOrange else theme.inkMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", entry.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.inkPrimary
                        )
                        entry.price?.let { price ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${entry.currency} ${price.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BiteyMint
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = theme.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    AnimatedFavoriteButton(
                        isFavorite = entry.isFavorite,
                        onToggle = onToggleFavorite,
                        containerSize = 34.dp,
                        iconSize = 18.dp,
                        withNeumorphicContainer = true
                    )
                }
            }

            entry.locationName?.let { loc ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = loc,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Directions,
                    contentDescription = null,
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Navigate with Google Maps",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = StickerDieCutWhite
                )
            }
        }
    }
}
