package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.TagEntity
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

/**
 * Filter bar positioned with generous spacing above the bottom navigation bar.
 */
@Composable
fun JournalFilterBar(
    selectedMealType: MealType?,
    selectedTagId: Long?,
    availableTags: List<TagEntity>,
    onSelectMealType: (MealType?) -> Unit,
    onSelectTag: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        theme.background.copy(alpha = 0.85f),
                        theme.background
                    )
                )
            )
            .padding(top = 10.dp, bottom = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterPill(
                    text = "All",
                    isSelected = selectedMealType == null && selectedTagId == null,
                    onClick = {
                        onSelectMealType(null)
                        onSelectTag(null)
                    }
                )
            }
            items(MealType.entries, key = { it.name }) { mealType ->
                val isSelected = selectedMealType == mealType
                FilterPill(
                    text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = isSelected,
                    onClick = { onSelectMealType(if (isSelected) null else mealType) }
                )
            }
            items(availableTags, key = { it.tagId }) { tag ->
                FilterPill(
                    text = "#${tag.tagName}",
                    isSelected = selectedTagId == tag.tagId,
                    onClick = { onSelectTag(tag.tagId) }
                )
            }
        }
    }
}
