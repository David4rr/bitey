package com.bitey.app.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
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
fun MultiDishCandidateCard(
    item: CandidateStickerItem,
    index: Int,
    isStickerMode: Boolean,
    onToggle: () -> Unit,
    onNameChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val displayFile = File(if (isStickerMode) item.stickerFilePath else item.originalFilePath)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surface)
            .border(
                width = 1.dp,
                color = if (item.isSelected) BiteyOrange.copy(alpha = 0.35f) else theme.border.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .alpha(if (item.isSelected) 1f else 0.45f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceVariant.copy(alpha = 0.5f))
                .clickable(onClick = onToggle)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = displayFile,
                contentDescription = "Dish ${index + 1}",
                contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isStickerMode) Modifier.dieCutStickerEffect() else Modifier.clip(RoundedCornerShape(8.dp)))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (item.isSelected) BiteyOrange else theme.surface.copy(alpha = 0.8f))
                    .border(1.dp, if (item.isSelected) BiteyOrange.copy(alpha = 0.5f) else theme.border.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = StickerDieCutWhite,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        BasicTextField(
            value = item.label,
            onValueChange = onNameChange,
            enabled = item.isSelected,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = theme.inkPrimary
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            cursorBrush = SolidColor(BiteyOrange),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.background.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            if (item.label.isNotBlank()) BiteyOrange.copy(alpha = 0.4f) else theme.border.copy(alpha = 0.18f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (item.label.isEmpty()) {
                        Text(
                            text = "Dish ${index + 1} name",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = theme.inkMuted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceVariant.copy(alpha = 0.6f))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MealType.entries.forEach { type ->
                val selected = item.mealType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) BiteyOrange else Color.Transparent)
                        .clickable(enabled = item.isSelected) { onMealTypeChange(type) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 9.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) StickerDieCutWhite else theme.inkSecondary
                    )
                }
            }
        }
    }
}
