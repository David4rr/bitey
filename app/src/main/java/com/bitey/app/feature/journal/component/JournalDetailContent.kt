package com.bitey.app.feature.journal.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
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
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    var showPreview by remember { mutableStateOf(false) }
    val stickerFile = File(
        if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Pure sticker image floating cleanly, no grid, tap to preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = stickerFile,
                contentDescription = entry.title,
                modifier = Modifier
                    .sizeIn(maxWidth = 240.dp, maxHeight = 210.dp)
                    .clickable { showPreview = true },
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rating & Price row - compact & card-less
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
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
                if (price > 0.0) {
                    Text(
                        text = "${entry.currency} ${price.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BiteyMint
                    )
                }
            }
        }

        // Location - compact & card-less
        entry.locationName?.let { location ->
            if (location.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = location, style = MaterialTheme.typography.bodyMedium, color = theme.inkSecondary)
                }
            }
        }

        // Notes - compact & card-less
        if (!entry.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = theme.inkSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.note,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.inkPrimary,
                lineHeight = 21.sp
            )
        }

        // Tags - compact & card-less
        if (item.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = theme.inkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

    if (showPreview) {
        StickerPreviewDialog(
            imageFile = stickerFile,
            title = entry.title,
            onDismiss = { showPreview = false }
        )
    }
}
