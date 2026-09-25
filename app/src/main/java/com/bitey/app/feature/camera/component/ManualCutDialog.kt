package com.bitey.app.feature.camera.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import java.io.File
import kotlin.math.max

@Composable
fun ManualCutDialog(
    imageFilePath: String,
    onDismiss: () -> Unit,
    onApplyCut: (normCenterX: Float, normCenterY: Float, normRadius: Float) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var circleCenter by remember { mutableStateOf<Offset?>(null) }
    var circleRadius by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Smart Circle Cut", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Circle or drag over the food object to cut",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(theme.surfaceVariant)
                        .onSizeChanged {
                            canvasSize = it
                            if (circleCenter == null && it.width > 0 && it.height > 0) {
                                circleCenter = Offset(it.width / 2f, it.height / 2f)
                                circleRadius = minOf(it.width, it.height) * 0.35f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(imageFilePath),
                        contentDescription = "Original Dish",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(canvasSize) {
                                var dragStart = Offset.Zero
                                detectDragGestures(
                                    onDragStart = { start ->
                                        dragStart = start
                                        circleCenter = start
                                        circleRadius = 30f
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val current = change.position
                                        val minX = minOf(dragStart.x, current.x)
                                        val maxX = maxOf(dragStart.x, current.x)
                                        val minY = minOf(dragStart.y, current.y)
                                        val maxY = maxOf(dragStart.y, current.y)
                                        val center = Offset((minX + maxX) / 2f, (minY + maxY) / 2f)
                                        val radius = max(maxX - minX, maxY - minY) / 2f
                                        circleCenter = center
                                        circleRadius = radius.coerceAtLeast(24f)
                                    }
                                )
                            }
                    ) {
                        val center = circleCenter ?: Offset(size.width / 2f, size.height / 2f)
                        val radius = if (circleRadius > 0f) circleRadius else minOf(size.width, size.height) * 0.35f

                        // Scrim path masking out the circle
                        val scrimPath = Path().apply {
                            fillType = PathFillType.EvenOdd
                            addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                            addOval(androidx.compose.ui.geometry.Rect(center, radius))
                        }
                        drawPath(scrimPath, color = Color.Black.copy(alpha = 0.52f))

                        // Smart circle boundary
                        drawCircle(color = StickerDieCutWhite, radius = radius + 2f, center = center, style = Stroke(width = 4f))
                        drawCircle(color = BiteyOrange, radius = radius, center = center, style = Stroke(width = 2.5f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val w = canvasSize.width.toFloat().coerceAtLeast(1f)
                        val h = canvasSize.height.toFloat().coerceAtLeast(1f)
                        val center = circleCenter ?: Offset(w / 2f, h / 2f)
                        val radius = if (circleRadius > 0f) circleRadius else minOf(w, h) * 0.35f
                        val normCx = (center.x / w).coerceIn(0f, 1f)
                        val normCy = (center.y / h).coerceIn(0f, 1f)
                        val normR = (radius / minOf(w, h)).coerceIn(0.05f, 0.9f)
                        onApplyCut(normCx, normCy, normR)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Cut Object & Remove Background", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
