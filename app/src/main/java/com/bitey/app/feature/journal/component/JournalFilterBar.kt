package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

/**
 * Category filter bar with All, Food, Drink, and Other distributed evenly across the screen width.
 */
@Composable
fun JournalFilterBar(
    selectedMealType: MealType?,
    onSelectMealType: (MealType?) -> Unit,
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
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterPill(
                text = "All",
                isSelected = selectedMealType == null,
                onClick = { onSelectMealType(null) },
                modifier = Modifier.weight(1f)
            )
            MealType.entries.forEach { mealType ->
                val isSelected = selectedMealType == mealType
                FilterPill(
                    text = mealType.label,
                    isSelected = isSelected,
                    onClick = { onSelectMealType(if (isSelected) null else mealType) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
