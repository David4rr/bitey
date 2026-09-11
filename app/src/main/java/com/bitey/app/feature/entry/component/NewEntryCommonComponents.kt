package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun ProcessingCard(
    title: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = BiteyOrange,
                modifier = Modifier.size(52.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary
            )
        }
    }
}

@Composable
fun MetadataRow(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.inkMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodySmall,
            color = theme.inkSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = theme.inkPrimary
        )
    }
}
