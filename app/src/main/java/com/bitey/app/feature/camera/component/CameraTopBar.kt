package com.bitey.app.feature.camera.component

import androidx.camera.core.ImageCapture
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.feature.camera.CameraRatio

@Composable
fun CameraTopBar(
    selectedRatio: CameraRatio,
    onRatioSelected: (CameraRatio) -> Unit,
    flashMode: Int,
    onFlashToggle: () -> Unit,
    onClose: () -> Unit,
    applyStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (applyStatusBarPadding) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close / Dismiss button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close Camera",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Minimalist Aspect Ratio Selector (1:1 vs 4:3)
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraRatio.entries.forEach { ratio ->
                val isSelected = selectedRatio == ratio
                val bgColor by animateColorAsState(
                    if (isSelected) Color.White.copy(alpha = 0.22f) else Color.Transparent,
                    label = "ratioBg"
                )
                val textColor by animateColorAsState(
                    if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
                    label = "ratioText"
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onRatioSelected(ratio) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ratio.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = textColor
                    )
                }
            }
        }

        // Flash Mode Toggle
        val isFlashActive = flashMode != ImageCapture.FLASH_MODE_OFF
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .border(
                    0.5.dp,
                    if (isFlashActive) BiteyWarmYellow.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                    CircleShape
                )
        ) {
            val flashIcon = when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                ImageCapture.FLASH_MODE_AUTO -> Icons.Rounded.FlashAuto
                else -> Icons.Rounded.FlashOff
            }
            Icon(
                imageVector = flashIcon,
                contentDescription = "Toggle Flash",
                tint = if (isFlashActive) BiteyWarmYellow else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
