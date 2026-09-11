package com.bitey.app.feature.footprints.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.component.AnimatedFavoriteButton
import com.bitey.app.core.ui.component.MorphingSearchBar
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun FootprintsTopBar(
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    spotsCount: Int,
    favoritesCount: Int,
    isFavoritesOnly: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .minimalistCard(cornerRadius = 20.dp)
    ) {
        MorphingSearchBar(
            isSearchActive = isSearchActive,
            onSearchActiveChange = onSearchActiveChange,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            placeholderText = "Search visited dish, spot, or tag...",
            titleContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = theme.inkPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Culinary Map",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.inkPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BiteyOrange.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$spotsCount spots",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BiteyOrange
                                )
                            }
                        }
                        Text(
                            text = "$favoritesCount favorite pin markers",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.inkSecondary
                        )
                    }
                }
            },
            actions = {
                AnimatedFavoriteButton(
                    isFavorite = isFavoritesOnly,
                    onToggle = onToggleFavoritesOnly,
                    containerSize = 38.dp,
                    iconSize = 18.dp,
                    withContainer = true
                )
            }
        )
    }
}
