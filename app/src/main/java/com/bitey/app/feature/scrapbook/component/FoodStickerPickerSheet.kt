package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File

/**
 * Reusable modal bottom sheet content for selecting food stickers across Scrapbook and canvas editors.
 */
@Composable
fun FoodStickerPickerSheet(
    entries: List<PlateEntryWithTags>,
    onSelectSticker: (PlateEntryWithTags) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Pick Food Sticker",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = theme.inkPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bites recorded yet. Record a meal in the journal to unlock stickers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().height(360.dp)
            ) {
                items(entries, key = { it.entry.id }) { item ->
                    val path = item.entry.stickerImagePath ?: item.entry.fullImagePath
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .minimalistCard(cornerRadius = 16.dp)
                            .clickable { onSelectSticker(item) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = item.entry.title,
                            modifier = Modifier.fillMaxSize().dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
