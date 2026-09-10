package com.bitey.app.feature.fatetable

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val SliceColors = listOf(
    Color(0xFFFF6B35), // BiteyOrange
    Color(0xFF2EC4B6), // BiteyMint
    Color(0xFFFFBF69), // Warm Amber
    Color(0xFF70C1B3), // Soft Sage
    Color(0xFFFF9F1C), // Deep Tangerine
    Color(0xFF6C5CE7), // Lavender
    Color(0xFFF15BB5), // Soft Pink
    Color(0xFF00BBF9)  // Sky Blue
)

@Composable
fun FateTableScreen(
    onNavigateToNewEntry: () -> Unit = {},
    viewModel: FateTableViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val rotationAnimatable = remember { Animatable(uiState.currentRotationAngle) }
    var lastHapticSliceIndex by remember { mutableIntStateOf(-1) }

    // Haptic feedback tick on slice boundary crossing
    LaunchedEffect(rotationAnimatable.value) {
        if (uiState.candidates.isNotEmpty()) {
            val sliceAngle = 360f / uiState.candidates.size
            val currentSlice = (rotationAnimatable.value / sliceAngle).toInt()
            if (currentSlice != lastHapticSliceIndex) {
                lastHapticSliceIndex = currentSlice
                if (uiState.isSpinning) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Fate's Table",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = BiteyOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${uiState.candidates.size} Options",
                            style = MaterialTheme.typography.labelSmall,
                            color = BiteyOrange
                        )
                    }
                }
                Text(
                    text = "Dining dilemma? Let serendipity choose your bite",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }

            // Candidate shuffle button if pool is large
            if (uiState.candidates.size >= 2) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .neumorphicCard(cornerRadius = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.shuffleCandidates() },
                        enabled = !uiState.isSpinning
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Candidates",
                            tint = if (uiState.isSpinning) InkMuted else InkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FateFilterPill(
                    text = "All Recorded",
                    isSelected = uiState.selectedFilter == FateSourceFilter.ALL && uiState.selectedTagId == null,
                    onClick = {
                        viewModel.selectFilter(FateSourceFilter.ALL)
                        viewModel.selectTag(null)
                    },
                    enabled = !uiState.isSpinning
                )
            }
            item {
                FateFilterPill(
                    text = "Favorites Only",
                    isSelected = uiState.selectedFilter == FateSourceFilter.FAVORITES && uiState.selectedTagId == null,
                    onClick = {
                        viewModel.selectFilter(FateSourceFilter.FAVORITES)
                        viewModel.selectTag(null)
                    },
                    enabled = !uiState.isSpinning
                )
            }
            item {
                FateFilterPill(
                    text = "Recent 30 Days",
                    isSelected = uiState.selectedFilter == FateSourceFilter.RECENT_30_DAYS && uiState.selectedTagId == null,
                    onClick = {
                        viewModel.selectFilter(FateSourceFilter.RECENT_30_DAYS)
                        viewModel.selectTag(null)
                    },
                    enabled = !uiState.isSpinning
                )
            }

            items(uiState.availableTags) { tag ->
                val isSelected = uiState.selectedTagId == tag.tagId
                FateFilterPill(
                    text = tag.tagName,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTag(tag.tagId) },
                    enabled = !uiState.isSpinning
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Content: Wheel or Empty State
        if (uiState.candidates.size < 2) {
            InsufficientCandidatesPrompt(
                totalEntriesCount = uiState.totalEntriesCount,
                onCaptureClick = onNavigateToNewEntry,
                onResetFilters = {
                    viewModel.selectFilter(FateSourceFilter.ALL)
                    viewModel.selectTag(null)
                }
            )
        } else {
            // Spinning Wheel Assembly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Neumorphic bezel ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .neumorphicCard(cornerRadius = 180.dp, elevation = 10.dp)
                )

                // Rotating Wheel Canvas
                WheelCanvas(
                    candidates = uiState.candidates,
                    rotationAngle = rotationAnimatable.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                )

                // Top Pointer Indicator
                WheelPointerIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                )

                // Center tactile hub cap
                CenterHubCap()
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Spin CTA Button
            Button(
                onClick = {
                    val spinResult = viewModel.calculateSpinTarget(uiState.candidates)
                    if (spinResult != null) {
                        viewModel.onSpinStarted()
                        coroutineScope.launch {
                            rotationAnimatable.animateTo(
                                targetValue = spinResult.targetAngle,
                                animationSpec = tween(
                                    durationMillis = 4600,
                                    easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1f)
                                )
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onSpinCompleted(spinResult.targetAngle, spinResult.winningEntry)
                        }
                    }
                },
                enabled = !uiState.isSpinning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange,
                    contentColor = StickerDieCutWhite,
                    disabledContainerColor = BiteyOrange.copy(alpha = 0.6f),
                    disabledContentColor = StickerDieCutWhite.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (uiState.isSpinning) "Serendipity In Motion..." else "SPIN THE TABLE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Winning Dish Dialog
    if (uiState.showWinningDialog && uiState.winningEntry != null) {
        WinningDishDialog(
            entryWithTags = uiState.winningEntry!!,
            onDismiss = { viewModel.dismissWinningDialog() },
            onNavigate = {
                val lat = uiState.winningEntry!!.entry.latitude
                val lng = uiState.winningEntry!!.entry.longitude
                if (lat != null && lng != null) {
                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(uiState.winningEntry!!.entry.title)})")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            }
        )
    }
}

