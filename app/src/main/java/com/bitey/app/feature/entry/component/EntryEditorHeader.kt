package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun EntryEditorHeader(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .minimalistCard(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.inkPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Journal Your Bite",
                    style = MaterialTheme.typography.titleLarge,
                    color = theme.inkPrimary
                )
                Text(
                    text = "Document taste, memory & location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.inkSecondary
                )
            }
        }

        AnimatedFavoriteButton(
            isFavorite = isFavorite,
            onToggle = onToggleFavorite,
            containerSize = 42.dp,
            iconSize = 20.dp,
            withNeumorphicContainer = true
        )
    }
}
