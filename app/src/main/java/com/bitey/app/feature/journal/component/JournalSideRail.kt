package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme

@Composable
fun JournalSideRail(
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    onNavigateToFootprints: () -> Unit,
    onNavigateToFateTable: () -> Unit,
    onNavigateToScrapbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Column(
        modifier = modifier
            .width(44.dp)
            .fillMaxHeight()
            .padding(start = 10.dp, top = 4.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VerticalActionText(
            text = if (isGridView) "GRID" else "LIST",
            icon = if (isGridView) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
            onClick = onToggleGridView,
            isSelected = true
        )

        Box(
            modifier = Modifier
                .width(16.dp)
                .height(1.dp)
                .background(theme.border)
        )

        VerticalActionText(
            text = "FOOTPRINT",
            icon = Icons.Rounded.Place,
            onClick = onNavigateToFootprints
        )

        VerticalActionText(
            text = "FATES TABLE",
            icon = Icons.Rounded.AutoAwesome,
            onClick = onNavigateToFateTable
        )

        VerticalActionText(
            text = "SCRAPBOOK",
            icon = Icons.Rounded.Collections,
            onClick = onNavigateToScrapbook
        )
    }
}

@Composable
fun VerticalActionText(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) BiteyOrange else BiteyOrange.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Layout(
            content = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        fontSize = 9.sp
                    ),
                    color = if (isSelected) BiteyOrange else theme.inkSecondary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = -90f
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                )
            }
        ) { measurables, _ ->
            val placeable = measurables[0].measure(Constraints())
            layout(placeable.height, placeable.width) {
                placeable.placeRelative(0, placeable.width)
            }
        }
    }
}
