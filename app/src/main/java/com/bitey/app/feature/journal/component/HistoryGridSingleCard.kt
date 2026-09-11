package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryGridSingleCard(
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
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.94f)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BiteyOrange.copy(alpha = 0.12f))
                            .border(1.dp, BiteyOrange.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entry.mealType.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = BiteyOrange,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (entry.rating > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.Star, contentDescription = "Rating", tint = BiteyOrange, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", entry.rating),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BiteyOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                AnimatedFavoriteButton(isFavorite = entry.isFavorite, onToggle = onToggleFavorite, modifier = Modifier.size(34.dp))
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(model = imageFile, contentDescription = entry.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    entry.price?.let { price ->
                        if (price > 0.0) {
                            Text(
                                text = String.format(Locale.US, "$%.2f", price),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BiteyOrange
                            )
                        }
                    }
                }

                Text(text = formattedDate, style = MaterialTheme.typography.labelSmall, color = theme.inkMuted, fontSize = 11.sp)

                entry.locationName?.let { location ->
                    if (location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = location, style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                        }
                    }
                }

                if (item.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        item.tags.take(4).forEach { tag ->
                            Text(text = "#${tag.tagName}", style = MaterialTheme.typography.labelSmall, color = BiteyOrange, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
