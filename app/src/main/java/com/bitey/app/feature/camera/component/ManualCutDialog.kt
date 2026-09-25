package com.bitey.app.feature.camera.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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

@Composable
fun ManualCutDialog(
    imageFilePath: String,
    onDismiss: () -> Unit,
    onApplyCut: (normPoints: List<Pair<Float, Float>>) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var points by remember { mutableStateOf<List<Offset>>(emptyList()) }

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
                        Text(text = "Outline Object", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (points.isNotEmpty()) {
                            IconButton(onClick = { points = emptyList() }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Redraw", tint = theme.inkSecondary)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (points.isEmpty()) "Trace an outline around the food object" else "Outline traced! Tap Cut to isolate object",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (points.isEmpty()) theme.inkSecondary else BiteyOrange
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(theme.surfaceVariant)
                        .onSizeChanged { canvasSize = it },
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
                                detectDragGestures(
                                    onDragStart = { start -> points = listOf(start) },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        points = points + change.position
                                    }
                                )
                            }
                    ) {
                        if (points.size >= 2) {
                            val outlinePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                                close()
                            }
                            val scrimPath = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                                addPath(outlinePath)
                            }
                            drawPath(scrimPath, color = Color.Black.copy(alpha = 0.52f))
                            drawPath(outlinePath, color = StickerDieCutWhite, style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawPath(outlinePath, color = BiteyOrange, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val w = canvasSize.width.toFloat().coerceAtLeast(1f)
                        val h = canvasSize.height.toFloat().coerceAtLeast(1f)
                        val normPoints = points.map { Pair((it.x / w).coerceIn(0f, 1f), (it.y / h).coerceIn(0f, 1f)) }
                        onApplyCut(normPoints)
                    },
                    enabled = points.size >= 3,
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
