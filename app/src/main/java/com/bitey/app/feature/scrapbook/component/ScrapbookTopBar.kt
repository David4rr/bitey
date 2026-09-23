package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandHeaderLarge
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio

/**
 * Minimalist top bar for Scrapbook with consistent actions and overflow menu.
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
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Primary Share Action
            IconButton(
                onClick = onShareStory,
                modifier = Modifier.size(36.dp),
                enabled = !isExporting && elementCount > 0
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Story",
                    tint = if (elementCount > 0) BiteyOrange else theme.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Overflow Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More Options",
                        tint = theme.inkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = theme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (aspectRatio == CanvasAspectRatio.STORY_9_16) "Switch to 1:1 Square" else "Switch to 9:16 Story",
                                color = theme.inkPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AspectRatio,
                                contentDescription = null,
                                tint = theme.inkSecondary
                            )
                        },
                        onClick = {
                            onToggleAspectRatio()
                            showMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Save to Gallery", color = if (elementCount > 0) theme.inkPrimary else theme.inkMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = if (elementCount > 0) BiteyOrange else theme.inkMuted
                            )
                        },
                        enabled = !isExporting && elementCount > 0,
                        onClick = {
                            onExportToGallery()
                            showMenu = false
                        }
                    )

                    if (elementCount > 0) {
                        DropdownMenuItem(
                            text = { Text("Clear Canvas", color = Color(0xFFE57373)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373)
                                )
                            },
                            onClick = {
                                showClearDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
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
