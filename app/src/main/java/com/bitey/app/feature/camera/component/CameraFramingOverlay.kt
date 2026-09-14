package com.bitey.app.feature.camera.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
                // Top Dimmer (Soft cinematic scrim)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(verticalMarginDp)
                        .background(Color.Black.copy(alpha = 0.45f))
                )
                // Center Active Square Frame with subtle corner guides & grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(squareSideDp)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f))
                        .drawWithContent {
                            drawContent()

                            val strokeW = 1.5.dp.toPx()
                            val cornerLen = 18.dp.toPx()
                            val cornerColor = Color.White.copy(alpha = 0.85f)
                            val gridColor = Color.White.copy(alpha = 0.08f)
                            val w = size.width
                            val h = size.height

                            // Faint rule-of-thirds grid lines
                            val oneThirdX = w / 3f
                            val twoThirdsX = (w * 2) / 3f
                            val oneThirdY = h / 3f
                            val twoThirdsY = (h * 2) / 3f

                            drawLine(gridColor, Offset(oneThirdX, 0f), Offset(oneThirdX, h), strokeWidth = 0.5.dp.toPx())
                            drawLine(gridColor, Offset(twoThirdsX, 0f), Offset(twoThirdsX, h), strokeWidth = 0.5.dp.toPx())
                            drawLine(gridColor, Offset(0f, oneThirdY), Offset(w, oneThirdY), strokeWidth = 0.5.dp.toPx())
                            drawLine(gridColor, Offset(0f, twoThirdsY), Offset(w, twoThirdsY), strokeWidth = 0.5.dp.toPx())

                            // Top-Left Corner Bracket
                            drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = strokeW)
                            drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = strokeW)

                            // Top-Right Corner Bracket
                            drawLine(cornerColor, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeWidth = strokeW)
                            drawLine(cornerColor, Offset(w, 0f), Offset(w, cornerLen), strokeWidth = strokeW)

                            // Bottom-Left Corner Bracket
                            drawLine(cornerColor, Offset(0f, h), Offset(cornerLen, h), strokeWidth = strokeW)
                            drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cornerLen), strokeWidth = strokeW)

                            // Bottom-Right Corner Bracket
                            drawLine(cornerColor, Offset(w, h), Offset(w - cornerLen, h), strokeWidth = strokeW)
                            drawLine(cornerColor, Offset(w, h), Offset(w, h - cornerLen), strokeWidth = strokeW)
                        }
                )
                // Bottom Dimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            }
        }

        // Tap-to-focus visual ring indicator
        focusPoint?.let { (fx, fy) ->
            val ringSize = 56.dp
            val ringHalfPx = with(density) { (ringSize / 2).toPx() }

            AnimatedVisibility(
                visible = showFocusRing,
                enter = scaleIn(initialScale = 1.2f, animationSpec = tween(180)) + fadeIn(tween(150)),
                exit = scaleOut(targetScale = 0.8f, animationSpec = tween(250)) + fadeOut(tween(250)),
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
                        .border(1.5.dp, Color.White.copy(alpha = 0.90f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(BiteyWarmYellow)
                    )
                }
            }
        }
    }
}
