package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.neumorphic.minimalistInset
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalDetailContent(
    item: PlateEntryWithTags,
    isShowingSticker: Boolean,
    onToggleSticker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val currentFile = File(
        if (isShowingSticker && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .minimalistCard(cornerRadius = 20.dp, elevation = 0.dp)
                .background(theme.surfaceVariant)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isShowingSticker && entry.stickerImagePath != null) {
                AsyncImage(model = currentFile, contentDescription = entry.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                AsyncImage(
                    model = currentFile,
                    contentDescription = entry.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (entry.stickerImagePath != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surface.copy(alpha = 0.92f))
                        .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                        .clickable(onClick = onToggleSticker)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isShowingSticker) Icons.Rounded.Layers else Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isShowingSticker) "View Photo" else "View Sticker",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = theme.inkPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 14.dp, elevation = 0.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format(Locale.US, "%.1f", entry.rating),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )
            }

            entry.price?.let { price ->
                Text(
                    text = "${entry.currency} ${price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BiteyMint
                )
            }
        }

        entry.locationName?.let { location ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimalistCard(cornerRadius = 14.dp, elevation = 0.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = location, style = MaterialTheme.typography.bodyMedium, color = theme.inkPrimary)
            }
        }

        if (!entry.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Palate Impressions",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimalistInset(cornerRadius = 14.dp)
                    .padding(14.dp)
            ) {
                Text(text = entry.note, style = MaterialTheme.typography.bodyMedium, color = theme.inkPrimary, lineHeight = 22.sp)
            }
        }

        if (item.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Tags & Flavors",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item.tags.forEach { tag ->
                    Text(
                        text = "#${tag.tagName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BiteyOrange
                    )
                }
            }
        }
    }
}
