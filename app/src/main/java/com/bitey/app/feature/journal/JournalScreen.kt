package com.bitey.app.feature.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.LocalAnimatedVisibilityScope
import com.bitey.app.core.ui.LocalSharedTransitionScope
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.journal.component.JournalDetailBottomSheet
import com.bitey.app.feature.journal.component.JournalDetailSheet
import com.bitey.app.feature.journal.component.JournalFeedView
import com.bitey.app.feature.journal.component.StickerPreviewOverlay
import java.io.File

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onNavigateToNewEntry: () -> Unit = {},
    onNavigateToFootprints: () -> Unit = {},
    onNavigateToFateTable: () -> Unit = {},
    onNavigateToScrapbook: () -> Unit = {},
    onJournalDetailVisibilityChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current
    var isSearchActive by remember { mutableStateOf(false) }

    var activeDetailDish by remember { mutableStateOf<PlateEntryWithTags?>(null) }
    var activeDetailPlate by remember { mutableStateOf<JournalPlate?>(null) }

    var previewFile by remember { mutableStateOf<File?>(null) }
    var previewTitle by remember { mutableStateOf("") }
    var previewIsSticker by remember { mutableStateOf(true) }
    var previewDishKey by remember { mutableStateOf("") }
    var stickerSourceBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    LaunchedEffect(uiState.selectedEntryForDetail, uiState.selectedPlateForDetail) {
        if (uiState.selectedEntryForDetail != null) {
            activeDetailDish = uiState.selectedEntryForDetail
            activeDetailPlate = uiState.selectedPlateForDetail
        } else {
            previewFile = null
        }
        onJournalDetailVisibilityChange(uiState.selectedEntryForDetail != null)
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize().background(theme.background)) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = true,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        JournalFeedView(
                            uiState = uiState,
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onSelectMealType = { viewModel.selectMealType(it) },
                            onSelectTag = { viewModel.selectTag(it?.tagId) },
                            onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                            onToggleGridView = { viewModel.setGridView(!uiState.isGridView) },
                            onDishClick = { plate, dish, bounds ->
                                stickerSourceBounds = bounds
                                viewModel.selectPlateForDetail(plate, dish)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onNavigateToNewEntry = onNavigateToNewEntry,
                            onNavigateToFootprints = onNavigateToFootprints,
                            onNavigateToFateTable = onNavigateToFateTable,
                            onNavigateToScrapbook = onNavigateToScrapbook
                        )
                    }
                }

                JournalDetailBottomSheet(
                    visible = uiState.selectedEntryForDetail != null,
                    onDismissRequest = { viewModel.selectPlateForDetail(null) }
                ) {
                    activeDetailDish?.let { selectedDish ->
                        JournalDetailSheet(
                            item = selectedDish,
                            plate = activeDetailPlate,
                            availableTags = uiState.availableTags,
                            initialStickerBounds = stickerSourceBounds,
                            onClose = { viewModel.selectPlateForDetail(null) },
                            onToggleFavorite = { viewModel.toggleFavorite(selectedDish.entry) },
                            onDelete = {
                                viewModel.deleteEntry(selectedDish.entry)
                                viewModel.selectPlateForDetail(null)
                            },
                            onSaveEntry = { updated, tags ->
                                viewModel.updateEntry(updated, tags)
                            },
                            onPreviewSticker = { file, title, isSticker, key ->
                                previewFile = file
                                previewTitle = title
                                previewIsSticker = isSticker
                                previewDishKey = key
                            }
                        )
                    }
                }

                StickerPreviewOverlay(
                    visible = previewFile != null,
                    imageFile = previewFile,
                    title = previewTitle,
                    dishKey = previewDishKey,
                    isSticker = previewIsSticker,
                    onDismiss = { previewFile = null }
                )
            }
        }
    }
}
