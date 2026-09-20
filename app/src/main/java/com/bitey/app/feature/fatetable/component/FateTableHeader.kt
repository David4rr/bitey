package com.bitey.app.feature.fatetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BrandHeaderLarge
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun FateTableHeader(
    candidateCount: Int,
    isSpinning: Boolean,
    onShuffle: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                            text = "Fate's Table",
                            style = BrandHeaderLarge,
                            color = BiteyOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BiteyOrange.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$candidateCount Options",
                                style = MaterialTheme.typography.labelSmall,
                                color = BiteyOrange
                            )
                        }
                    }
                    Text(
                        text = "Dining dilemma? Let serendipity choose your bite",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary
                    )
                }
            }

            if (candidateCount >= 2) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .minimalistCard(cornerRadius = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onShuffle, enabled = !isSpinning) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Candidates",
                            tint = if (isSpinning) theme.inkMuted else theme.inkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
