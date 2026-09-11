package com.bitey.app.feature.camera.component

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CameraRatio

@Composable
fun CameraTopBar(
    selectedRatio: CameraRatio,
    onRatioSelected: (CameraRatio) -> Unit,
    flashMode: Int,
    onFlashToggle: () -> Unit,
    onClose: () -> Unit,
    applyStatusBarPadding: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (applyStatusBarPadding) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close / Dismiss button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close Camera",
                tint = StickerDieCutWhite
            )
        }

        // Aspect Ratio Selector (1:1 vs 4:3)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraRatio.entries.forEach { ratio ->
                val isSelected = selectedRatio == ratio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BiteyOrange else Color.Transparent)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .pointerInput(ratio) {
                            detectTapGestures { onRatioSelected(ratio) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ratio.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) StickerDieCutWhite else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Flash Mode Toggle
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            val flashIcon = when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                ImageCapture.FLASH_MODE_AUTO -> Icons.Rounded.FlashAuto
                else -> Icons.Rounded.FlashOff
            }
            Icon(
                imageVector = flashIcon,
                contentDescription = "Toggle Flash",
                tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) BiteyWarmYellow else StickerDieCutWhite
            )
        }
    }
}
