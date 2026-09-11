package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalDetailSheet(
    item: PlateEntryWithTags,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isShowingSticker by remember { mutableStateOf(entry.isStickerMode && entry.stickerImagePath != null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(theme.inkMuted.copy(alpha = 0.35f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary
                    )
                    val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(Date(entry.timestamp))
                    Text(
                        text = "$dateStr • ${entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedFavoriteButton(
                        isFavorite = entry.isFavorite,
                        onToggle = onToggleFavorite,
                        containerSize = 38.dp,
                        iconSize = 20.dp,
                        withNeumorphicContainer = true
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .minimalistCard(cornerRadius = 19.dp, elevation = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Entry",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .minimalistCard(cornerRadius = 19.dp, elevation = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = theme.inkSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            JournalDetailContent(
                item = item,
                isShowingSticker = isShowingSticker,
                onToggleSticker = { isShowingSticker = !isShowingSticker }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(text = "Delete Entry?", color = theme.inkPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${entry.title}\"? This action cannot be undone.",
                    color = theme.inkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = StickerDieCutWhite)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = theme.inkPrimary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
