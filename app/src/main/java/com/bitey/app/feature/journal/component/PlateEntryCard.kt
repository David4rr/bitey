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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 12.dp, elevation = 0.dp)
            .clickable(onClick = onClick)
            .padding(14.dp)
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
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                val timeFormat = SimpleDateFormat("h:mm a", Locale.US).format(Date(entry.timestamp))
                val mealLabel = entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = mealLabel, style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = timeFormat, style = MaterialTheme.typography.bodySmall, color = theme.inkMuted, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
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

                entry.locationName?.let { loc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = loc, style = MaterialTheme.typography.bodySmall, color = theme.inkMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item.tags.take(3).forEach { tag ->
                            Text(
                                text = "#${tag.tagName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = BiteyOrange,
                                fontSize = 11.sp
                            )
                        }
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
