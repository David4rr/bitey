package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.bitey.app.feature.journal.JournalPlate
private val plateDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
private val bookCoverShape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 22.dp, bottomEnd = 22.dp)
private val innerCanvasShape = RoundedCornerShape(22.dp)

@Composable
fun HistoryGridPlateCard(
    plate: JournalPlate,
    isCurrentPage: Boolean = true,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val imageFiles = remember(plate.id, plate.entries.size) { plate.allImageFiles }
    val formattedDate = remember(plate.timestamp) { plateDateFormat.format(Date(plate.timestamp)) }
    val spineBrush = remember(theme.surfaceVariant, theme.border) {
        Brush.horizontalGradient(listOf(theme.surfaceVariant.copy(alpha = 0.6f), theme.surfaceVariant.copy(alpha = 0.2f), theme.border.copy(alpha = 0.35f)))
    }
    val radialBrush = remember(theme.surfaceVariant, theme.surface) {
        Brush.radialGradient(colors = listOf(theme.surfaceVariant.copy(alpha = 0.5f), theme.surfaceVariant.copy(alpha = 0.2f), theme.surface.copy(alpha = 0.05f)))
    }

    var isGravityEnabled by remember(plate.id) {
        mutableStateOf(StickerPositionCache.isGravityEnabled(plate.id, default = false))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .widthIn(max = 360.dp)
            .clip(bookCoverShape)
            .background(theme.surface)
            .border(1.dp, theme.border, bookCoverShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Book spine & hinge crease effect on the left edge
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
                    .background(spineBrush)
            )

            // Book Cover Front Face
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 14.dp, top = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BookCoverPlateHeader(
                    plate = plate,
                    isGravityEnabled = isGravityEnabled,
                    onToggleGravity = {
                        val next = !isGravityEnabled
                        isGravityEnabled = next
                        StickerPositionCache.setGravityEnabled(plate.id, next)
                    },
                    onToggleFavorite = onToggleFavorite
                )

                // Widened Sticker Canvas & Aesthetic Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 6.dp)
                        .clip(innerCanvasShape)
                        .background(radialBrush)
                        .border(width = 1.dp, color = theme.border.copy(alpha = 0.4f), shape = innerCanvasShape),
                    contentAlignment = Alignment.Center
                ) {
                    GravitySticker(
                        imageFiles = imageFiles,
                        isStickerMode = plate.isStickerMode,
                        isGravityEnabled = isGravityEnabled && isCurrentPage,
                        contentDescription = plate.title,
                        onClick = onClick
                    )
                }

                BookCoverPlateBottomInfo(
                    plate = plate,
                    formattedDate = formattedDate
                )
            }
        }
    }
}

@Composable
fun HistoryGridSingleCard(
    item: PlateEntryWithTags,
    isCurrentPage: Boolean = true,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plate = remember(item) {
        JournalPlate(
            id = "plate_${item.entry.id}",
            venueName = item.entry.locationName,
            timestamp = item.entry.timestamp,
            entries = listOf(item)
        )
    }
    HistoryGridPlateCard(
        plate = plate,
        isCurrentPage = isCurrentPage,
        onClick = onClick,
        onToggleFavorite = onToggleFavorite,
        modifier = modifier
    )
}
