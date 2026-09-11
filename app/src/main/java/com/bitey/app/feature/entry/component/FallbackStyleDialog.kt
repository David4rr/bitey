package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.entry.StickerStyle

@Composable
fun FallbackStyleDialog(
    onDismiss: () -> Unit,
    onSelectStyle: (StickerStyle) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Sticker Style",
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose how your food is clipped into a die-cut sticker:",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyleOptionItem(
                    title = "AI Subject Cutout",
                    subtitle = "Automatically extracts the food foreground",
                    onClick = { onSelectStyle(StickerStyle.AI_SEGMENTED) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                StyleOptionItem(
                    title = "Circular Plate Badge",
                    subtitle = "Classic round collectible sticker crop",
                    onClick = { onSelectStyle(StickerStyle.CIRCULAR_BADGE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                StyleOptionItem(
                    title = "Polaroid Rounded Tile",
                    subtitle = "Soft rounded rectangle tile with die-cut border",
                    onClick = { onSelectStyle(StickerStyle.ROUNDED_TILE) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.inkSecondary)
            }
        },
        containerColor = theme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun StyleOptionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted
            )
        }
    }
}
