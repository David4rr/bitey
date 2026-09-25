package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.scrapbook.ScrapbookThemeUtils
import com.bitey.app.feature.scrapbook.model.CanvasAspectRatio
import com.bitey.app.feature.scrapbook.model.CanvasElement
import kotlin.math.roundToInt

/**
 * Interactive Scrapbook Canvas Viewport handling pan/zoom/rotate transformations,
 * layer ordering, and element selection.
 */
@Composable
fun ScrapbookCanvas(
    elements: List<CanvasElement>,
    selectedElementId: String?,
    aspectRatio: CanvasAspectRatio,
    backgroundColorHex: Long,
    isExporting: Boolean,
    onSelectElement: (String?) -> Unit,
    onUpdateTransform: (String, Float, Float, Float, Float) -> Unit,
    onBringToFront: (String) -> Unit,
    onSendToBack: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onRotate: (String, Float) -> Unit,
    onDelete: (String) -> Unit,
    onViewportSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    val density = LocalDensity.current
    val aspect = if (aspectRatio == CanvasAspectRatio.STORY_9_16) 9f / 16f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .onSizeChanged { size ->
                    if (size.width > 0 && size.height > 0) {
                        onViewportSizeChanged(size.width.toFloat(), size.height.toFloat())
                    }
                }
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(backgroundColorHex))
                .pointerInput(Unit) { detectTapGestures { onSelectElement(null) } },
            contentAlignment = Alignment.Center
        ) {
            if (elements.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Story Canvas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ScrapbookThemeUtils.getCanvasPrimaryTextColor(backgroundColorHex)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add food stickers and accessories below",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScrapbookThemeUtils.getCanvasSecondaryTextColor(backgroundColorHex),
                        textAlign = TextAlign.Center
                    )
                }
            }

            elements.sortedBy { it.zIndex }.forEach { element ->
                val isSelected = element.id == selectedElementId
                Box(
                    modifier = Modifier
                        .offset { IntOffset(element.xOffset.roundToInt(), element.yOffset.roundToInt()) }
                        .graphicsLayer {
                            rotationZ = element.rotation
                            scaleX = element.scale
                            scaleY = element.scale
                        }
                        .pointerInput(element.id) { detectTapGestures { onSelectElement(element.id) } }
                        .pointerInput(element.id) {
                            detectTransformGestures { _, pan, zoom, rot ->
                                onUpdateTransform(element.id, pan.x, pan.y, zoom, rot)
                            }
                        }
                ) {
                    CanvasElementItem(element = element, backgroundColorHex = backgroundColorHex)
                    if (isSelected) {
                        SelectionOverlay(
                            onFront = { onBringToFront(element.id) },
                            onBack = { onSendToBack(element.id) },
                            onDuplicate = { onDuplicate(element.id) },
                            onRotate = { delta -> onRotate(element.id, delta) },
                            onDelete = { onDelete(element.id) }
                        )
                    }
                }
            }

            if (isExporting) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BiteyOrange)
                }
            }
        }
    }
}
