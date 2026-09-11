package com.bitey.app.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasteProfileSection(
    favoriteTagNames: List<String>,
    topMealType: MealType?,
    modifier: Modifier = Modifier
) {
    if (favoriteTagNames.isEmpty() && topMealType == null) return

    val theme = LocalNeumorphicTheme.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Taste Profile",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = theme.inkPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        topMealType?.let { topMeal ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Most Logged Meal: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
                Text(
                    text = topMeal.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary
                )
            }
        }

        if (favoriteTagNames.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                favoriteTagNames.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.inkPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
