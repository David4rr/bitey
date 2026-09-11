package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Bottom action dock with "+ Sticker", "+ Accessory" shortcuts and background palette selector.
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 22.dp, elevation = 2.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenFoodStickers,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteyOrange,
                            contentColor = StickerDieCutWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sticker", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onOpenAccessories,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.surfaceVariant,
                            contentColor = theme.inkPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Accessory", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BackgroundPalettes.forEach { colorHex ->
                        val isSelected = currentBackgroundColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BiteyOrange else Color(0x33000000),
                                    shape = CircleShape
                                )
                                .clickable { onSelectBackgroundColor(colorHex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (colorHex == 0xFF263238L) StickerDieCutWhite else BiteyOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
