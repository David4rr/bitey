package com.bitey.app.feature.scrapbook

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.scrapbook.component.AccessoryPickerSheet
import com.bitey.app.feature.scrapbook.component.FoodStickerPickerSheet
import com.bitey.app.feature.scrapbook.component.ScrapbookBottomDock
import com.bitey.app.feature.scrapbook.component.ScrapbookCanvas
import com.bitey.app.feature.scrapbook.component.ScrapbookTopBar
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio

/**
 * Story Canvas Screen: Coordinates top bar actions, gesture-enabled interactive canvas,
 * bottom dock, and sticker picker sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapbookScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ScrapbookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

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
            .background(theme.background)
    ) {
        ScrapbookTopBar(
            elementCount = uiState.elements.size,
            aspectRatio = uiState.aspectRatio,
            isExporting = uiState.isExporting,
            onNavigateBack = onNavigateBack,
            onToggleAspectRatio = {
                val next = if (uiState.aspectRatio == CanvasAspectRatio.STORY_9_16) CanvasAspectRatio.SQUARE_1_1 else CanvasAspectRatio.STORY_9_16
                viewModel.setAspectRatio(next)
            },
            onClearCanvas = { viewModel.clearCanvas() },
            onExportToGallery = {
                viewModel.exportToGallery(viewportWidthPx, viewportHeightPx) { uri ->
                    if (uri != null) Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                }
            },
            onShareStory = {
                viewModel.exportToShare(viewportWidthPx, viewportHeightPx) { file ->
                    if (file != null && file.exists()) {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
            }
        )

        ScrapbookCanvas(
            elements = uiState.elements,
            selectedElementId = uiState.selectedElementId,
            aspectRatio = uiState.aspectRatio,
            backgroundColorHex = uiState.backgroundColorHex,
            isExporting = uiState.isExporting,
            onSelectElement = { viewModel.selectElement(it) },
            onUpdateTransform = { id, px, py, zoom, rot -> viewModel.updateElementTransform(id, px, py, zoom, rot) },
            onBringToFront = { viewModel.bringToFront(it) },
            onSendToBack = { viewModel.sendToBack(it) },
            onDuplicate = { viewModel.duplicateElement(it) },
            onDelete = { viewModel.deleteElement(it) },
            onViewportSizeChanged = { w, h -> viewportWidthPx = w; viewportHeightPx = h },
            modifier = Modifier.weight(1f)
        )

        ScrapbookBottomDock(
            currentBackgroundColorHex = uiState.backgroundColorHex,
            onOpenFoodStickers = { viewModel.openFoodStickerSheet(true) },
            onOpenAccessories = { viewModel.openAccessorySheet(true) },
            onSelectBackgroundColor = { viewModel.setBackgroundColor(it) }
        )
    }

    if (uiState.isFoodStickerSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.openFoodStickerSheet(false) },
            sheetState = sheetState,
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FoodStickerPickerSheet(
                entries = uiState.availableFoodEntries,
                onSelectSticker = { viewModel.addFoodSticker(it) },
                onClose = { viewModel.openFoodStickerSheet(false) }
            )
        }
    }

    if (uiState.isAccessorySheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.openAccessorySheet(false) },
            sheetState = sheetState,
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AccessoryPickerSheet(
                onSelectAccessory = { type, text, sub, col -> viewModel.addAccessory(type, text, sub, col) },
                onClose = { viewModel.openAccessorySheet(false) }
            )
        }
    }
}
