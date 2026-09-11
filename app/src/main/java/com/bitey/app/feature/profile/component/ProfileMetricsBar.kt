package com.bitey.app.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun ProfileMetricsBar(
    totalEntriesCount: Int,
    favoriteEntriesCount: Int,
    spotsWithLocationCount: Int,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricColumn(
            value = "$totalEntriesCount",
            label = "Total Bites",
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(theme.border)
        )
        MetricColumn(
            value = "$favoriteEntriesCount",
            label = "Favorites",
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(theme.border)
        )
        MetricColumn(
            value = "$spotsWithLocationCount",
            label = "Spots Visited",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = theme.inkPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = theme.inkSecondary,
            fontSize = 11.sp
        )
    }
}
