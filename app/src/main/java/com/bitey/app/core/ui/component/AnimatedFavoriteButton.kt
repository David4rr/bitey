package com.bitey.app.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import kotlinx.coroutines.launch

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    containerSize: Dp = 38.dp,
    withContainer: Boolean = true,
    withNeumorphicContainer: Boolean = withContainer
) {
    val theme = LocalNeumorphicTheme.current
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1.0f) }
    val rotateAnim = remember { Animatable(0f) }

    val iconColor by animateColorAsState(
        targetValue = if (isFavorite) BiteyOrange else theme.inkMuted,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "favorite_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    val handleToggle: () -> Unit = {
        coroutineScope.launch {
            // Heart pop scale & wobble bounce
            launch {
                scaleAnim.animateTo(
                    targetValue = 1.45f,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
                )
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                rotateAnim.animateTo(
                    targetValue = -15f,
                    animationSpec = tween(durationMillis = 80)
                )
                rotateAnim.animateTo(
                    targetValue = 12f,
                    animationSpec = tween(durationMillis = 100)
                )
                rotateAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            }
        }
        onToggle()
    }

    val showContainer = withContainer && withNeumorphicContainer
    if (showContainer) {
        Box(
            modifier = modifier
                .size(containerSize)
                .clip(CircleShape)
                .background(theme.surface)
                .border(BorderStroke(1.dp, theme.border), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = handleToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = iconColor,
                modifier = Modifier
                    .size(iconSize)
                    .scale(scaleAnim.value)
                    .rotate(rotateAnim.value)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(containerSize)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = handleToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = iconColor,
                modifier = Modifier
                    .size(iconSize)
                    .scale(scaleAnim.value)
                    .rotate(rotateAnim.value)
            )
        }
    }
}
