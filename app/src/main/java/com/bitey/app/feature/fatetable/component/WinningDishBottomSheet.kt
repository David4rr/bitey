package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.journal.component.JournalDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinningDishBottomSheet(
    entryWithTags: PlateEntryWithTags,
    onDismiss: () -> Unit,
    onShuffleAgain: () -> Unit,
    onNavigate: () -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onToggleFavorite: (() -> Unit)? = null,
    onSaveEntry: ((PlateEntryEntity, List<TagEntity>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        JournalDetailSheet(
            item = entryWithTags,
            onClose = onDismiss,
            onToggleFavorite = { onToggleFavorite?.invoke() },
            onDelete = null,
            enableDelete = false,
            availableTags = availableTags,
            onSaveEntry = onSaveEntry,
            bottomAction = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onShuffleAgain,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.inkPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BiteyOrange
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle Again", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onNavigate,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteyOrange,
                            contentColor = StickerDieCutWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
