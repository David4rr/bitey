package com.bitey.app.feature.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.TagEntity
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryEditorScreen(
    onNavigateBack: () -> Unit,
    onEntrySaved: () -> Unit,
    viewModel: EntryEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSavedSuccessfully) {
        if (uiState.isSavedSuccessfully) {
            onEntrySaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neumorphicCard(cornerRadius = 21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = InkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Journal Your Bite",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary
                    )
                    Text(
                        text = "Document taste, memory & location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )
                }
            }

            // Favorite Toggle Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .neumorphicCard(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isFavorite) BiteyOrange else InkMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message banner
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            uiState.errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(14.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Hero Preview Frame (Sticker or Photo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewModel = if (uiState.isStickerMode && uiState.stickerPath != null) {
                File(uiState.stickerPath!!)
            } else if (uiState.imagePath.isNotBlank()) {
                File(uiState.imagePath)
            } else null

            if (uiState.isStickerMode && uiState.stickerPath != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Food Sticker",
                    modifier = Modifier
                        .fillMaxSize()
                        .dieCutStickerEffect(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Food Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Mode toggle chip if sticker is present
            if (uiState.stickerPath != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftBackground.copy(alpha = 0.92f))
                        .clickable { viewModel.toggleStickerMode() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isStickerMode) Icons.Rounded.AutoAwesome else Icons.Rounded.Layers,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isStickerMode) "Sticker" else "Photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dish Title Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dish Title",
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.dishTitle,
                    onValueChange = { viewModel.updateDishTitle(it) },
                    placeholder = { Text("e.g. Smoked Wagyu Brisket", color = InkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = NeumorphicSurface,
                        unfocusedContainerColor = NeumorphicSurface,
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Meal Type Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Meal Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { type ->
                        val isSelected = uiState.mealType == type
                        val label = when (type) {
                            MealType.BREAKFAST -> "Breakfast"
                            MealType.LUNCH -> "Lunch"
                            MealType.DINNER -> "Dinner"
                            MealType.SNACK -> "Snack"
                            MealType.LATE_NIGHT -> "Late Night"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BiteyOrange else NeumorphicSurface)
                                .clickable { viewModel.updateMealType(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) StickerDieCutWhite else InkSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Rating & Price Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rating",
                        style = MaterialTheme.typography.titleSmall,
                        color = InkPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f / 5.0", uiState.rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = BiteyOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive 5-star row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..5) {
                        val isStarFilled = uiState.rating >= i.toFloat()
                        IconButton(
                            onClick = { viewModel.updateRating(i.toFloat()) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isStarFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (isStarFilled) BiteyOrange else InkMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Paid,
                        contentDescription = null,
                        tint = BiteyMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Price (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        color = InkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Currency Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeumorphicSurface)
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = uiState.currency,
                            style = MaterialTheme.typography.labelMedium,
                            color = InkPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = uiState.priceString,
                        onValueChange = { viewModel.updatePrice(it) },
                        placeholder = { Text("Amount (e.g. 65000)", color = InkMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiteyOrange,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = NeumorphicSurface,
                            unfocusedContainerColor = NeumorphicSurface,
                            focusedTextColor = InkPrimary,
                            unfocusedTextColor = InkPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Location & Geocoding Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Establishment & Location",
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.requestCurrentLocation() },
                        enabled = !uiState.isLocating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (uiState.isLocating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = BiteyOrange
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "Refresh Location",
                                tint = InkSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.locationName,
                    onValueChange = { viewModel.updateLocationName(it) },
                    placeholder = { Text("Restaurant or street name", color = InkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = NeumorphicSurface,
                        unfocusedContainerColor = NeumorphicSurface,
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                if (uiState.latitude != null && uiState.longitude != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "GPS: %.4f, %.4f", uiState.latitude, uiState.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Palate Impressions & Tasting Notes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Palate Impressions",
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Textures, aroma, dining companions, or memory",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    placeholder = { Text("Crispy skin, rich broth with hint of lime...", color = InkMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = NeumorphicSurface,
                        unfocusedContainerColor = NeumorphicSurface,
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 5
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tags Selection Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 20.dp, elevation = 4.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tag,
                            contentDescription = null,
                            tint = BiteyMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tags & Characteristics",
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                    }

                    TextButton(onClick = { viewModel.openNewTagDialog() }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Tag", color = BiteyOrange, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.availableTags.forEach { tag ->
                        val isSelected = uiState.selectedTags.any { it.tagId == tag.tagId || it.tagName == tag.tagName }
                        TagChip(
                            tag = tag,
                            isSelected = isSelected,
                            onToggle = { viewModel.toggleTag(tag) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timestamp & EXIF info footer
        val formattedDate = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale.US).format(Date(uiState.timestamp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Save Button
        Button(
            onClick = { viewModel.saveEntry() },
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteyOrange,
                contentColor = StickerDieCutWhite
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp),
                    color = StickerDieCutWhite
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Save Journal Entry",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // New Tag Dialog
    if (uiState.isNewTagDialogOpen) {
        NewTagDialog(
            onDismiss = { viewModel.dismissNewTagDialog() },
            onConfirm = { name, category ->
                viewModel.createAndSelectTag(name, category)
            }
        )
    }
}

@Composable
private fun TagChip(
    tag: TagEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BiteyMint else NeumorphicSurface)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = tag.tagName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) StickerDieCutWhite else InkPrimary
            )
        }
    }
}

@Composable
private fun NewTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String?) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Taste") }
    val categories = listOf("Taste", "Ambience", "Diet", "Type")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Tag",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    placeholder = { Text("e.g. Extra Crispy", color = InkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = InkMuted.copy(alpha = 0.3f),
                        focusedContainerColor = NeumorphicSurface,
                        unfocusedContainerColor = NeumorphicSurface,
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val isCatSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCatSelected) BiteyOrange else NeumorphicSurface)
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCatSelected) StickerDieCutWhite else InkSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onConfirm(tagName.trim(), selectedCategory)
                    }
                },
                enabled = tagName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange,
                    contentColor = StickerDieCutWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Tag")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = InkSecondary)
            }
        },
        containerColor = SoftBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
