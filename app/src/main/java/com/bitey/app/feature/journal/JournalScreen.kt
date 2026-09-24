package com.bitey.app.feature.journal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.component.MorphingSearchBar
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandWordmarkStyle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.journal.component.*
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

    Column(modifier = Modifier.fillMaxSize().background(theme.background)) {
        MorphingSearchBar(
            isSearchActive = isSearchActive,
            onSearchActiveChange = { isSearchActive = it },
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            titleContent = {
                Text(text = "Bitey", style = BrandWordmarkStyle, color = BiteyOrange)
            }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(modifier = Modifier.fillMaxSize()) {
                JournalSideRail(
                    isGridView = uiState.isGridView,
                    onToggleGridView = { viewModel.setGridView(!uiState.isGridView) },
                    onNavigateToFootprints = onNavigateToFootprints,
                    onNavigateToFateTable = onNavigateToFateTable,
                    onNavigateToScrapbook = onNavigateToScrapbook
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight().clipToBounds()) {
                    when {
                        uiState.plates.isEmpty() && uiState.totalEntriesCount == 0 -> {
                            EmptyJournalPrompt(onCaptureClick = onNavigateToNewEntry)
                        }
                        uiState.plates.isEmpty() -> {
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
                            val pagerState = rememberPagerState(pageCount = { uiState.plates.size })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 64.dp),
                                pageSpacing = 16.dp,
                                key = { uiState.plates[it].id },
                                modifier = Modifier.fillMaxSize().clipToBounds()
                            ) { page ->
                                val plate = uiState.plates[page]
                                Box(
                                    modifier = Modifier.fillMaxSize().graphicsLayer {
                                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                        alpha = if (pageOffset > 0) (1f - pageOffset * 1.5f).coerceIn(0f, 1f) else 1f
                                        if (pageOffset > 0) {
                                            scaleX = (1f - pageOffset * 0.08f).coerceIn(0.92f, 1f)
                                            scaleY = (1f - pageOffset * 0.08f).coerceIn(0.92f, 1f)
                                        }
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    HistoryGridPlateCard(
                                        plate = plate,
                                        isCurrentPage = pagerState.currentPage == page && uiState.selectedEntryForDetail == null,
                                        onClick = { viewModel.selectPlateForDetail(plate) },
                                        onToggleFavorite = { viewModel.toggleFavorite(plate.primaryEntry) }
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 6.dp, end = 16.dp, top = 2.dp, bottom = 68.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                uiState.dateGroups.forEach { group ->
                                    item(key = "header_${group.dateLabel}") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = group.dateLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = theme.inkSecondary,
                                                letterSpacing = 0.5.sp
                                            )
                                            val totalDishes = group.plates.sumOf { it.entries.size }
                                            Text(
                                                text = "$totalDishes bites${if (group.plates.size > 1) " • ${group.plates.size} plates" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = theme.inkMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    group.plates.forEach { plate ->
                                        if (plate.isMergedPlate) {
                                            item(key = "plate_group_${plate.id}") {
                                                PlateGroupHeader(plate = plate)
                                            }
                                        }
                                        items(plate.entries, key = { it.entry.id }) { dish ->
                                            PlateEntryCard(
                                                item = dish,
                                                onClick = { viewModel.selectPlateForDetail(plate, dish) },
                                                onToggleFavorite = { viewModel.toggleFavorite(dish.entry) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            JournalFilterBar(
                selectedMealType = uiState.selectedMealType,
                onSelectMealType = { viewModel.selectMealType(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    uiState.selectedEntryForDetail?.let { item ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPlateForDetail(null) },
            sheetState = sheetState,
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            JournalDetailSheet(
                item = item,
                plate = uiState.selectedPlateForDetail,
                availableTags = uiState.availableTags,
                onClose = { viewModel.selectPlateForDetail(null) },
                onToggleFavorite = { viewModel.toggleFavorite(item.entry) },
                onDelete = {
                    viewModel.deleteEntry(item.entry)
                    viewModel.selectPlateForDetail(null)
                },
                onSaveEntry = { updated, tags ->
                    viewModel.updateEntry(updated, tags)
                }
            )
        }
    }
}
