package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.StickerDieCutWhite

/**
 * Selection dashed outline with layer management (front/back/duplicate/delete)
 * and interactive rotation controls (tap +45deg or drag-to-rotate handle).
 */
@Composable
fun BoxScope.SelectionOverlay(
    onFront: () -> Unit,
    onBack: () -> Unit,
    onDuplicate: () -> Unit,
    onRotate: (Float) -> Unit,
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
            IconButton(onClick = { onRotate(45f) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Rounded.RotateRight, contentDescription = "Rotate", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Duplicate", tint = StickerDieCutWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFE57373), modifier = Modifier.size(14.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 10.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(BiteyOrange)
                .clickable { onRotate(45f) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onRotate(dragAmount.x * 0.6f + dragAmount.y * 0.6f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.RotateRight,
                contentDescription = "Rotate Handle",
                tint = StickerDieCutWhite,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
