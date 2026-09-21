package com.bitey.app.feature.footprints.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.location.model.NavigationRoute
import com.bitey.app.core.location.model.RouteStep
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File

@Composable
fun NavigationTopBanner(
    currentStep: RouteStep?,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val step = currentStep ?: return

    val icon = when {
        step.maneuverType == "arrive" -> Icons.Rounded.Flag
        step.modifier?.contains("left") == true -> Icons.AutoMirrored.Rounded.ArrowBack
        step.modifier?.contains("right") == true -> Icons.AutoMirrored.Rounded.ArrowForward
        else -> Icons.Rounded.Navigation
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .minimalistCard(cornerRadius = 22.dp, elevation = 6.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BiteyOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StickerDieCutWhite,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                val distText = when {
                    step.distanceMeters >= 1000 -> String.format(java.util.Locale.US, "In %.1f km", step.distanceMeters / 1000.0)
                    step.distanceMeters > 20 -> "In ${step.distanceMeters.toInt()} m"
                    else -> "Now"
                }

                Text(
                    text = distText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BiteyOrange
                )
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.inkPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NavigationBottomPanel(
    targetEntry: PlateEntryWithTags,
    route: NavigationRoute,
    onStopNavigation: () -> Unit,
    onRecenterRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val entry = targetEntry.entry
    val stickerPath = entry.stickerImagePath?.takeIf { File(it).exists() }
        ?: entry.getAllStickerPaths().firstOrNull { File(it).exists() } ?: entry.fullImagePath
    val isSticker = entry.isStickerMode || stickerPath != entry.fullImagePath

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .minimalistCard(cornerRadius = 24.dp, elevation = 6.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(stickerPath),
                    contentDescription = entry.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isSticker) Modifier.dieCutStickerEffect()
                            else Modifier.clip(RoundedCornerShape(12.dp))
                        ),
                    contentScale = if (isSticker) ContentScale.Fit else ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = theme.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = route.formattedDuration,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BiteyOrange
                    )
                    Text(text = "•", color = theme.inkMuted)
                    Text(
                        text = route.formattedDistance,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.inkSecondary
                    )
                }
            }

            IconButton(
                onClick = onRecenterRoute,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Rounded.NearMe,
                    contentDescription = "Fit Route",
                    tint = BiteyOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Button(
                onClick = onStopNavigation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" End", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
