package com.bitey.app.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.clipToBounds
import com.bitey.app.core.ui.component.MorphingSearchBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.neumorphic.minimalistInset
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onNavigateToNewEntry: () -> Unit = {},
    onNavigateToFootprints: () -> Unit = {},
    onNavigateToFateTable: () -> Unit = {},
    onNavigateToScrapbook: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Minimalist Top App Bar with Shared Morphing Search Bar Component
        MorphingSearchBar(
            isSearchActive = isSearchActive,
            onSearchActiveChange = { isSearchActive = it },
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            titleContent = {
                Text(
                    text = "Bitey",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = BiteyOrange
                )
            }
        )

        // Main Content Area: Left Vertical Rail (Menu-View Mode + 3 Menus) + Main Feed + Tag Filter Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left Vertical Rail: Menu-View Mode + 3 Menus (Footprint, Fates Table, Scrapbook)
                // NO CARDS, pure vertical text directly on canvas
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .padding(start = 10.dp, top = 4.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Menu-View Mode (Toggle between Grid and List)
                    VerticalActionText(
                        text = if (uiState.isGridView) "GRID" else "LIST",
                        icon = if (uiState.isGridView) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                        onClick = { viewModel.setGridView(!uiState.isGridView) },
                        isSelected = true
                    )

                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(1.dp)
                            .background(theme.border)
                    )

                    // 1. Footprint
                    VerticalActionText(
                        text = "FOOTPRINT",
                        icon = Icons.Rounded.Place,
                        onClick = onNavigateToFootprints
                    )

                    // 2. Fates Table
                    VerticalActionText(
                        text = "FATES TABLE",
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = onNavigateToFateTable
                    )

                    // 3. Scrapbook
                    VerticalActionText(
                        text = "SCRAPBOOK",
                        icon = Icons.Rounded.Collections,
                        onClick = onNavigateToScrapbook
                    )
                }

                // Main Feed Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clipToBounds()
                ) {
                    when {
                        uiState.entries.isEmpty() && uiState.totalEntriesCount == 0 -> {
                            EmptyJournalPrompt(onCaptureClick = onNavigateToNewEntry)
                        }
                        uiState.entries.isEmpty() -> {
                            NoSearchResultsPrompt(
                                onClearFilters = {
                                    viewModel.updateSearchQuery("")
                                    viewModel.selectMealType(null)
                                    viewModel.selectTag(null)
                                    if (uiState.isFavoritesOnly) viewModel.toggleFavoritesOnly()
                                }
                            )
                        }
                        uiState.isGridView -> {
                            // History Grid View: Left-Right Swipe with Fade-out
                            // One item per card
                            val pagerState = rememberPagerState(pageCount = { uiState.entries.size })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(start = 8.dp, end = 18.dp, top = 2.dp, bottom = 54.dp),
                                pageSpacing = 14.dp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) { page ->
                                val item = uiState.entries[page]
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                                // Items that slide to the left have a fade-out effect so they don't overlap with the menu
                                val fadeAlpha = if (pageOffset > 0) {
                                    (1f - pageOffset * 1.5f).coerceIn(0f, 1f)
                                } else {
                                    1f
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            alpha = fadeAlpha
                                            if (pageOffset > 0) {
                                                scaleX = (1f - pageOffset * 0.08f).coerceIn(0.92f, 1f)
                                                scaleY = (1f - pageOffset * 0.08f).coerceIn(0.92f, 1f)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    HistoryGridSingleCard(
                                        item = item,
                                        onClick = { viewModel.selectEntryForDetail(item) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item.entry) }
                                    )
                                }
                            }
                        }
                        else -> {
                            // List Mode: Usual layout retaining chronological dates
                            LazyColumn(
                                contentPadding = PaddingValues(start = 6.dp, end = 16.dp, top = 2.dp, bottom = 54.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                uiState.dateGroups.forEach { group ->
                                    // Chronological Date Header
                                    item(key = "header_${group.dateLabel}") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = group.dateLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = theme.inkSecondary,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "${group.entries.size} bites",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = theme.inkMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    items(group.entries, key = { it.entry.id }) { item ->
                                        PlateEntryCard(
                                            item = item,
                                            onClick = { viewModel.selectEntryForDetail(item) },
                                            onToggleFavorite = { viewModel.toggleFavorite(item.entry) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tag Filter: Placed just above the bottom navigation bar, WITHOUT A CARD
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // All Chip
                item {
                    FilterPill(
                        text = "All",
                        isSelected = uiState.selectedMealType == null && uiState.selectedTagId == null,
                        onClick = {
                            viewModel.selectMealType(null)
                            viewModel.selectTag(null)
                        }
                    )
                }

                // Meal Types
                items(MealType.entries.toTypedArray()) { mealType ->
                    val isSelected = uiState.selectedMealType == mealType
                    FilterPill(
                        text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        onClick = {
                            viewModel.selectMealType(if (isSelected) null else mealType)
                        }
                    )
                }

                // Custom & Standard Tags
                items(uiState.availableTags) { tag ->
                    val isSelected = uiState.selectedTagId == tag.tagId
                    FilterPill(
                        text = "#${tag.tagName}",
                        isSelected = isSelected,
                        onClick = { viewModel.selectTag(tag.tagId) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    uiState.selectedEntryForDetail?.let { item ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectEntryForDetail(null) },
            sheetState = sheetState,
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            JournalDetailSheet(
                item = item,
                onClose = { viewModel.selectEntryForDetail(null) },
                onToggleFavorite = { viewModel.toggleFavorite(item.entry) },
                onDelete = {
                    viewModel.deleteEntry(item.entry)
                    viewModel.selectEntryForDetail(null)
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryGridSingleCard(
    item: PlateEntryWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.94f)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Meal Type Badge + Rating + Favorite Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BiteyOrange.copy(alpha = 0.12f))
                            .border(1.dp, BiteyOrange.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entry.mealType.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = BiteyOrange,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (entry.rating != null && entry.rating > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Rating",
                                tint = BiteyOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", entry.rating),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BiteyOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                AnimatedFavoriteButton(
                    isFavorite = entry.isFavorite,
                    onToggle = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Center: Organic Food Sticker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
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

            // Footer: Dish Title + Price + Date + Location + Tags
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    entry.price?.let { price ->
                        if (price > 0.0) {
                            Text(
                                text = String.format(Locale.US, "$%.2f", price),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BiteyOrange
                            )
                        }
                    }
                }

                // Date
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.inkMuted,
                    fontSize = 11.sp
                )

                // Location if present
                entry.locationName?.let { location ->
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = BiteyOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.inkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Tags
                if (item.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        item.tags.take(4).forEach { tag ->
                            Text(
                                text = "#${tag.tagName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BiteyOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalActionText(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) BiteyOrange else BiteyOrange.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Layout(
            content = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        fontSize = 9.sp
                    ),
                    color = if (isSelected) BiteyOrange else theme.inkSecondary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = -90f
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                )
            }
        ) { measurables, _ ->
            val placeable = measurables[0].measure(Constraints())
            layout(placeable.height, placeable.width) {
                placeable.placeRelative(0, placeable.width)
            }
        }
    }
}
/**
 * Minimalist Sticker Card:
 * Highlights the authentic die-cut food sticker front-and-center inside a clean,
 * modern card with crisp 1dp border, dish title, meal badge, rating, and location.
 */
@Composable
private fun StickerCard(
    item: PlateEntryWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 12.dp, elevation = 0.dp)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            // Food Sticker Hero Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
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
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Favorite Heart Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (entry.isFavorite) BiteyOrange else theme.inkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dish Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Meal badge & Rating Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val mealLabel = entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = mealLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.inkSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary
                    )
                }
            }

            entry.locationName?.let { loc ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = theme.inkMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = loc,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Minimalist Food Entry Card:
 * Displays the complete entry details (title, rating, meal type, time, price, tags, location)
 * while showing ONLY the organic sticker image without any square grid/box frame!
 */
@Composable
private fun PlateEntryCard(
    item: PlateEntryWithTags,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val entry = item.entry
    val theme = LocalNeumorphicTheme.current
    val imageFile = remember(entry) {
        File(if (entry.isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 12.dp, elevation = 0.dp)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Food Sticker: ONLY the sticker image, floating with transparent background.
            // NO rectangular box, NO frame, and NO grid around the sticker!
            Box(
                modifier = Modifier
                    .size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isStickerMode && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = entry.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Entry Details
            Column(modifier = Modifier.weight(1f)) {
                // Dish Title
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Meal Type badge & Time
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US).format(Date(entry.timestamp))
                val mealLabel = entry.mealType.name.lowercase().replaceFirstChar { it.uppercase() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mealLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.inkSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeFormat,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rating & Price
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = theme.inkPrimary
                    )

                    entry.price?.let { price ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${entry.currency} ${price.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BiteyMint
                        )
                    }
                }

                // Location if available
                entry.locationName?.let { loc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Custom & Standard Tags (without card)
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item.tags.take(3).forEach { tag ->
                            Text(
                                text = "#${tag.tagName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = BiteyOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Animated Favorite Heart Button
            AnimatedFavoriteButton(
                isFavorite = entry.isFavorite,
                onToggle = onToggleFavorite,
                containerSize = 36.dp,
                iconSize = 20.dp,
                withNeumorphicContainer = false
            )
        }
    }
}

/**
 * Minimalist Filter Pill Chip.
 */
@Composable
private fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp
                ),
                color = if (isSelected) BiteyOrange else theme.inkSecondary
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(BiteyOrange)
                )
            }
        }
    }
}

/**
 * Detail View refactored to clean, user-friendly Minimalist Design.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalDetailSheet(
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
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(theme.inkMuted.copy(alpha = 0.35f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sheet Header with Action Buttons
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
                    // Animated Favorite Button
                    AnimatedFavoriteButton(
                        isFavorite = entry.isFavorite,
                        onToggle = onToggleFavorite,
                        containerSize = 38.dp,
                        iconSize = 20.dp,
                        withNeumorphicContainer = true
                    )

                    // Delete Button
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

                    // Close Button
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

            // Hero Preview Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .minimalistCard(cornerRadius = 20.dp, elevation = 0.dp)
                    .background(theme.surfaceVariant)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentFile = if (isShowingSticker && entry.stickerImagePath != null) {
                    File(entry.stickerImagePath)
                } else {
                    File(entry.fullImagePath)
                }

                if (isShowingSticker && entry.stickerImagePath != null) {
                    AsyncImage(
                        model = currentFile,
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = currentFile,
                        contentDescription = entry.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Toggle between Sticker and Original Photo (if sticker mode available)
                if (entry.stickerImagePath != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surface.copy(alpha = 0.92f))
                            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                            .clickable { isShowingSticker = !isShowingSticker }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isShowingSticker) Icons.Rounded.Layers else Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = BiteyOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isShowingSticker) "View Photo" else "View Sticker",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = theme.inkPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating & Price Strip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimalistCard(cornerRadius = 14.dp, elevation = 0.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (entry.rating >= i) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.rating),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary
                    )
                }

                entry.price?.let { price ->
                    Text(
                        text = "${entry.currency} ${price.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BiteyMint
                    )
                }
            }

            // Location card if present
            entry.locationName?.let { location ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimalistCard(cornerRadius = 14.dp, elevation = 0.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.inkPrimary
                    )
                }
            }

            // Notes / Palate impressions
            if (!entry.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Palate Impressions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimalistInset(cornerRadius = 14.dp)
                        .padding(14.dp)
                ) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.inkPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Tags section
            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tags & Flavors",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.tags.forEach { tag ->
                        Text(
                            text = "#${tag.tagName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BiteyOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete Entry?",
                    color = theme.inkPrimary,
                    fontWeight = FontWeight.Bold
                )
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

@Composable
private fun EmptyJournalPrompt(onCaptureClick: () -> Unit) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 24.dp, elevation = 1.dp)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BiteyOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "No Bites Logged Yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Capture your meal to start creating die-cut stickers and tracking your culinary adventures.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.inkSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCaptureClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = StickerDieCutWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Capture First Bite",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = StickerDieCutWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun NoSearchResultsPrompt(onClearFilters: () -> Unit) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 24.dp, elevation = 1.dp)
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = theme.inkMuted,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "No Matching Bites",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Try adjusting your search terms or meal type filters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reset Filters",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = theme.inkPrimary
                    )
                }
            }
        }
    }
}
