package com.bitey.app.feature.journal.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun JournalDetailSheet(
    item: PlateEntryWithTags,
    plate: com.bitey.app.feature.journal.JournalPlate? = null,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    initialStickerBounds: androidx.compose.ui.geometry.Rect? = null,
    onSaveEntry: ((PlateEntryEntity, List<TagEntity>) -> Unit)? = null,
    onPreviewSticker: ((java.io.File, String, Boolean, String) -> Unit)? = null
) {
    var activeDish by remember(item, plate) { mutableStateOf(item) }
    val entry = activeDish.entry
    val theme = LocalNeumorphicTheme.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var overscrollOffset by remember { mutableFloatStateOf(0f) }
    val pullThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var isArmed by remember { mutableStateOf(false) }
    var springBackJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(showDeleteConfirm) {
        if (!showDeleteConfirm && overscrollOffset > 0f) {
            isArmed = false
            springBackJob?.cancel()
            springBackJob = launch {
                Animatable(overscrollOffset).animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                ) { overscrollOffset = value }
            }
        }
    }

    fun handleDragDelta(deltaY: Float) {
        springBackJob?.cancel()
        val resistance = 0.55f * (1f - (overscrollOffset / (pullThresholdPx * 2f)).coerceIn(0f, 0.65f))
        overscrollOffset = (overscrollOffset - deltaY * resistance).coerceIn(0f, pullThresholdPx * 1.4f)
        isArmed = overscrollOffset >= pullThresholdPx
    }

    fun handleRelease() {
        val reached = isArmed
        isArmed = false
        springBackJob?.cancel()
        springBackJob = coroutineScope.launch {
            if (reached) showDeleteConfirm = true
            Animatable(overscrollOffset).animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) { overscrollOffset = value }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y < 0 && scrollState.value >= scrollState.maxValue - 2) {
                        handleDragDelta(available.y)
                        return Offset(0f, available.y)
                    }
                    if (available.y > 0 && overscrollOffset > 0f) {
                        handleDragDelta(available.y)
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollOffset > 0f) handleRelease()
                return Velocity.Zero
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (overscrollOffset > 0f) handleRelease()
                return Velocity.Zero
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().background(theme.surface).padding(horizontal = 20.dp, vertical = 12.dp).nestedScroll(nestedScrollConnection)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(theme.inkMuted.copy(alpha = 0.35f)))
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Hardware-accelerated GPU draw phase: zero composition and layout overhead on scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .graphicsLayer { translationY = -overscrollOffset * 0.65f }
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                JournalDetailContent(
                    item = item, plate = plate, availableTags = availableTags,
                    isFavorite = activeDish.entry.isFavorite, onToggleFavorite = onToggleFavorite,
                    initialStickerBounds = initialStickerBounds,
                    onActiveDishChange = { activeDish = it }, onClose = onClose,
                    onEntryChange = { updated, tags -> onSaveEntry?.invoke(updated, tags) },
                    onPreviewSticker = onPreviewSticker
                )
            }
        }

        ScrollUpToDeleteZone(
            isArmed = isArmed,
            onDragDelta = { handleDragDelta(it) },
            onDragEnd = { handleRelease() },
            onDragCancel = { handleRelease() },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Delete Entry?", color = theme.inkPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to delete \"${entry.title}\"? This action cannot be undone.", color = theme.inkSecondary) },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), shape = RoundedCornerShape(12.dp)) {
                    Text("Delete", color = StickerDieCutWhite)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                    Text("Cancel", color = theme.inkPrimary)
                }
            },
            containerColor = theme.surface, shape = RoundedCornerShape(20.dp)
        )
    }
}
