package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun HistoryGridSingleCard(
    item: PlateEntryWithTags,
    isCurrentPage: Boolean = true,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val stickers = remember(entry) { entry.getIndividualStickerPaths() }
    val imageFiles = remember(entry, stickers) {
        if (stickers.isNotEmpty() && entry.isStickerMode && entry.stickerImagePath != null) {
            stickers.map { File(it) }
        } else {
            listOf(File(entry.fullImagePath))
        }
    }
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }
    val bookCoverShape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 22.dp, bottomEnd = 22.dp)

    var isGravityEnabled by remember(entry.id) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.88f)
            .widthIn(max = 360.dp)
            .clip(bookCoverShape)
            .background(theme.surface)
            .border(1.dp, theme.border, bookCoverShape)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Book spine & hinge crease effect on the left edge
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                theme.surfaceVariant.copy(alpha = 0.6f),
                                theme.surfaceVariant.copy(alpha = 0.2f),
                                theme.border.copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            // Book Cover Front Face
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BookCoverHeader(
                    entry = entry,
                    isGravityEnabled = isGravityEnabled,
                    onToggleGravity = { isGravityEnabled = !isGravityEnabled },
                    onToggleFavorite = onToggleFavorite
                )

                // Widened Sticker Canvas & Aesthetic Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    theme.surfaceVariant.copy(alpha = 0.5f),
                                    theme.surfaceVariant.copy(alpha = 0.2f),
                                    theme.surface.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = theme.border.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GravitySticker(
                        imageFiles = imageFiles,
                        isStickerMode = entry.isStickerMode && entry.stickerImagePath != null,
                        isGravityEnabled = isGravityEnabled && isCurrentPage,
                        contentDescription = entry.title,
                        onClick = onClick
                    )
                }

                BookCoverBottomInfo(
                    item = item,
                    formattedDate = formattedDate
                )
            }
        }
    }
}
