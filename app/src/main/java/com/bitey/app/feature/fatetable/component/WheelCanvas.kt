package com.bitey.app.feature.fatetable.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WheelCanvas(
    candidates: List<PlateEntryWithTags>,
    rotationAngle: () -> Float,
    winningEntry: PlateEntryWithTags? = null,
    isSpinning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalNeumorphicTheme.current
    val density = LocalDensity.current
    val sliceCount = candidates.size
    val hasWinner = winningEntry != null && !isSpinning

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val minDimension = if (maxWidth < maxHeight) maxWidth else maxHeight
        val radiusPx = with(density) { (minDimension / 2f).toPx() }
        val itemSizeDp = if (sliceCount >= 7) 54.dp else 64.dp
        val itemSizePx = with(density) { itemSizeDp.toPx() }
        val paddingPx = with(density) { 14.dp.toPx() }
        val orbitRadiusPx = radiusPx - (itemSizePx / 2f) - paddingPx
        val sliceAngle = 360f / sliceCount

        candidates.forEachIndexed { i, candidate ->
            val isSelected = hasWinner && candidate.entry.id == winningEntry.entry.id

            val targetScale = if (isSelected) 1.28f else if (hasWinner) 0.82f else 1.0f
            val targetAlpha = if (isSelected) 1.0f else if (hasWinner) 0.50f else 1.0f

            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dishScale"
            )

            val alpha by animateFloatAsState(
                targetValue = targetAlpha,
                label = "dishAlpha"
            )

            val stickerPath = candidate.entry.stickerImagePath?.takeIf { File(it).exists() }
                ?: candidate.entry.getAllStickerPaths().firstOrNull { File(it).exists() }
                ?: candidate.entry.fullImagePath.takeIf { it.isNotBlank() && File(it).exists() }
            val isSticker = candidate.entry.isStickerMode || (stickerPath != null && stickerPath != candidate.entry.fullImagePath)
            val imageRequest = remember(stickerPath, isSticker) {
                if (stickerPath != null) {
                    ImageRequest.Builder(context)
                        .data(File(stickerPath))
                        .apply {
                            if (isSticker) {
                                transformations(CropTransparentTransformation())
                            }
                        }
                        .crossfade(true)
                        .build()
                } else null
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(80.dp)
                    .graphicsLayer {
                        val angleDeg = (rotationAngle() + (i * sliceAngle) - 90f)
                        val rad = Math.toRadians(angleDeg.toDouble())
                        translationX = (orbitRadiusPx.toDouble() * cos(rad)).toFloat()
                        translationY = (orbitRadiusPx.toDouble() * sin(rad)).toFloat()
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(itemSizeDp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (stickerPath != null && imageRequest != null) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = candidate.entry.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (isSticker) Modifier.dieCutStickerEffect()
                                    else Modifier
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.85f)
                                .clip(CircleShape)
                                .background(BiteyOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = candidate.entry.title,
                                tint = BiteyOrange,
                                modifier = Modifier.size(if (sliceCount >= 7) 20.dp else 24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = candidate.entry.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = if (isSelected) 12.sp else 11.sp
                    ),
                    color = if (isSelected) BiteyOrange else theme.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
