package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import java.io.File

/**
 * Renders individual canvas elements with authentic typography, die-cut food sticker effects,
 * and custom accessory cards.
 */
@Composable
fun CanvasElementItem(
    element: CanvasElement,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (element.type) {
            CanvasElementType.FOOD_STICKER -> {
                val file = element.imagePath?.let { File(it) }
                if (file != null && file.exists()) {
                    Box(
                        modifier = Modifier.size(150.dp).padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = element.text,
                            modifier = Modifier.fillMaxSize().dieCutStickerEffect(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            CanvasElementType.DATE_STAMP -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = element.text ?: "TODAY",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(element.primaryColorHex)
                        )
                        element.subtitle?.let { sub ->
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(element.primaryColorHex)
                            )
                        }
                    }
                }
            }
            CanvasElementType.WASHI_TAPE -> {
                Box(
                    modifier = Modifier
                        .background(Color(element.primaryColorHex).copy(alpha = 0.85f))
                        .border(1.dp, Color(0x33FFFFFF))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StickerDieCutWhite
                    )
                }
            }
            CanvasElementType.LOCATION_TAG -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(StickerDieCutWhite)
                        .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "Location",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                }
            }
            CanvasElementType.RATING_BADGE -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(element.primaryColorHex))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "5.0 ★",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StickerDieCutWhite
                    )
                }
            }
            CanvasElementType.MOOD_CHIP -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(StickerDieCutWhite)
                        .border(1.5.dp, Color(element.primaryColorHex), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "CHEF'S KISS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(element.primaryColorHex)
                    )
                }
            }
        }
    }
}

/**
 * Selection dashed outline with layer management (front/back/duplicate/delete) actions.
 */
@Composable
fun BoxScope.SelectionOverlay(
    onFront: () -> Unit,
    onBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .border(width = 2.dp, color = BiteyOrange, shape = RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xDD2B2120))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFront, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Front", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = "Back", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Duplicate", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
            }
        }
    }
}
