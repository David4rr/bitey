package com.bitey.app.feature.camera.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CandidateStickerItem

@Composable
fun NewEntryReviewView(
    name: String = "",
    onNameChange: (String) -> Unit = {},
    mealType: MealType = MealType.FOOD,
    onMealTypeChange: (MealType) -> Unit = {},
    candidates: List<CandidateStickerItem>,
    onToggleCandidate: (String) -> Unit,
    onUpdateCandidateName: (id: String, name: String) -> Unit = { _, _ -> },
    onUpdateCandidateMealType: (id: String, mealType: MealType) -> Unit = { _, _ -> },
    onCutItMyselfClick: () -> Unit = {}, onClose: () -> Unit = {},
    onSaveAndClose: () -> Unit, modifier: Modifier = Modifier,
    isStickerMode: Boolean = true, onStickerModeChange: (Boolean) -> Unit = {}
) {
    val theme = LocalNeumorphicTheme.current
    val selectedCount = candidates.count { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Minimal Top Header: Cancel (X), Title, Save button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cancel", tint = theme.inkSecondary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Review (${selectedCount}/${candidates.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )
            }
            Button(
                onClick = onSaveAndClose,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Save", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode switch and Adjust Cut row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.surfaceVariant)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(true to "Sticker Mode", false to "Full Photo").forEach { (isSticker, label) ->
                    val active = isStickerMode == isSticker
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) BiteyOrange else Color.Transparent)
                            .clickable { onStickerModeChange(isSticker) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.Medium),
                            color = if (active) StickerDieCutWhite else theme.inkSecondary
                        )
                    }
                }
            }

            if (isStickerMode && candidates.size <= 1) {
                OutlinedButton(
                    onClick = onCutItMyselfClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BiteyOrange.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Adjust Cut", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = BiteyOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Optional Dish Name without placeholder
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Dish name (optional)", fontSize = 12.sp, color = theme.inkSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.inkPrimary, unfocusedTextColor = theme.inkPrimary,
                focusedBorderColor = BiteyOrange, unfocusedBorderColor = theme.border.copy(alpha = 0.5f),
                focusedContainerColor = theme.surface, unfocusedContainerColor = theme.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Compact Meal Type selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(theme.surfaceVariant)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MealType.entries.forEach { type ->
                val selected = mealType == type
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (selected) BiteyOrange else Color.Transparent)
                        .clickable { onMealTypeChange(type) }.padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 10.5.sp),
                        color = if (selected) StickerDieCutWhite else theme.inkSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Food Sticker Review Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(candidates, key = { it.id }) { item ->
                ReviewGridItem(
                    item = item,
                    isStickerMode = isStickerMode,
                    onToggle = { onToggleCandidate(item.id) }
                )
            }
        }
    }
}
