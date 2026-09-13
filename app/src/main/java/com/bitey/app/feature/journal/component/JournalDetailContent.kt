package com.bitey.app.feature.journal.component

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.*
import com.bitey.app.feature.entry.component.NewTagDialog
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalDetailContent(
    item: PlateEntryWithTags,
    availableTags: List<TagEntity>,
    onEntryChange: (PlateEntryEntity, List<TagEntity>) -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = item.entry.isFavorite,
    onToggleFavorite: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val theme = LocalNeumorphicTheme.current
    val entry = item.entry

    var title by remember(entry.id) { mutableStateOf(entry.title) }
    var tempTitle by remember(entry.id) { mutableStateOf(entry.title) }
    var editingTitle by remember { mutableStateOf(false) }

    var note by remember(entry.id) { mutableStateOf(entry.note ?: "") }

    var rating by remember(entry.id) { mutableFloatStateOf(entry.rating) }

    var priceString by remember(entry.id) {
        mutableStateOf(entry.price?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var tempPriceString by remember(entry.id) { mutableStateOf(priceString) }
    var editingPrice by remember { mutableStateOf(false) }

    var locationName by remember(entry.id) { mutableStateOf(entry.locationName ?: "") }
    var tempLocationName by remember(entry.id) { mutableStateOf(entry.locationName ?: "") }
    var editingLocation by remember { mutableStateOf(false) }

    var mealType by remember(entry.id) { mutableStateOf(entry.mealType) }

    var timestamp by remember(entry.id) { mutableLongStateOf(entry.timestamp) }
    var isStickerMode by remember(entry.id, entry.isStickerMode) { mutableStateOf(entry.isStickerMode) }

    var currentAvailableTags by remember(availableTags) { mutableStateOf(availableTags) }
    var selectedTags by remember(entry.id, item.tags) { mutableStateOf(item.tags) }
    var editingTags by remember { mutableStateOf(false) }

    var isNewTagDialogOpen by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    val calendar = remember(timestamp) { Calendar.getInstance().apply { timeInMillis = timestamp } }
    val datePickerDialog = remember(timestamp) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                timestamp = updatedCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    var isFavoriteState by remember(entry.id, isFavorite) { mutableStateOf(isFavorite) }
    LaunchedEffect(isFavorite) {
        isFavoriteState = isFavorite
    }

    LaunchedEffect(title, note, rating, priceString, locationName, mealType, timestamp, isStickerMode, selectedTags, isFavoriteState) {
        delay(300L)
        val parsedPrice = priceString.toDoubleOrNull()
        val updated = entry.copy(
            title = title.trim().ifEmpty { entry.title },
            note = note.trim().ifEmpty { null },
            rating = rating,
            price = parsedPrice,
            locationName = locationName.trim().ifEmpty { null },
            mealType = mealType,
            timestamp = timestamp,
            isStickerMode = isStickerMode,
            isFavorite = isFavoriteState
        )
        if (updated != entry || selectedTags != item.tags) {
            onEntryChange(updated, selectedTags)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hero Row: Dish Info (Title + Love Button, Stars, Category, Price & Place) on Left, Pure Food Sticker on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Title + Love Button, Rating, Category & Date, Price & Place Name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dish Title with Direct Inline Cardless Typography Edit + Love Button directly beside it
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = editingTitle,
                        transitionSpec = {
                            (fadeIn(tween(150)) + expandVertically(tween(180)))
                                .togetherWith(fadeOut(tween(120)) + shrinkVertically(tween(180)))
                        },
                        label = "TitleMorph",
                        modifier = Modifier.weight(1f, fill = false)
                    ) { isEditing ->
                        if (isEditing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = tempTitle,
                                    onValueChange = { tempTitle = it },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = theme.inkPrimary
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(BiteyOrange),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                                        title = tempTitle.trim().ifEmpty { entry.title }
                                        editingTitle = false
                                    }),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (tempTitle.isEmpty()) {
                                            Text(
                                                text = "Dish Title",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = theme.inkMuted
                                            )
                                        }
                                        inner()
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        title = tempTitle.trim().ifEmpty { entry.title }
                                        editingTitle = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Save Title",
                                        tint = BiteyOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    tempTitle = title
                                    editingTitle = true
                                }
                            ) {
                                Text(
                                    text = if (title.isNotBlank()) title else "Untitled Dish",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (title.isNotBlank()) theme.inkPrimary else theme.inkMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit Title",
                                    tint = theme.inkMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    if (onToggleFavorite != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedFavoriteButton(
                            isFavorite = isFavoriteState,
                            onToggle = {
                                isFavoriteState = !isFavoriteState
                                onToggleFavorite()
                            },
                            containerSize = 30.dp,
                            iconSize = 20.dp,
                            withContainer = false
                        )
                    }
                }
                // Interactive 5-Star Rating (Cardless: Touch stars to rate directly)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    for (i in 1..5) {
                        val isFilled = rating >= i.toFloat()
                        Icon(
                            imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Rate $i",
                            tint = if (isFilled) BiteyWarmYellow else theme.inkMuted.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { rating = i.toFloat() }
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", rating),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = theme.inkPrimary
                    )
                }

                // Native Calendar Date (Cardless, meal type deleted)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { datePickerDialog.show() }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Change Date",
                        tint = theme.inkSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date(timestamp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = theme.inkSecondary,
                        fontSize = 12.sp
                    )
                }

                // Price Item (Cardless)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tempPriceString = priceString
                            editingPrice = true
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Paid,
                        contentDescription = null,
                        tint = BiteyMint,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (editingPrice) {
                        Text(
                            text = "${entry.currency} ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = BiteyMint
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = tempPriceString,
                            onValueChange = { tempPriceString = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = theme.inkPrimary
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(BiteyMint),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                                priceString = tempPriceString.trim()
                                editingPrice = false
                            }),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (tempPriceString.isEmpty()) {
                                    Text("0", style = MaterialTheme.typography.bodyMedium, color = theme.inkMuted)
                                }
                                inner()
                            }
                        )
                        IconButton(
                            onClick = {
                                priceString = tempPriceString.trim()
                                editingPrice = false
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = "Done", tint = BiteyMint, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        val displayPrice = priceString.toDoubleOrNull()
                        Text(
                            text = if (displayPrice != null && displayPrice > 0) "${entry.currency} ${displayPrice.toInt()}" else "Add price",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (displayPrice != null && displayPrice > 0) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (displayPrice != null && displayPrice > 0) BiteyMint else theme.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Price",
                            tint = theme.inkMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Place Name Item (Cardless)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tempLocationName = locationName
                            editingLocation = true
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (editingLocation) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = tempLocationName,
                            onValueChange = { tempLocationName = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = theme.inkPrimary),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(BiteyOrange),
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                                locationName = tempLocationName.trim()
                                editingLocation = false
                            }),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (tempLocationName.isEmpty()) {
                                    Text("Place name", style = MaterialTheme.typography.bodyMedium, color = theme.inkMuted)
                                }
                                inner()
                            }
                        )
                        IconButton(
                            onClick = {
                                locationName = tempLocationName.trim()
                                editingLocation = false
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = "Done", tint = BiteyOrange, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Text(
                            text = if (locationName.isNotBlank()) locationName else "Place name",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (locationName.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                            ),
                            color = if (locationName.isNotBlank()) theme.inkPrimary else theme.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Place Name",
                            tint = theme.inkMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // Right: Pure Prominent Sticker in its Aesthetic Zone
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                theme.surfaceVariant.copy(alpha = 0.45f),
                                theme.surfaceVariant.copy(alpha = 0.15f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = theme.border.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showPreview = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val displayFile = File(
                    if (isStickerMode && entry.stickerImagePath != null) entry.stickerImagePath else entry.fullImagePath
                )

                val imageRequest = remember(displayFile, isStickerMode) {
                    ImageRequest.Builder(context)
                        .data(displayFile)
                        .apply {
                            if (isStickerMode && entry.stickerImagePath != null) {
                                transformations(CropTransparentTransformation())
                            }
                        }
                        .crossfade(true)
                        .build()
                }

                AsyncImage(
                    model = imageRequest,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isStickerMode && entry.stickerImagePath != null) {
                                Modifier.dieCutStickerEffect()
                            } else {
                                Modifier.clip(RoundedCornerShape(18.dp))
                            }
                        ),
                    contentScale = if (isStickerMode && entry.stickerImagePath != null) ContentScale.Fit else ContentScale.Crop
                )
            }
        }

        // Tasting Notes: Seamless Cardless Journal Notepad (Directly on sheet surface)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Notes,
                    contentDescription = null,
                    tint = BiteyOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tasting Notes",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkSecondary
                )
            }

            androidx.compose.foundation.text.BasicTextField(
                value = note,
                onValueChange = { note = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = theme.inkPrimary,
                    lineHeight = 20.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(BiteyOrange),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                decorationBox = { innerTextField ->
                    if (note.isEmpty()) {
                        Text(
                            text = "Flavors, aroma, texture, impressions...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.inkMuted.copy(alpha = 0.65f)
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Tags Section: Cardless Flat Hashtags (No drawer box, clean flat tokens)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selected Tag Chips (Cardless: flat text with remove icon)
                selectedTags.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                selectedTags = selectedTags.filterNot { it.tagName.equals(tag.tagName, ignoreCase = true) }
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${tag.tagName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BiteyOrange,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove",
                            tint = BiteyOrange.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Add / Toggle Tag Selector (Cardless text link)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { editingTags = !editingTags }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (editingTags) Icons.Rounded.ExpandLess else Icons.Rounded.Add,
                        contentDescription = null,
                        tint = theme.inkSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (selectedTags.isEmpty()) "Add tags" else "Tag",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = theme.inkSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Available Tags: Cardless flat selection
            AnimatedVisibility(
                visible = editingTags,
                enter = fadeIn(tween(150)) + expandVertically(tween(180)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(180))
            ) {
                val allTags = (currentAvailableTags + selectedTags).distinctBy { it.tagName.lowercase() }
                val unselectedTags = allTags.filterNot { avail ->
                    selectedTags.any { it.tagName.equals(avail.tagName, ignoreCase = true) }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    unselectedTags.forEach { tag ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    selectedTags = selectedTags + tag
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = theme.inkMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = tag.tagName,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.inkSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Create New Custom Tag Option (Cardless)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isNewTagDialogOpen = true }
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = BiteyOrange,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "New tag...",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BiteyOrange,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showPreview) {
        val stickerFile = entry.stickerImagePath?.let { File(it) } ?: File(entry.fullImagePath)
        val fullPhotoFile = File(entry.fullImagePath)
        StickerPreviewDialog(
            imageFile = stickerFile,
            fullPhotoFile = fullPhotoFile,
            title = title,
            initialIsSticker = isStickerMode && entry.stickerImagePath != null,
            onDismiss = { showPreview = false }
        )
    }

    if (isNewTagDialogOpen) {
        NewTagDialog(
            onDismiss = { isNewTagDialogOpen = false },
            onConfirm = { name, category ->
                val newTag = TagEntity(tagName = name, category = category)
                if (currentAvailableTags.none { it.tagName.equals(name, ignoreCase = true) }) {
                    currentAvailableTags = currentAvailableTags + newTag
                }
                selectedTags = selectedTags + newTag
                isNewTagDialogOpen = false
            }
        )
    }
}
