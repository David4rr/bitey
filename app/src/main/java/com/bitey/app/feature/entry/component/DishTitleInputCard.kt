package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun DishTitleInputCard(
    dishTitle: String,
    onDishTitleChange: (String) -> Unit,
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
            Text(
                text = "Dish Title",
                style = MaterialTheme.typography.titleSmall,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dishTitle,
                onValueChange = onDishTitleChange,
                placeholder = { Text("e.g. Smoked Wagyu Brisket", color = theme.inkMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BiteyOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = theme.surfaceVariant,
                    unfocusedContainerColor = theme.surfaceVariant,
                    focusedTextColor = theme.inkPrimary,
                    unfocusedTextColor = theme.inkPrimary
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )
        }
    }
}
