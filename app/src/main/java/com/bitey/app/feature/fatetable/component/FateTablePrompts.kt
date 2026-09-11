package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun FateFilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BiteyOrange else theme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) StickerDieCutWhite else if (enabled) theme.inkSecondary else theme.inkMuted
        )
    }
}

@Composable
fun InsufficientCandidatesPrompt(
    totalEntriesCount: Int,
    onCaptureClick: () -> Unit,
    onResetFilters: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BiteyOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restaurant,
                    contentDescription = null,
                    tint = BiteyOrange,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (totalEntriesCount < 2) "Wheel of Serendipity" else "Need More Options",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = theme.inkPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (totalEntriesCount < 2) {
                    "Record at least 2 meals in your journal to unlock Fate's Table and spin for your next meal."
                } else {
                    "The current filter has fewer than 2 candidates. Switch to 'All Recorded' or clear tag filters."
                },
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (totalEntriesCount < 2) {
                Button(
                    onClick = onCaptureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record a Bite")
                }
            } else {
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Filters")
                }
            }
        }
    }
}
