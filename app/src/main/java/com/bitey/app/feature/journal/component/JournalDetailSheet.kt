package com.bitey.app.feature.journal.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun JournalDetailSheet(
    item: PlateEntryWithTags,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onSaveEntry: ((PlateEntryEntity, List<TagEntity>) -> Unit)? = null
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var pullUpDistance by remember { mutableFloatStateOf(0f) }
    val pullThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }
    val animatedPullDistance by animateFloatAsState(
        targetValue = pullUpDistance,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PullUpAnimation"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If scrolling up when already at bottom of content
                if (available.y < 0 && scrollState.value >= scrollState.maxValue - 2) {
                    pullUpDistance = (pullUpDistance - available.y * 0.45f).coerceIn(0f, pullThresholdPx * 1.5f)
                    return Offset(0f, available.y)
                }
                if (available.y > 0 && pullUpDistance > 0f) {
                    pullUpDistance = (pullUpDistance - available.y).coerceAtLeast(0f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullUpDistance >= pullThresholdPx) {
                    showDeleteConfirm = true
                }
                pullUpDistance = 0f
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Drag Handle & Close Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(theme.inkMuted.copy(alpha = 0.35f))
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = theme.inkSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Direct in-place editable content with Title aligned with Love button
            JournalDetailContent(
                item = item,
                availableTags = availableTags,
                isFavorite = entry.isFavorite,
                onToggleFavorite = onToggleFavorite,
                onClose = onClose,
                onEntryChange = { updated, tags ->
                    onSaveEntry?.invoke(updated, tags)
                }
            )

            // Interactive "Scroll Up to Delete" Zone
            val isPastThreshold = animatedPullDistance >= pullThresholdPx
            val pullFraction = (animatedPullDistance / pullThresholdPx).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 20.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                if (dragAmount < 0 || pullUpDistance > 0) {
                                    pullUpDistance = (pullUpDistance - dragAmount * 0.65f).coerceIn(0f, pullThresholdPx * 1.5f)
                                    change.consume()
                                }
                            },
                            onDragEnd = {
                                if (pullUpDistance >= pullThresholdPx) {
                                    showDeleteConfirm = true
                                }
                                pullUpDistance = 0f
                            },
                            onDragCancel = {
                                pullUpDistance = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .animateContentSize()
                ) {
                    val iconSize = (16 + (pullFraction * 8)).dp
                    val tintColor = if (isPastThreshold) Color(0xFFEF4444) else theme.inkMuted.copy(alpha = 0.5f + pullFraction * 0.5f)

                    Icon(
                        imageVector = if (isPastThreshold) Icons.Rounded.Delete else Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPastThreshold) "Release to Delete Entry" else "Scroll up to delete",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isPastThreshold) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tintColor
                    )
                }
            }
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
