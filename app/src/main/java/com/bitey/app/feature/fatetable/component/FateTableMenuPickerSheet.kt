package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FateTableMenuPickerSheet(
    allEntries: List<PlateEntryWithTags>,
    selectedCandidates: List<PlateEntryWithTags>,
    onToggleEntry: (PlateEntryWithTags) -> Unit,
    onResetToAuto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember(selectedCandidates) { selectedCandidates.map { it.entry.id }.toSet() }

    val filteredEntries = remember(allEntries, searchQuery) {
        if (searchQuery.isBlank()) allEntries
        else {
            val q = searchQuery.trim().lowercase()
            allEntries.filter { item ->
                item.entry.title.lowercase().contains(q) ||
                    item.entry.mealType.label.lowercase().contains(q) ||
                    (item.entry.locationName?.lowercase()?.contains(q) == true) ||
                    item.tags.any { it.tagName.lowercase().contains(q) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Dishes on Menu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                    Text("${selectedCandidates.size}/12 dishes on wheel", style = MaterialTheme.typography.bodySmall, color = if (selectedCandidates.size < 2) BiteyOrange else theme.inkSecondary)
                }
                TextButton(onClick = onResetToAuto) {
                    Text("Auto Recommend", style = MaterialTheme.typography.labelSmall, color = BiteyOrange)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search dishes on your menu...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = theme.inkSecondary) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = theme.inkSecondary) } }
                } else null,
                singleLine = true, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BiteyOrange, unfocusedBorderColor = theme.border)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (allEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("No dishes on your menu yet.\nLog your meals to add them here!", style = MaterialTheme.typography.bodyMedium, color = theme.inkSecondary, textAlign = TextAlign.Center)
                }
            } else if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("No matching dishes found on menu.", style = MaterialTheme.typography.bodyMedium, color = theme.inkSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(filteredEntries, key = { it.entry.id }) { item ->
                        val isChecked = selectedIds.contains(item.entry.id)
                        val stickerPath = item.entry.stickerImagePath?.takeIf { File(it).exists() } ?: item.entry.fullImagePath.takeIf { File(it).exists() }
                        val isStk = item.entry.isStickerMode && stickerPath == item.entry.stickerImagePath
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).minimalistCard(cornerRadius = 14.dp)
                                .clickable { onToggleEntry(item) }.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                                if (stickerPath != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(File(stickerPath)).apply { if (isStk) transformations(CropTransparentTransformation()) }.crossfade(true).build(),
                                        contentDescription = item.entry.title,
                                        modifier = Modifier.fillMaxSize().then(if (isStk) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(8.dp))),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.entry.title.ifBlank { item.entry.mealType.label }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = theme.inkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                item.entry.locationName?.takeIf { it.isNotBlank() }?.let { loc ->
                                    Text(text = loc, style = MaterialTheme.typography.labelSmall, color = theme.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Checkbox(checked = isChecked, onCheckedChange = { onToggleEntry(item) }, colors = CheckboxDefaults.colors(checkedColor = BiteyOrange, checkmarkColor = StickerDieCutWhite))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Confirm (${selectedCandidates.size} on Wheel)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = StickerDieCutWhite)
            }
        }
    }
}
