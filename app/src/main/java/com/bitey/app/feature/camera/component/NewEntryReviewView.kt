package com.bitey.app.feature.camera.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandHeaderMedium
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CandidateStickerItem
import kotlin.math.roundToInt

@Composable
fun NewEntryReviewView(
    name: String = "", onNameChange: (String) -> Unit = {},
    mealType: MealType = MealType.FOOD, onMealTypeChange: (MealType) -> Unit = {},
    candidates: List<CandidateStickerItem>, onToggleCandidate: (String) -> Unit,
    onUpdateCandidateName: (id: String, name: String) -> Unit = { _, _ -> },
    onUpdateCandidateMealType: (id: String, mealType: MealType) -> Unit = { _, _ -> },
    onCutItMyselfClick: () -> Unit = {}, onClose: () -> Unit = {},
    onSaveAndClose: () -> Unit, modifier: Modifier = Modifier,
    isStickerMode: Boolean = true, onStickerModeChange: (Boolean) -> Unit = {}
) {
    val theme = LocalNeumorphicTheme.current
    val isMultiDish = candidates.size > 1
    val selectedCandidates = candidates.filter { it.isSelected }
    val canSave = if (isMultiDish) selectedCandidates.isNotEmpty() && selectedCandidates.all { it.label.trim().isNotBlank() } else name.trim().isNotBlank()
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.fillMaxSize().offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) }
            .draggable(
                state = rememberDraggableState { delta -> dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f) },
                orientation = Orientation.Vertical,
                onDragStopped = { velocity -> if (dragOffsetY > 120f || velocity > 400f) onClose() else dragOffsetY = 0f }
            )
            .background(theme.background).padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(CircleShape).background(theme.inkSecondary.copy(alpha = 0.2f)))
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (isMultiDish) "Review Dishes (${selectedCandidates.size}/${candidates.size})" else "Review Dish ✨", style = BrandHeaderMedium, color = theme.inkPrimary)
            Button(
                onClick = onSaveAndClose, enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange, contentColor = StickerDieCutWhite,
                    disabledContainerColor = BiteyOrange.copy(alpha = 0.35f), disabledContentColor = StickerDieCutWhite.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Save", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(theme.surfaceVariant).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                listOf(true to "Sticker Mode", false to "Full Photo").forEach { (isSticker, label) ->
                    val active = isStickerMode == isSticker
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (active) BiteyOrange else Color.Transparent)
                            .clickable { onStickerModeChange(isSticker) }.padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.Medium), color = if (active) StickerDieCutWhite else theme.inkSecondary)
                    }
                }
            }

            if (isStickerMode && !isMultiDish) {
                OutlinedButton(
                    onClick = onCutItMyselfClick, shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), border = BorderStroke(1.dp, BiteyOrange.copy(alpha = 0.35f))
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Adjust Cut", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = BiteyOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isMultiDish) {
            BasicTextField(
                value = name, onValueChange = onNameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = theme.inkPrimary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                cursorBrush = SolidColor(BiteyOrange),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(theme.surface)
                            .border(1.dp, if (name.isNotBlank()) BiteyOrange.copy(alpha = 0.5f) else theme.border.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (name.isEmpty()) Text("Dish name (required)", fontSize = 13.sp, color = theme.inkMuted)
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(theme.surfaceVariant).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                MealType.entries.forEach { type ->
                    val selected = mealType == type
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selected) BiteyOrange else Color.Transparent)
                            .clickable { onMealTypeChange(type) }.padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = type.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 10.5.sp), color = if (selected) StickerDieCutWhite else theme.inkSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                candidates.firstOrNull()?.let { single ->
                    ReviewGridItem(item = single, isStickerMode = isStickerMode, onToggle = { onToggleCandidate(single.id) }, modifier = Modifier.fillMaxSize())
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                itemsIndexed(candidates, key = { _, item -> item.id }) { index, item ->
                    MultiDishCandidateCard(
                        item = item, index = index, isStickerMode = isStickerMode,
                        onToggle = { onToggleCandidate(item.id) },
                        onNameChange = { onUpdateCandidateName(item.id, it) },
                        onMealTypeChange = { onUpdateCandidateMealType(item.id, it) }
                    )
                }
            }
        }
    }
}
