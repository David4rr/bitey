package com.bitey.app.feature.scrapbook

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.neumorphicCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.InkMuted
import com.bitey.app.core.ui.theme.InkPrimary
import com.bitey.app.core.ui.theme.InkSecondary
import com.bitey.app.core.ui.theme.NeumorphicSurface
import com.bitey.app.core.ui.theme.SoftBackground
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val BackgroundPalettes = listOf(
    0xFFF4F1EA, // Soft Cream
    0xFFEBF5EE, // Pale Mint
    0xFFFFF3E0, // Warm Peach
    0xFF263238  // Deep Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapbookScreen(
    viewModel: ScrapbookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    var viewportWidthPx by remember { mutableStateOf(1080f) }
    var viewportHeightPx by remember { mutableStateOf(1920f) }

    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearExportMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "Story Canvas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = BiteyOrange
                )
                Text(
                    text = "${uiState.elements.size} Elements • ${uiState.aspectRatio.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Aspect Ratio Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeumorphicSurface)
                        .clickable {
                            val nextRatio = if (uiState.aspectRatio == CanvasAspectRatio.STORY_9_16) {
                                CanvasAspectRatio.SQUARE_1_1
                            } else {
                                CanvasAspectRatio.STORY_9_16
                            }
                            viewModel.setAspectRatio(nextRatio)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (uiState.aspectRatio == CanvasAspectRatio.STORY_9_16) "9:16" else "1:1",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkPrimary
                    )
                }

                // Clear Canvas
                if (uiState.elements.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Clear Canvas",
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Save to Gallery
                IconButton(
                    onClick = {
                        viewModel.exportToGallery(viewportWidthPx, viewportHeightPx) { uri ->
                            if (uri != null) {
                                Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    enabled = !uiState.isExporting && uiState.elements.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Save to Gallery",
                        tint = if (uiState.elements.isNotEmpty()) BiteyOrange else InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share Story
                IconButton(
                    onClick = {
                        viewModel.exportToShare(viewportWidthPx, viewportHeightPx) { file ->
                            if (file != null && file.exists()) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    type = "image/png"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Food Story"))
                            } else {
                                Toast.makeText(context, "Failed to prepare story", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    enabled = !uiState.isExporting && uiState.elements.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share Story",
                        tint = if (uiState.elements.isNotEmpty()) BiteyMint else InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Canvas Viewport Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val aspect = if (uiState.aspectRatio == CanvasAspectRatio.STORY_9_16) 9f / 16f else 1f / 1f

            BoxWithConstraints(
                modifier = Modifier
                    .aspectRatio(aspect)
                    .neumorphicCard(cornerRadius = 24.dp, elevation = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(uiState.backgroundColorHex))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            viewModel.selectElement(null)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                viewportWidthPx = with(density) { maxWidth.toPx() }
                viewportHeightPx = with(density) { maxHeight.toPx() }

                // Subtle Grid Dots Background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 32.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        var y = step
                        while (y < size.height) {
                            drawCircle(
                                color = if (uiState.backgroundColorHex == 0xFF263238L) Color(0x22FFFFFF) else Color(0x18000000),
                                radius = 1.2f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            y += step
                        }
                        x += step
                    }
                }

                // Empty canvas prompt
                if (uiState.elements.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Blank Story Canvas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (uiState.backgroundColorHex == 0xFF263238L) StickerDieCutWhite else InkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Food Sticker' or '+ Accessory' below to craft your scrapbook.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.backgroundColorHex == 0xFF263238L) InkMuted else InkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Render Canvas Elements in Z-order
                val sortedElements = uiState.elements.sortedBy { it.zIndex }
                sortedElements.forEach { element ->
                    val isSelected = element.id == uiState.selectedElementId

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(element.xOffset.roundToInt(), element.yOffset.roundToInt()) }
                            .rotate(element.rotation)
                            .scale(element.scale)
                            .pointerInput(element.id) {
                                detectTapGestures {
                                    viewModel.selectElement(element.id)
                                }
                            }
                            .pointerInput(element.id) {
                                detectTransformGestures { _, pan, zoom, rot ->
                                    viewModel.updateElementTransform(
                                        id = element.id,
                                        panX = pan.x,
                                        panY = pan.y,
                                        zoom = zoom,
                                        rotationDelta = rot
                                    )
                                }
                            }
                    ) {
                        // Element Content View
                        CanvasElementItem(element = element)

                        // Selection dashed bounding box & quick layer controls
                        if (isSelected) {
                            SelectionOverlay(
                                onFront = { viewModel.bringToFront(element.id) },
                                onBack = { viewModel.sendToBack(element.id) },
                                onDuplicate = { viewModel.duplicateElement(element.id) },
                                onDelete = { viewModel.deleteElement(element.id) }
                            )
                        }
                    }
                }

                // Loading overlay when exporting
                if (uiState.isExporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BiteyOrange)
                    }
                }
            }
        }

        // Bottom Action Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicCard(cornerRadius = 22.dp, elevation = 6.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.openFoodStickerSheet(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BiteyOrange,
                                contentColor = StickerDieCutWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sticker",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = { viewModel.openAccessorySheet(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeumorphicSurface,
                                contentColor = InkPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Accessory",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    // Background Palette Swatches
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BackgroundPalettes.forEach { colorHex ->
                            val isSelected = uiState.backgroundColorHex == colorHex
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
                                    .clickable { viewModel.setBackgroundColor(colorHex) },
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

    // Food Sticker Bottom Sheet
    if (uiState.isFoodStickerSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.openFoodStickerSheet(false) },
            sheetState = sheetState,
            containerColor = SoftBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FoodStickerPickerSheet(
                entries = uiState.availableFoodEntries,
                onSelectSticker = { viewModel.addFoodSticker(it) },
                onClose = { viewModel.openFoodStickerSheet(false) }
            )
        }
    }

    // Accessory Bottom Sheet
    if (uiState.isAccessorySheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.openAccessorySheet(false) },
            sheetState = sheetState,
            containerColor = SoftBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AccessoryPickerSheet(
                onSelectAccessory = { type, text, subtitle, color ->
                    viewModel.addAccessory(type, text, subtitle, color)
                },
                onClose = { viewModel.openAccessorySheet(false) }
            )
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Story Canvas", color = InkPrimary) },
            text = { Text("Remove all stickers and accessories from this canvas?", color = InkSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCanvas()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeumorphicSurface, contentColor = InkPrimary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = SoftBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun CanvasElementItem(element: CanvasElement) {
    when (element.type) {
        CanvasElementType.FOOD_STICKER -> {
            val file = element.imagePath?.let { File(it) }
            if (file != null && file.exists()) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = file,
                        contentDescription = element.text,
                        modifier = Modifier
                            .fillMaxSize()
                            .dieCutStickerEffect(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        CanvasElementType.DATE_STAMP -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = element.text ?: "TODAY",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(element.primaryColorHex)
                    )
                    element.subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(element.primaryColorHex)
                        )
                    }
                }
            }
        }
        CanvasElementType.WASHI_TAPE -> {
            Box(
                modifier = Modifier
                    .background(Color(element.primaryColorHex).copy(alpha = 0.85f))
                    .border(1.dp, Color(0x33FFFFFF))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = element.text ?: "",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StickerDieCutWhite
                )
            }
        }
        CanvasElementType.LOCATION_TAG -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(StickerDieCutWhite)
                    .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = element.text ?: "Location",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
            }
        }
        CanvasElementType.RATING_BADGE -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(element.primaryColorHex))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = element.text ?: "5.0 ★",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StickerDieCutWhite
                )
            }
        }
        CanvasElementType.MOOD_CHIP -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StickerDieCutWhite)
                    .border(1.5.dp, Color(element.primaryColorHex), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = element.text ?: "CHEF'S KISS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(element.primaryColorHex)
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.SelectionOverlay(
    onFront: () -> Unit,
    onBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .border(
                width = 2.dp,
                color = BiteyOrange,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Floating action toolbar directly above the element
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xDD2B2120))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFront, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Front", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = "Back", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Duplicate", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun FoodStickerPickerSheet(
    entries: List<PlateEntryWithTags>,
    onSelectSticker: (PlateEntryWithTags) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Pick Food Sticker",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = InkSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bites recorded yet. Record a meal in the journal to unlock stickers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                items(entries) { item ->
                    val path = item.entry.stickerImagePath ?: item.entry.fullImagePath
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .neumorphicCard(cornerRadius = 16.dp, elevation = 4.dp)
                            .clickable { onSelectSticker(item) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = item.entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessoryPickerSheet(
    onSelectAccessory: (CanvasElementType, String, String?, Long) -> Unit,
    onClose: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE • dd MMM yyyy", Locale.US).format(Date()).uppercase()

    Column(
        modifier = Modifier
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
                color = InkPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = InkSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            // Date Stamps
            item {
                Text(
                    text = "DATE STAMPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = dateStr,
                        subtitle = "EATS OF THE DAY",
                        onClick = {
                            onSelectAccessory(CanvasElementType.DATE_STAMP, dateStr, "EATS OF THE DAY", 0xFFFF6B35)
                        }
                    )
                    AccessoryChip(
                        title = "WEEKEND FEAST",
                        subtitle = "PALATE CHRONICLE",
                        onClick = {
                            onSelectAccessory(CanvasElementType.DATE_STAMP, "WEEKEND FEAST", "PALATE CHRONICLE", 0xFF2EC4B6)
                        }
                    )
                }
            }

            // Washi Tapes
            item {
                Text(
                    text = "WASHI TAPE STRIPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "••• BUSTLING STREETS •••",
                        subtitle = null,
                        colorHex = 0xFF2EC4B6,
                        onClick = {
                            onSelectAccessory(CanvasElementType.WASHI_TAPE, "••• BUSTLING STREETS •••", null, 0xFF2EC4B6)
                        }
                    )
                    AccessoryChip(
                        title = "••• SIGNATURE DISH •••",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = {
                            onSelectAccessory(CanvasElementType.WASHI_TAPE, "••• SIGNATURE DISH •••", null, 0xFFFF6B35)
                        }
                    )
                }
            }

            // Rating Badges
            item {
                Text(
                    text = "RATING & VERDICTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "5.0 ★ PALATE APPROVED",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = {
                            onSelectAccessory(CanvasElementType.RATING_BADGE, "5.0 ★ PALATE APPROVED", null, 0xFFFF6B35)
                        }
                    )
                    AccessoryChip(
                        title = "CHEF'S KISS",
                        subtitle = null,
                        colorHex = 0xFF6C5CE7,
                        onClick = {
                            onSelectAccessory(CanvasElementType.RATING_BADGE, "CHEF'S KISS", null, 0xFF6C5CE7)
                        }
                    )
                }
            }

            // Mood Chips
            item {
                Text(
                    text = "MOOD CHIPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryChip(
                        title = "SPICY ADVENTURE",
                        subtitle = null,
                        colorHex = 0xFFFF6B35,
                        onClick = {
                            onSelectAccessory(CanvasElementType.MOOD_CHIP, "SPICY ADVENTURE", null, 0xFFFF6B35)
                        }
                    )
                    AccessoryChip(
                        title = "COMFORT FOOD",
                        subtitle = null,
                        colorHex = 0xFF2EC4B6,
                        onClick = {
                            onSelectAccessory(CanvasElementType.MOOD_CHIP, "COMFORT FOOD", null, 0xFF2EC4B6)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessoryChip(
    title: String,
    subtitle: String?,
    colorHex: Long = 0xFFFF6B35,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .neumorphicCard(cornerRadius = 14.dp, elevation = 4.dp)
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
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
            }
        }
    }
}
