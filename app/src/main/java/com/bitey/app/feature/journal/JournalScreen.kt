package com.bitey.app.feature.journal

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.component.MorphingSearchBar
import com.bitey.app.core.ui.theme.BiteyOrange
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
                Text(text = "Bitey", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = BiteyOrange)
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
                            val pagerState = rememberPagerState(pageCount = { uiState.entries.size })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(start = 8.dp, end = 18.dp, top = 2.dp, bottom = 54.dp),
                                pageSpacing = 14.dp,
                                modifier = Modifier.fillMaxSize().clipToBounds()
                            ) { page ->
                                val item = uiState.entries[page]
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                val fadeAlpha = if (pageOffset > 0) (1f - pageOffset * 1.5f).coerceIn(0f, 1f) else 1f

                                Box(
                                    modifier = Modifier.fillMaxSize().graphicsLayer {
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
                            LazyColumn(
                                contentPadding = PaddingValues(start = 6.dp, end = 16.dp, top = 2.dp, bottom = 54.dp),
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
                                            Text(text = "${group.entries.size} bites", style = MaterialTheme.typography.labelSmall, color = theme.inkMuted, fontSize = 11.sp)
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

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 6.dp)
            ) {
                item {
                    FilterPill(
                        text = "All",
                        isSelected = uiState.selectedMealType == null && uiState.selectedTagId == null,
                        onClick = { viewModel.selectMealType(null); viewModel.selectTag(null) }
                    )
                }
                items(MealType.entries, key = { it.name }) { mealType ->
                    val isSelected = uiState.selectedMealType == mealType
                    FilterPill(
                        text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        onClick = { viewModel.selectMealType(if (isSelected) null else mealType) }
                    )
                }
                items(uiState.availableTags, key = { it.tagId }) { tag ->
                    FilterPill(text = "#${tag.tagName}", isSelected = uiState.selectedTagId == tag.tagId, onClick = { viewModel.selectTag(tag.tagId) })
                }
            }
        }
    }

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