@Composable
private fun WheelCanvas(
    candidates: List<PlateEntryWithTags>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 3.dp.toPx() }
    val textSizePx = with(density) { 13.sp.toPx() }

    val textPaint = remember(textSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, 0x66000000)
        }
    }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val sliceCount = candidates.size
        val sliceAngle = 360f / sliceCount

        rotate(degrees = rotationAngle, pivot = center) {
            for (i in 0 until sliceCount) {
                val startAngle = -90f + (i * sliceAngle)
                val sliceColor = SliceColors[i % SliceColors.size]

                // Draw colored sector
                drawArc(
                    color = sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )

                // Draw radial divider line
                val rad = Math.toRadians(startAngle.toDouble())
                val edgeX = center.x + radius * cos(rad).toFloat()
                val edgeY = center.y + radius * sin(rad).toFloat()
                drawLine(
                    color = StickerDieCutWhite,
                    start = center,
                    end = Offset(edgeX, edgeY),
                    strokeWidth = strokeWidthPx
                )

                // Draw dish title rotated along slice bisector
                val bisectorAngle = startAngle + (sliceAngle / 2f)
                val itemTitle = candidates[i].entry.title
                val displayTitle = if (itemTitle.length > 11) itemTitle.take(10) + "…" else itemTitle

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(bisectorAngle + 90f, center.x, center.y)

                // Position text along the upper radial axis
                val textDistance = radius * 0.65f
                drawContext.canvas.nativeCanvas.drawText(
                    displayTitle,
                    center.x,
                    center.y - textDistance,
                    textPaint
                )

                drawContext.canvas.nativeCanvas.restore()
            }

            // Outer white boundary stroke
            drawCircle(
                color = StickerDieCutWhite,
                radius = radius - (strokeWidthPx / 2f),
                center = center,
                style = Stroke(width = strokeWidthPx)
            )
        }
    }
}

@Composable
private fun WheelPointerIndicator(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp, 40.dp)) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(width / 2f, height) // bottom tip pointing down
            lineTo(width - 4f, 4f)
            lineTo(4f, 4f)
            close()
        }

        // Drop shadow
        drawPath(
            path = path,
            color = Color(0x33000000)
        )

        // White border
        drawPath(
            path = path,
            color = StickerDieCutWhite
        )

        // Inner solid pointer
        val innerPath = Path().apply {
            moveTo(width / 2f, height - 5f)
            lineTo(width - 8f, 7f)
            lineTo(8f, 7f)
            close()
        }
        drawPath(
            path = innerPath,
            color = BiteyOrange
        )
    }
}

@Composable
private fun CenterHubCap() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(SoftBackground)
            .neumorphicCard(cornerRadius = 27.dp, elevation = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BiteyOrange),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StickerDieCutWhite)
            )
        }
    }
}

@Composable
private fun WinningDishDialog(
    entryWithTags: PlateEntryWithTags,
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    val entry = entryWithTags.entry
    val imageFile = File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (entry.latitude != null && entry.longitude != null) {
                Button(
                    onClick = {
                        onNavigate()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate Now")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeumorphicSurface,
                    contentColor = InkPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Spin Again")
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Serendipity Decided!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BiteyOrange,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your next meal has been chosen",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Sticker frame
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .aspectRatio(1f)
                        .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.isStickerMode && entry.stickerImagePath != null) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rating and meal type row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = InkPrimary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "•",
                        color = InkMuted
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = BiteyMint
                    )
                }

                entry.locationName?.let { loc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        containerColor = SoftBackground,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun FateFilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BiteyOrange else NeumorphicSurface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) StickerDieCutWhite else if (enabled) InkSecondary else InkMuted
        )
    }
}

@Composable
private fun InsufficientCandidatesPrompt(
    totalEntriesCount: Int,
    onCaptureClick: () -> Unit,
    onResetFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restaurant,
                    contentDescription = null,
                    tint = BiteyOrange,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (totalEntriesCount < 2) "Wheel of Serendipity" else "Need More Options",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (totalEntriesCount < 2) {
                    "Record at least 2 meals in your journal to unlock Fate's Table and spin for your next meal."
                } else {
                    "The current filter has fewer than 2 candidates. Switch to 'All Recorded' or clear tag filters."
                },
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (totalEntriesCount < 2) {
                Button(
                    onClick = onCaptureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record a Bite")
                }
            } else {
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Filters")
                }
            }
        }
    }
}
