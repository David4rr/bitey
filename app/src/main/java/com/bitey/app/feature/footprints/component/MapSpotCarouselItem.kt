package com.bitey.app.feature.footprints.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File

@Composable
fun MapSpotCarouselItem(
    item: PlateEntryWithTags,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = modifier
            .width(130.dp)
            .minimalistCard(cornerRadius = 18.dp)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .dieCutStickerEffect(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            entry.locationName?.let { loc ->
                Text(
                    text = loc,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
