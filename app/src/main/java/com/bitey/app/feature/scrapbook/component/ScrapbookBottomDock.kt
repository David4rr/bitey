package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

val BackgroundPalettes = listOf(
    0xFFF4F1EAL, // Soft Cream
    0xFFEBF5EEL, // Pale Mint
    0xFFFFF3E0L, // Warm Peach
    0xFF263238L  // Deep Slate
)

/**
 * Minimalist bottom action dock with streamlined triggers for stickers,
 * decorations, and subtle canvas background palette swatches.
 */
@Composable
fun ScrapbookBottomDock(
    currentBackgroundColorHex: Long,
    onOpenFoodStickers: () -> Unit,
    onOpenAccessories: () -> Unit,
    onSelectBackgroundColor: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .minimalistCard(cornerRadius = 20.dp, elevation = 2.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // + Sticker primary action pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BiteyOrange)
                    .clickable(onClick = onOpenFoodStickers)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sticker",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = StickerDieCutWhite
                )
            }

            // + Deco secondary action pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceVariant)
                    .clickable(onClick = onOpenAccessories)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = theme.inkPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Deco",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkPrimary
                )
            }
        }

        // Minimalist paper background swatches
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackgroundPalettes.forEach { colorHex ->
                val isSelected = currentBackgroundColorHex == colorHex
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 26.dp else 22.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BiteyOrange else Color(0x33000000),
                            shape = CircleShape
                        )
                        .clickable { onSelectBackgroundColor(colorHex) }
                )
            }
        }
    }
}
