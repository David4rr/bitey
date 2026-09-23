package com.bitey.app.feature.journal.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.LocalAnimatedVisibilityScope
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import kotlinx.coroutines.launch

@Composable
fun JournalDetailBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            val offsetY = remember { Animatable(screenHeightPx) }

            LaunchedEffect(Unit) {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            fun dismiss() {
                coroutineScope.launch {
                    offsetY.animateTo(
                        targetValue = screenHeightPx,
                        animationSpec = tween(220)
                    )
                    onDismissRequest()
                }
            }

            BackHandler(enabled = true) {
                dismiss()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.52f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { dismiss() }
                        )
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = offsetY.value
                        }
                        .draggable(
                            state = rememberDraggableState { delta ->
                                if (delta > 0 || offsetY.value > 0) {
                                    coroutineScope.launch {
                                        offsetY.snapTo((offsetY.value + delta).coerceAtLeast(0f))
                                    }
                                }
                            },
                            orientation = Orientation.Vertical,
                            onDragStopped = { velocity ->
                                if (offsetY.value > screenHeightPx * 0.22f || velocity > 1200f) {
                                    dismiss()
                                } else {
                                    coroutineScope.launch {
                                        offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                }
                            }
                        ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = theme.surface
                ) {
                    content()
                }
            }
        }
    }
}
