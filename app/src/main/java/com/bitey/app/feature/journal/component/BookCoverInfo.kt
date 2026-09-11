package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.util.Locale

@Composable
fun BookCoverHeader(
    entry: PlateEntryEntity,
    isGravityEnabled: Boolean,
    onToggleGravity: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BiteyOrange.copy(alpha = 0.12f))
                    .border(1.dp, BiteyOrange.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "VOL. ${entry.mealType.name.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = BiteyOrange,
                    fontSize = 9.sp
                )
            }

            if (entry.rating > 0f) {
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Star, contentDescription = "Rating", tint = BiteyOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGravityEnabled) BiteyOrange.copy(alpha = 0.15f) else theme.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, if (isGravityEnabled) BiteyOrange.copy(alpha = 0.4f) else theme.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleGravity)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.ScreenRotation, contentDescription = null, tint = if (isGravityEnabled) BiteyOrange else theme.inkMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = if (isGravityEnabled) "GRAV" else "FREE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), fontSize = 9.sp, color = if (isGravityEnabled) BiteyOrange else theme.inkMuted)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(theme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(3.dp)
            ) {
                AnimatedFavoriteButton(isFavorite = entry.isFavorite, onToggle = onToggleFavorite, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookCoverBottomInfo(
    item: PlateEntryWithTags,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = theme.inkPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            entry.price?.let { price ->
                if (price > 0.0) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", price),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BiteyOrange,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Text(
            text = "First Record • $formattedDate",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
            color = theme.inkMuted,
            fontSize = 10.sp
        )

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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                item.tags.take(4).forEach { tag ->
                    Text(text = "#${tag.tagName}", style = MaterialTheme.typography.labelSmall, color = BiteyOrange, fontSize = 11.sp)
                }
            }
        }
    }
}
