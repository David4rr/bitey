package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun TagChip(
    tag: TagEntity,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BiteyMint else theme.surfaceVariant)
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
                color = if (isSelected) StickerDieCutWhite else theme.inkPrimary
            )
        }
    }
}

@Composable
fun NewTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String?) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    var tagName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Taste") }
    val categories = listOf("Taste", "Ambience", "Diet", "Type")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Tag",
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    placeholder = { Text("e.g. Extra Crispy", color = theme.inkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiteyOrange,
                        unfocusedBorderColor = theme.border,
                        focusedContainerColor = theme.surfaceVariant,
                        unfocusedContainerColor = theme.surfaceVariant,
                        focusedTextColor = theme.inkPrimary,
                        unfocusedTextColor = theme.inkPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.inkSecondary
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
                                .background(if (isCatSelected) BiteyOrange else theme.surfaceVariant)
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCatSelected) StickerDieCutWhite else theme.inkSecondary
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
                Text("Cancel", color = theme.inkSecondary)
            }
        },
        containerColor = theme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
