package com.bitey.app.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CircularProgressIndicator
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
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.PhotoMode

@Composable
fun CameraBottomBar(
    photoMode: PhotoMode,
    onPhotoModeChange: (PhotoMode) -> Unit,
    keepOriginal: Boolean,
    onKeepOriginalToggle: () -> Unit,
    isCapturing: Boolean,
    onCapture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onPickFromFile: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Photo Modes (Whole Dish vs Per Dish) & Keep Original Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo Mode Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PhotoMode.entries.forEach { mode ->
                    val isSelected = photoMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (isSelected) BiteyOrange else Color.Transparent)
                            .clickable { onPhotoModeChange(mode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) StickerDieCutWhite else Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Keep Original Photo Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (keepOriginal) BiteyOrange.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                    .clickable { onKeepOriginalToggle() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    tint = if (keepOriginal) BiteyOrange else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (keepOriginal) "Keep Orig" else "Sticker Only",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (keepOriginal) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Capture Bar: Gallery Picker | Shutter Button | Camera Flip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pick from File / Gallery Option
            if (onPickFromFile != null) {
                IconButton(
                    onClick = onPickFromFile,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = "Pick from device files",
                        tint = StickerDieCutWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(50.dp))
            }

            // Tactile Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, StickerDieCutWhite, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = BiteyOrange,
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BiteyOrange)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    onCapture()
                                }
                            }
                    )
                }
            }

            // Switch Camera Lens (Back / Front)
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = StickerDieCutWhite
                )
            }
        }
    }
}
