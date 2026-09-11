package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modal bottom sheet for choosing date stamps, washi tape strips, ratings, and mood chips.
 */
@Composable
fun AccessoryPickerSheet(
    onSelectAccessory: (CanvasElementType, String, String?, Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val dateStr = SimpleDateFormat("EEEE • dd MMM yyyy", Locale.US).format(Date()).uppercase()

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
                text = "Pick Decorative Accessory",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = theme.inkPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().height(400.dp)
        ) {
            item {
                Text(text = "DATE STAMPS", style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = dateStr,
                        subtitle = "EATS OF THE DAY",
                        onClick = { onSelectAccessory(CanvasElementType.DATE_STAMP, dateStr, "EATS OF THE DAY", 0xFFFF6B35) }
                    )
                    AccessoryChip(
                        title = "WEEKEND FEAST",
                        subtitle = "PALATE CHRONICLE",
                        onClick = { onSelectAccessory(CanvasElementType.DATE_STAMP, "WEEKEND FEAST", "PALATE CHRONICLE", 0xFF2EC4B6) }
                    )
                }
            }

            item {
                Text(text = "WASHI TAPE STRIPS", style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "••• BUSTLING STREETS •••",
                        subtitle = null,
                        colorHex = 0xFF2EC4B6,
                        onClick = { onSelectAccessory(CanvasElementType.WASHI_TAPE, "••• BUSTLING STREETS •••", null, 0xFF2EC4B6) }
                    )
                    AccessoryChip(
                        title = "••• SIGNATURE DISH •••",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = { onSelectAccessory(CanvasElementType.WASHI_TAPE, "••• SIGNATURE DISH •••", null, 0xFFFF6B35) }
                    )
                }
            }

            item {
                Text(text = "RATING & VERDICTS", style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "5.0 ★ PALATE APPROVED",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = { onSelectAccessory(CanvasElementType.RATING_BADGE, "5.0 ★ PALATE APPROVED", null, 0xFFFF6B35) }
                    )
                    AccessoryChip(
                        title = "CHEF'S KISS",
                        subtitle = null,
                        colorHex = 0xFF6C5CE7,
                        onClick = { onSelectAccessory(CanvasElementType.RATING_BADGE, "CHEF'S KISS", null, 0xFF6C5CE7) }
                    )
                }
            }

            item {
                Text(text = "MOOD CHIPS", style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "SPICY ADVENTURE",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = { onSelectAccessory(CanvasElementType.MOOD_CHIP, "SPICY ADVENTURE", null, 0xFFFF6B35) }
                    )
                    AccessoryChip(
                        title = "COMFORT FOOD",
                        subtitle = null,
                        colorHex = 0xFF2EC4B6,
                        onClick = { onSelectAccessory(CanvasElementType.MOOD_CHIP, "COMFORT FOOD", null, 0xFF2EC4B6) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccessoryChip(
    title: String,
    subtitle: String?,
    colorHex: Long = 0xFFFF6B35,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .minimalistCard(cornerRadius = 14.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(colorHex)
            )
            subtitle?.let { sub ->
                Text(text = sub, style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary)
            }
        }
    }
}
