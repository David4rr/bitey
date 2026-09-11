package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp
                ),
                color = if (isSelected) BiteyOrange else theme.inkSecondary
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.width(14.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(BiteyOrange))
            }
        }
    }
}

@Composable
fun EmptyJournalPrompt(onCaptureClick: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalNeumorphicTheme.current
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth().minimalistCard(cornerRadius = 24.dp, elevation = 1.dp).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(BiteyOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.Restaurant, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "No Bites Logged Yet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Capture your meal to start creating die-cut stickers and tracking your culinary adventures.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.inkSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onCaptureClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = StickerDieCutWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Capture First Bite", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = StickerDieCutWhite)
                }
            }
        }
    }
}

@Composable
fun NoSearchResultsPrompt(onClearFilters: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalNeumorphicTheme.current
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth().minimalistCard(cornerRadius = 24.dp, elevation = 1.dp).padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = theme.inkMuted, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "No Matching Bites", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try adjusting your search terms or meal type filters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Reset Filters", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = theme.inkPrimary)
                }
            }
        }
    }
}
