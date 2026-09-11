package com.bitey.app.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CandidateStickerItem
import java.io.File

@Composable
fun NewEntryReviewView(
    name: String,
    onNameChange: (String) -> Unit,
    mealType: MealType,
    onMealTypeChange: (MealType) -> Unit,
    candidates: List<CandidateStickerItem>,
    onToggleCandidate: (String) -> Unit,
    onCutItMyselfClick: () -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val selectedCount = candidates.count { it.isSelected }

    Column(
        modifier = modifier.fillMaxSize().background(theme.background).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Review Stickers", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                Text(text = "Closing sheet saves everything", style = MaterialTheme.typography.bodySmall, color = theme.inkSecondary)
            }
            Button(
                onClick = onSaveAndClose,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Save ($selectedCount)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Name", color = theme.inkSecondary) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.inkPrimary, unfocusedTextColor = theme.inkPrimary,
                focusedBorderColor = BiteyOrange, unfocusedBorderColor = theme.border,
                focusedContainerColor = theme.surface, unfocusedContainerColor = theme.surface
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(theme.surfaceVariant).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealType.entries.forEach { type ->
                val isSelected = mealType == type
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BiteyOrange else Color.Transparent)
                        .clickable { onMealTypeChange(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                        color = if (isSelected) StickerDieCutWhite else theme.inkSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Pick stickers to keep (${selectedCount}/${candidates.size}):", style = MaterialTheme.typography.labelLarge, color = theme.inkPrimary)
            OutlinedButton(
                onClick = onCutItMyselfClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BiteyOrange.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Cut it myself", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = BiteyOrange)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(candidates, key = { it.id }) { item ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(135.dp).clip(RoundedCornerShape(18.dp)).background(theme.surface)
                        .border(if (item.isSelected) 2.dp else 1.dp, if (item.isSelected) BiteyOrange else theme.border.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .clickable { onToggleCandidate(item.id) }.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(model = File(item.stickerFilePath), contentDescription = "Sticker", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(8.dp))
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape)
                            .background(if (item.isSelected) BiteyOrange else theme.surfaceVariant)
                            .border(1.5.dp, if (item.isSelected) BiteyOrange else theme.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isSelected) Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = StickerDieCutWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
