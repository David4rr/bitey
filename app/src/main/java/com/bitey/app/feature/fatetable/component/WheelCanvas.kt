package com.bitey.app.feature.fatetable.component

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import kotlin.math.cos
import kotlin.math.sin

val SliceColors = listOf(
    Color(0xFFFF6B35), // BiteyOrange
    Color(0xFF2EC4B6), // BiteyMint
    Color(0xFFFFBF69), // Warm Amber
    Color(0xFF70C1B3), // Soft Sage
    Color(0xFFFF9F1C), // Deep Tangerine
    Color(0xFF6C5CE7), // Lavender
    Color(0xFFF15BB5), // Soft Pink
    Color(0xFF00BBF9)  // Sky Blue
)

@Composable
fun WheelCanvas(
    candidates: List<PlateEntryWithTags>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 3.dp.toPx() }
    val textSizePx = with(density) { 13.sp.toPx() }

    val textPaint = remember(textSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, 0x66000000)
        }
    }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val sliceCount = candidates.size
        val sliceAngle = 360f / sliceCount

        rotate(degrees = rotationAngle, pivot = center) {
            for (i in 0 until sliceCount) {
                val startAngle = -90f + (i * sliceAngle)
                val sliceColor = SliceColors[i % SliceColors.size]

                drawArc(
                    color = sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )

                val rad = Math.toRadians(startAngle.toDouble())
                val edgeX = center.x + radius * cos(rad).toFloat()
                val edgeY = center.y + radius * sin(rad).toFloat()
                drawLine(
                    color = StickerDieCutWhite,
                    start = center,
                    end = Offset(edgeX, edgeY),
                    strokeWidth = strokeWidthPx
                )

                val bisectorAngle = startAngle + (sliceAngle / 2f)
                val itemTitle = candidates[i].entry.title
                val displayTitle = if (itemTitle.length > 11) itemTitle.take(10) + "…" else itemTitle

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(bisectorAngle + 90f, center.x, center.y)

                val textDistance = radius * 0.65f
                drawContext.canvas.nativeCanvas.drawText(
                    displayTitle,
                    center.x,
                    center.y - textDistance,
                    textPaint
                )
                drawContext.canvas.nativeCanvas.restore()
            }

            drawCircle(
                color = StickerDieCutWhite,
                radius = radius - (strokeWidthPx / 2f),
                center = center,
                style = Stroke(width = strokeWidthPx)
            )
        }
    }
}

@Composable
fun WheelPointerIndicator(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp, 40.dp)) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(width / 2f, height)
            lineTo(width - 4f, 4f)
            lineTo(4f, 4f)
            close()
        }

        drawPath(path = path, color = Color(0x33000000))
        drawPath(path = path, color = StickerDieCutWhite)

        val innerPath = Path().apply {
            moveTo(width / 2f, height - 5f)
            lineTo(width - 8f, 7f)
            lineTo(8f, 7f)
            close()
        }
        drawPath(path = innerPath, color = BiteyOrange)
    }
}

@Composable
fun CenterHubCap() {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(theme.surface)
            .minimalistCard(cornerRadius = 27.dp, elevation = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BiteyOrange),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StickerDieCutWhite)
            )
        }
    }
}
