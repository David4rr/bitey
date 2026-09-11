package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.entry.StickerStyle

@Composable
fun StickerInfoCard(
    sticker: CompositedSticker,
    style: StickerStyle,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Style,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sticker Style",
                        style = MaterialTheme.typography.titleSmall,
                        color = theme.inkPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BiteyOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = BiteyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val stickerKb = sticker.sizeBytes / 1024
            MetadataRow(
                icon = Icons.Rounded.Info,
                title = "Artifact",
                value = "${sticker.width} x ${sticker.height} px • ${stickerKb} KB WebP"
            )

            Spacer(modifier = Modifier.height(6.dp))

            MetadataRow(
                icon = Icons.Rounded.CheckCircle,
                title = "Outline & Shadow",
                value = "12px die-cut white border + soft drop shadow"
            )
        }
    }
}
