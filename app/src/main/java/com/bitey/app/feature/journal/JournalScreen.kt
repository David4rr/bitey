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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.bitey.app.core.ui.theme.LocalOnThemeModeChanged
import com.bitey.app.core.ui.theme.LocalThemeMode
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.core.ui.theme.ThemeMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onNavigateToNewEntry: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current
    val currentThemeMode = LocalThemeMode.current
    val onThemeModeChanged = LocalOnThemeModeChanged.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Minimalist Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bitey",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BiteyOrange.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${uiState.totalEntriesCount} bites",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BiteyOrange
                        )
                    }
                }
                Text(
                    text = "Your Culinary Journal",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Mode Selector Toggle Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .minimalistCard(cornerRadius = 19.dp, elevation = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        val nextMode = when (currentThemeMode) {
                            ThemeMode.AUTO -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.AUTO
                        }
                        onThemeModeChanged(nextMode)
                    }) {
                        Icon(
                            imageVector = when (currentThemeMode) {
                                ThemeMode.AUTO -> Icons.Rounded.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                                ThemeMode.DARK -> Icons.Rounded.DarkMode
                            },
                            contentDescription = "Theme: ${currentThemeMode.label}",
                            tint = if (currentThemeMode != ThemeMode.AUTO) BiteyOrange else theme.inkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Favorites Filter Toggle Button
                AnimatedFavoriteButton(
                    isFavorite = uiState.isFavoritesOnly,
                    onToggle = { viewModel.toggleFavoritesOnly() },
                    containerSize = 38.dp,
                    iconSize = 18.dp,
                    withNeumorphicContainer = true
                )

                // View Mode Toggle Button (Sticker Card View vs List View)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .minimalistCard(cornerRadius = 19.dp, elevation = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                            contentDescription = if (uiState.isGridView) "Switch to List View" else "Switch to Card View",
                            tint = if (uiState.isGridView) BiteyOrange else theme.inkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Minimalist Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search dishes, places, tags, or notes...",
                        color = theme.inkMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = theme.inkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = theme.inkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BiteyOrange,
                    unfocusedBorderColor = theme.border,
                    focusedContainerColor = theme.surface,
                    unfocusedContainerColor = theme.surface,
                    focusedTextColor = theme.inkPrimary,
                    unfocusedTextColor = theme.inkPrimary
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips (Meal Types & Custom Tags)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(14.dp))

        // Main Content Area: Chronological Vertical List of Minimalist Food Cards
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.entries, key = { it.entry.id }) { item ->
                        StickerCard(
                            item = item,
                            onClick = { viewModel.selectEntryForDetail(item) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.entry) }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
                                    .padding(top = 10.dp, bottom = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BiteyOrange)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = group.dateLabel,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = theme.inkPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${group.entries.size} ${if (group.entries.size == 1) "bite" else "bites"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.inkSecondary
                                    )
                                }
                            }
                        }

                        // Food Entry Cards under this date group
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
            .minimalistCard(cornerRadius = 18.dp, elevation = 1.dp)
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
            .minimalistCard(cornerRadius = 18.dp, elevation = 1.dp)
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

                // Custom & Standard Tags
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item.tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${tag.tagName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.inkSecondary,
                                    fontSize = 10.sp
                                )
                            }
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BiteyOrange else theme.surface)
            .border(
                width = 1.dp,
                color = if (isSelected) BiteyOrange else theme.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) StickerDieCutWhite else theme.inkSecondary
        )
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surfaceVariant)
                                .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "#${tag.tagName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = theme.inkPrimary
                            )
                        }
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
