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
    val configuration = LocalConfiguration.current
    val maxSheetHeight = (configuration.screenHeightDp.dp - 64.dp).coerceAtLeast(320.dp)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            val dragOffset = remember { Animatable(0f) }
            val slideOffset = remember { Animatable(1f) } // 1f = fully off-screen below

            LaunchedEffect(Unit) {
                slideOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }

            fun dismiss() {
                coroutineScope.launch {
                    onDismissRequest()
                }
            }

            BackHandler(enabled = true) {
                dismiss()
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
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
                        .heightIn(max = maxSheetHeight)
                        .wrapContentHeight(Alignment.Bottom)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = dragOffset.value + (size.height * slideOffset.value)
                        }
                        .draggable(
                            state = rememberDraggableState { delta ->
                                if (delta > 0 || dragOffset.value > 0) {
                                    coroutineScope.launch {
                                        dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f))
                                    }
                                }
                            },
                            orientation = Orientation.Vertical,
                            onDragStopped = { velocity ->
                                if (dragOffset.value > 160f || velocity > 1200f) {
                                    coroutineScope.launch {
                                        dragOffset.animateTo(800f, tween(180))
                                        dismiss()
                                    }
                                } else {
                                    coroutineScope.launch {
                                        dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
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
