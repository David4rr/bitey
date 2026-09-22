package com.bitey.app.feature.camera.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CandidateStickerItem
import java.io.File

@Composable
fun NewEntryReviewView(
    name: String = "Food",
    onNameChange: (String) -> Unit = {},
    mealType: MealType = MealType.FOOD,
    onMealTypeChange: (MealType) -> Unit = {},
    candidates: List<CandidateStickerItem>,
    onToggleCandidate: (String) -> Unit,
    onUpdateCandidateName: (id: String, name: String) -> Unit = { _, _ -> },
    onUpdateCandidateMealType: (id: String, mealType: MealType) -> Unit = { _, _ -> },
    onCutItMyselfClick: () -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier,
    isStickerMode: Boolean = true,
    onStickerModeChange: (Boolean) -> Unit = {}
) {
    val theme = LocalNeumorphicTheme.current
    val selectedCount = candidates.count { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Top Header: Title & Save Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isStickerMode) "Review Food Stickers" else "Review Food Photos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )
                Text(
                    text = if (selectedCount > 1) "$selectedCount dishes ready to save" else "Set food name & meal type per dish",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedCount > 1) BiteyOrange else theme.inkSecondary
                )
            }
            Button(
                onClick = onSaveAndClose,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Save", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display Mode: Sticker Mode vs Full Photo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isStickerMode) BiteyOrange else Color.Transparent)
                    .clickable { onStickerModeChange(true) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sticker Mode",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isStickerMode) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isStickerMode) StickerDieCutWhite else theme.inkSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (!isStickerMode) BiteyOrange else Color.Transparent)
                    .clickable { onStickerModeChange(false) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Full Photo",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (!isStickerMode) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (!isStickerMode) StickerDieCutWhite else theme.inkSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subheader with manual cutout option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dishes (${selectedCount}/${candidates.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = theme.inkPrimary
            )
            if (isStickerMode && candidates.size <= 1) {
                OutlinedButton(
                    onClick = onCutItMyselfClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BiteyOrange.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Adjust Cut", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = BiteyOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Per-Image List: Image Preview, Food Name, and Meal Type Selector
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(candidates, key = { it.id }) { item ->
                val displayFile = File(if (isStickerMode) item.stickerFilePath else item.originalFilePath)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(
                        width = if (item.isSelected) 1.5.dp else 1.dp,
                        color = if (item.isSelected) BiteyOrange else theme.border.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (item.isSelected) 1f else 0.5f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .then(
                                        if (isStickerMode) Modifier.background(theme.surfaceVariant).padding(6.dp)
                                        else Modifier
                                    )
                                    .clickable { onToggleCandidate(item.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = displayFile,
                                    contentDescription = item.label,
                                    contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isStickerMode) Modifier.dieCutStickerEffect()
                                            else Modifier.clip(RoundedCornerShape(14.dp))
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Food Name Input per Image
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = item.label,
                                    onValueChange = { onUpdateCandidateName(item.id, it) },
                                    label = { Text("Food Name", fontSize = 11.sp, color = theme.inkSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = theme.inkPrimary,
                                        unfocusedTextColor = theme.inkPrimary,
                                        focusedBorderColor = BiteyOrange,
                                        unfocusedBorderColor = theme.border.copy(alpha = 0.5f),
                                        focusedContainerColor = theme.surface,
                                        unfocusedContainerColor = theme.surface
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Toggle Include/Exclude Checkbox
                            IconButton(
                                onClick = { onToggleCandidate(item.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isSelected) BiteyOrange else theme.surfaceVariant)
                                    .border(1.5.dp, if (item.isSelected) BiteyOrange else theme.border, CircleShape)
                            ) {
                                if (item.isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = StickerDieCutWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Meal Type Selector per Image
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceVariant)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            MealType.entries.forEach { type ->
                                val isTypeSelected = item.mealType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isTypeSelected) BiteyOrange else Color.Transparent)
                                        .clickable { onUpdateCandidateMealType(item.id, type) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isTypeSelected) StickerDieCutWhite else theme.inkSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
