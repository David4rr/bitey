package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsSelectionCard(
    availableTags: List<TagEntity>,
    selectedTags: Set<TagEntity>,
    onToggleTag: (TagEntity) -> Unit,
    onOpenNewTagDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
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
                        color = theme.inkPrimary
                    )
                }

                TextButton(onClick = onOpenNewTagDialog) {
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
                availableTags.forEach { tag ->
                    val isSelected = selectedTags.any { it.tagId == tag.tagId || it.tagName == tag.tagName }
                    TagChip(
                        tag = tag,
                        isSelected = isSelected,
                        onToggle = { onToggleTag(tag) }
                    )
                }
            }
        }
    }
}
