package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlateEntryCard(
    item: PlateEntryWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val context = LocalContext.current
    val imageRequest = remember(entry, context) {
        val path = if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath
        ImageRequest.Builder(context)
            .data(File(path))
            .size(240, 240)
            .crossfade(false)
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                val stickerCount = remember(entry) { entry.getIndividualStickerPaths().size }
                if (stickerCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BiteyOrange)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$stickerCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkPrimary,
                    maxLines = if (entry.note.isNullOrBlank()) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!entry.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = theme.inkPrimary
                    )

                    entry.price?.let { price ->
                        if (price > 0.0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${entry.currency} ${price.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BiteyMint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val timeFormat = SimpleDateFormat("h:mm a", Locale.US).format(Date(entry.timestamp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeFormat,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkMuted,
                        fontSize = 11.sp
                    )

                    entry.locationName?.takeIf { it.isNotBlank() }?.let { loc ->
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkMuted,
                            fontSize = 11.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    if (item.tags.isNotEmpty()) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = item.tags.take(2).joinToString(" ") { "#${it.tagName}" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = BiteyOrange,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            AnimatedFavoriteButton(
                isFavorite = entry.isFavorite,
                onToggle = onToggleFavorite,
                containerSize = 36.dp,
                iconSize = 20.dp,
                withNeumorphicContainer = false
            )
        }
    }
}
