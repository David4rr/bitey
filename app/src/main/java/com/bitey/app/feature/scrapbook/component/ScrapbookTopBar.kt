package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio

/**
 * Top bar containing story canvas title, aspect ratio toggle, clear, export, and social share actions.
 */
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
    var showClearDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
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
            Column {
                Text(
                    text = "Story Canvas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                    color = BiteyOrange
                )
                Text(
                    text = "$elementCount Elements • ${aspectRatio.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Aspect Ratio Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceVariant)
                    .clickable(onClick = onToggleAspectRatio)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (aspectRatio == CanvasAspectRatio.STORY_9_16) "9:16" else "1:1",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.inkPrimary
                )
            }

            if (elementCount > 0) {
                IconButton(onClick = { showClearDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Clear Canvas", tint = theme.inkMuted, modifier = Modifier.size(18.dp))
                }
            }

            IconButton(
                onClick = onExportToGallery,
                modifier = Modifier.size(36.dp),
                enabled = !isExporting && elementCount > 0
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Save to Gallery",
                    tint = if (elementCount > 0) BiteyOrange else theme.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onShareStory,
                modifier = Modifier.size(36.dp),
                enabled = !isExporting && elementCount > 0
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Story",
                    tint = if (elementCount > 0) BiteyMint else theme.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Story Canvas", color = theme.inkPrimary) },
            text = { Text("Remove all stickers and accessories from this canvas?", color = theme.inkSecondary) },
            confirmButton = {
                Button(
                    onClick = { onClearCanvas(); showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) { Text("Clear All") }
            },
            dismissButton = {
                Button(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.inkPrimary)
                ) { Text("Cancel") }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
