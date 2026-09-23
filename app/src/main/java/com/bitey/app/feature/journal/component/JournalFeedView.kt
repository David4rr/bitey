package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.component.MorphingSearchBar
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandWordmarkStyle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.journal.JournalPlate
import com.bitey.app.feature.journal.JournalUiState

@Composable
fun JournalFeedView(
    uiState: JournalUiState,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectMealType: (MealType?) -> Unit,
    onSelectTag: (TagEntity?) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onToggleGridView: () -> Unit,
    onDishClick: (JournalPlate, PlateEntryWithTags, androidx.compose.ui.geometry.Rect?) -> Unit,
    onToggleFavorite: (PlateEntryEntity) -> Unit,
    onNavigateToNewEntry: () -> Unit,
    onNavigateToFootprints: () -> Unit,
    onNavigateToFateTable: () -> Unit,
    onNavigateToScrapbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Column(modifier = modifier.fillMaxSize().background(theme.background)) {
        MorphingSearchBar(
            isSearchActive = isSearchActive,
            onSearchActiveChange = onSearchActiveChange,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            titleContent = { Text(text = "Bitey", style = BrandWordmarkStyle, color = BiteyOrange) }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(modifier = Modifier.fillMaxSize()) {
                JournalSideRail(
                    isGridView = uiState.isGridView,
                    onToggleGridView = onToggleGridView,
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
                            NoSearchResultsPrompt(onClearFilters = {
                                onSearchQueryChange("")
                                onSelectMealType(null)
                                onSelectTag(null)
                                if (uiState.isFavoritesOnly) onToggleFavoritesOnly()
                            })
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
                                        isCurrentPage = pagerState.currentPage == page,
                                        onClick = { bounds -> plate.entries.firstOrNull()?.let { dish -> onDishClick(plate, dish, bounds) } },
                                        onToggleFavorite = { onToggleFavorite(plate.primaryEntry) }
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
                                                color = theme.inkSecondary, letterSpacing = 0.5.sp
                                            )
                                            val totalDishes = group.plates.sumOf { it.entries.size }
                                            Text(
                                                text = "$totalDishes bites${if (group.plates.size > 1) " • ${group.plates.size} plates" else ""}",
                                                style = MaterialTheme.typography.labelSmall, color = theme.inkMuted, fontSize = 11.sp
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
                                                onClick = { bounds -> onDishClick(plate, dish, bounds) },
                                                onToggleFavorite = { onToggleFavorite(dish.entry) }
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
                onSelectMealType = onSelectMealType,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
