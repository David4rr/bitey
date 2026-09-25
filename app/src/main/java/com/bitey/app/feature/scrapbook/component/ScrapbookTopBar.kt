package com.bitey.app.feature.scrapbook.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandHeaderLarge
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrapbookTopBar(
    elementCount: Int,
    aspectRatio: CanvasAspectRatio,
    isExporting: Boolean,
    onNavigateBack: (() -> Unit)?,
    onToggleAspectRatio: () -> Unit,
    onClearCanvas: () -> Unit,
    onExportToGallery: () -> Unit,
    onShareStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    var isExpanded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    BackHandler(enabled = isExpanded) { isExpanded = false }

    val animProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ScrapbookAppBarMorph"
    )

    val minWPx = remember(density) { with(density) { 42.dp.roundToPx().toFloat() } }
    val maxWPx = remember(density, elementCount) {
        with(density) { (if (elementCount > 0) 192.dp else 158.dp).roundToPx().toFloat() }
    }
    val buttonHPx = remember(density) { with(density) { 42.dp.toPx() } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.inkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = "Scrapbook",
                style = BrandHeaderLarge,
                color = BiteyOrange
            )
        }

        val cr = (21f - 5f * animProgress.coerceIn(0f, 1f)).dp
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val w = lerp(minWPx, maxWPx, animProgress).roundToInt()
                    val h = buttonHPx.roundToInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h))
                    layout(w, h) { placeable.place(0, 0) }
                }
                .clip(RoundedCornerShape(cr))
                .background(theme.surface)
                .border(
                    width = 1.dp,
                    color = theme.border.copy(alpha = 0.5f + 0.5f * animProgress),
                    shape = RoundedCornerShape(cr)
                )
                .then(
                    if (!isExpanded) {
                        Modifier.combinedClickable(
                            onClick = { isExpanded = true },
                            onLongClick = { if (!isExporting && elementCount > 0) onShareStory() }
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (animProgress < 0.15f && !isExpanded) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Canvas Actions",
                    tint = if (elementCount > 0) BiteyOrange else theme.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer { alpha = ((animProgress - 0.1f) * 1.18f).coerceIn(0f, 1f) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onToggleAspectRatio, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.AspectRatio, contentDescription = "Aspect Ratio", tint = theme.inkSecondary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onExportToGallery, modifier = Modifier.size(32.dp), enabled = !isExporting && elementCount > 0) {
                        Icon(Icons.Rounded.Download, contentDescription = "Save to Gallery", tint = if (elementCount > 0) theme.inkPrimary else theme.inkMuted, modifier = Modifier.size(17.dp))
                    }
                    if (elementCount > 0) {
                        IconButton(onClick = { showClearDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Clear Canvas", tint = Color(0xFFE57373), modifier = Modifier.size(17.dp))
                        }
                    }
                    IconButton(
                        onClick = { isExpanded = false; onShareStory() },
                        modifier = Modifier.size(32.dp),
                        enabled = !isExporting && elementCount > 0
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share Story", tint = if (elementCount > 0) BiteyOrange else theme.inkMuted, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ClearCanvasDialog(
            onConfirm = { onClearCanvas(); showClearDialog = false },
            onDismiss = { showClearDialog = false }
        )
    }
}
