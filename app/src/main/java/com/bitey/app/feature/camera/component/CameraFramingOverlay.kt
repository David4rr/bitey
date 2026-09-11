package com.bitey.app.feature.camera.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.feature.camera.CameraRatio
import kotlin.math.roundToInt

@Composable
fun CameraFramingOverlay(
    selectedRatio: CameraRatio,
    squareSideDp: Dp,
    verticalMarginDp: Dp,
    focusPoint: Pair<Float, Float>?,
    showFocusRing: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedRatio == CameraRatio.SQUARE_1_1) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Dimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(verticalMarginDp)
                        .background(Color.Black.copy(alpha = 0.65f))
                )
                // Center Active Square Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(squareSideDp)
                        .border(1.dp, Color.White.copy(alpha = 0.35f))
                )
                // Bottom Dimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.65f))
                )
            }
        }

        // Tap-to-focus visual ring indicator
        focusPoint?.let { (fx, fy) ->
            val ringSize = 64.dp
            val ringHalfPx = with(density) { (ringSize / 2).toPx() }

            AnimatedVisibility(
                visible = showFocusRing,
                enter = scaleIn(tween(200)) + fadeIn(tween(150)),
                exit = scaleOut(tween(300)) + fadeOut(tween(300)),
                modifier = Modifier.offset {
                    IntOffset(
                        (fx - ringHalfPx).roundToInt(),
                        (fy - ringHalfPx).roundToInt()
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(ringSize)
                        .border(2.dp, BiteyWarmYellow, CircleShape)
                )
            }
        }
    }
}
