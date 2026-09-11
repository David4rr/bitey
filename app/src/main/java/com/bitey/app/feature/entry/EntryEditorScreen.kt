package com.bitey.app.feature.entry

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.entry.component.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EntryEditorScreen(
    onNavigateBack: () -> Unit,
    onEntrySaved: () -> Unit,
    viewModel: EntryEditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    LaunchedEffect(uiState.isSavedSuccessfully) {
        if (uiState.isSavedSuccessfully) onEntrySaved()
    }

    val calendar = remember(uiState.timestamp) { Calendar.getInstance().apply { timeInMillis = uiState.timestamp } }
    val datePickerDialog = remember(uiState.timestamp) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = uiState.timestamp
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                viewModel.updateTimestamp(updatedCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        EntryEditorHeader(
            isFavorite = uiState.isFavorite,
            onToggleFavorite = { viewModel.toggleFavorite() },
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = uiState.errorMessage != null) {
            uiState.errorMessage?.let { error ->
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFEBEE)).padding(14.dp)) {
                    Text(text = error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        MediaPreviewCard(
            imagePath = uiState.imagePath,
            stickerPath = uiState.stickerPath,
            isStickerMode = uiState.isStickerMode,
            onToggleStickerMode = { viewModel.toggleStickerMode() }
        )

        Spacer(modifier = Modifier.height(20.dp))
        DishTitleInputCard(dishTitle = uiState.dishTitle, onDishTitleChange = { viewModel.updateDishTitle(it) })
        Spacer(modifier = Modifier.height(18.dp))
        DateAttributeCard(timestamp = uiState.timestamp, onClick = { datePickerDialog.show() })
        Spacer(modifier = Modifier.height(18.dp))
        MealTypeSelectorCard(selectedMealType = uiState.mealType, onMealTypeSelected = { viewModel.updateMealType(it) })
        Spacer(modifier = Modifier.height(18.dp))
        RatingAndPriceCard(
            rating = uiState.rating, onRatingChange = { viewModel.updateRating(it) },
            currency = uiState.currency, priceString = uiState.priceString, onPriceChange = { viewModel.updatePrice(it) }
        )
        Spacer(modifier = Modifier.height(18.dp))
        LocationInputCard(
            locationName = uiState.locationName, onLocationNameChange = { viewModel.updateLocationName(it) },
            latitude = uiState.latitude, longitude = uiState.longitude, isLocating = uiState.isLocating,
            onRefreshLocation = { viewModel.requestCurrentLocation() }
        )
        Spacer(modifier = Modifier.height(18.dp))
        PalateNotesCard(notes = uiState.notes, onNotesChange = { viewModel.updateNotes(it) })
        Spacer(modifier = Modifier.height(18.dp))
        TagsSelectionCard(
            availableTags = uiState.availableTags, selectedTags = uiState.selectedTags,
            onToggleTag = { viewModel.toggleTag(it) }, onOpenNewTagDialog = { viewModel.openNewTagDialog() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val formattedDate = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale.US).format(Date(uiState.timestamp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Rounded.CalendarToday, contentDescription = null, tint = theme.inkMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = theme.inkMuted)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveEntry() },
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(24.dp), color = StickerDieCutWhite)
            } else {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Save Journal Entry", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (uiState.isNewTagDialogOpen) {
        NewTagDialog(
            onDismiss = { viewModel.dismissNewTagDialog() },
            onConfirm = { name, category -> viewModel.createAndSelectTag(name, category) }
        )
    }
}
