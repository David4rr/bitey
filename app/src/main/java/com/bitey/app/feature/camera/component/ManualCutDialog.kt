package com.bitey.app.feature.camera.component

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
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
import com.bitey.app.feature.camera.RecognizedSubjectBox
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Composable
fun ManualCutDialog(
    imageFilePath: String,
    detectedSubjects: List<RecognizedSubjectBox>,
    onDismiss: () -> Unit,
    onApplyMask: (subjectId: Int?, customBounds: RectF?) -> Unit
) {
    val theme = LocalNeumorphicTheme.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var customBounds by remember { mutableStateOf<RectF?>(null) }

    LaunchedEffect(detectedSubjects) {
        if (selectedSubjectId == null && detectedSubjects.isNotEmpty()) selectedSubjectId = detectedSubjects.first().id
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Intelligent Masking", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = theme.inkPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedSubjectId != null || customBounds != null) {
                            IconButton(onClick = { selectedSubjectId = null; customBounds = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reset Mask", tint = theme.inkSecondary)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = theme.inkSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        selectedSubjectId != null -> "✨ Snapped to recognized dish • Mask active"
                        customBounds != null -> "✨ Custom region masked • Ready to cut"
                        detectedSubjects.isNotEmpty() -> "Recognition assistance: Tap or brush a dish"
                        else -> "Brush over dish to intelligently mask"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedSubjectId != null || customBounds != null) BiteyOrange else theme.inkSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(18.dp)).background(theme.surfaceVariant).onSizeChanged { canvasSize = it },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(model = File(imageFilePath), contentDescription = "Original Dish", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                            .pointerInput(canvasSize, detectedSubjects) {
                                detectTapGestures { tap ->
                                    val nx = (tap.x / size.width).coerceIn(0f, 1f); val ny = (tap.y / size.height).coerceIn(0f, 1f)
                                    val hit = detectedSubjects.firstOrNull { nx in it.left..it.right && ny in it.top..it.bottom }
                                    if (hit != null) { selectedSubjectId = hit.id; customBounds = null }
                                }
                            }
                            .pointerInput(canvasSize, detectedSubjects) {
                                var dragStart = Offset.Zero
                                detectDragGestures(
                                    onDragStart = { dragStart = it },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val p = change.position
                                        val nx = (p.x / size.width).coerceIn(0f, 1f); val ny = (p.y / size.height).coerceIn(0f, 1f)
                                        val hit = detectedSubjects.firstOrNull { nx in it.left..it.right && ny in it.top..it.bottom }
                                        if (hit != null) {
                                            selectedSubjectId = hit.id; customBounds = null
                                        } else {
                                            val x0 = min(dragStart.x, p.x) / size.width; val y0 = min(dragStart.y, p.y) / size.height
                                            val x1 = max(dragStart.x, p.x) / size.width; val y1 = max(dragStart.y, p.y) / size.height
                                            customBounds = RectF(x0.coerceIn(0f, 1f), y0.coerceIn(0f, 1f), x1.coerceIn(0f, 1f), y1.coerceIn(0f, 1f))
                                            selectedSubjectId = null
                                        }
                                    }
                                )
                            }
                    ) {
                        val activeBox = if (selectedSubjectId != null) {
                            detectedSubjects.firstOrNull { it.id == selectedSubjectId }?.let { RectF(it.left, it.top, it.right, it.bottom) }
                        } else customBounds

                        if (activeBox != null) {
                            val r = Rect(activeBox.left * size.width, activeBox.top * size.height, activeBox.right * size.width, activeBox.bottom * size.height)
                            val scrim = Path().apply {
                                fillType = PathFillType.EvenOdd; addRect(Rect(Offset.Zero, size)); addRoundRect(RoundRect(r, CornerRadius(16f, 16f)))
                            }
                            drawPath(scrim, color = Color.Black.copy(alpha = 0.50f))
                            drawRoundRect(color = StickerDieCutWhite, topLeft = r.topLeft, size = r.size, cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = 4.5f))
                            drawRoundRect(color = BiteyOrange, topLeft = r.topLeft, size = r.size, cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = 2.5f))
                        } else {
                            detectedSubjects.forEach { sub ->
                                val r = Rect(sub.left * size.width, sub.top * size.height, sub.right * size.width, sub.bottom * size.height)
                                drawRoundRect(color = BiteyOrange.copy(alpha = 0.6f), topLeft = r.topLeft, size = r.size, cornerRadius = CornerRadius(14f, 14f), style = Stroke(width = 2f))
                            }
                        }
                    }

                    if (detectedSubjects.isNotEmpty()) {
                        Row(modifier = Modifier.align(Alignment.TopStart).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            detectedSubjects.forEach { sub ->
                                val isSelected = sub.id == selectedSubjectId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BiteyOrange else Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier.clickable { selectedSubjectId = sub.id; customBounds = null }
                                ) {
                                    Text(
                                        text = "✨ ${sub.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = StickerDieCutWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onApplyMask(selectedSubjectId, customBounds) },
                    enabled = selectedSubjectId != null || customBounds != null,
                    colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Intelligent Mask", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
