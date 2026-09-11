package com.bitey.app.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.PhotoMode
import java.io.File

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
    capturedDishes: List<java.io.File> = emptyList(),
    onDoneDishByDish: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Oneshot vs Dish-by-dish
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(PhotoMode.ONESHOT to "Oneshot", PhotoMode.DISH_BY_DISH to "Dish-by-dish").forEach { (mode, label) ->
                val isSelected = photoMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BiteyOrange else Color.Transparent)
                        .clickable { onPhotoModeChange(mode) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                        color = if (isSelected) StickerDieCutWhite else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Dish-by-dish captured shelf & Done button
        if (photoMode == PhotoMode.DISH_BY_DISH && capturedDishes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                itemsIndexed(capturedDishes) { index, file ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, BiteyOrange, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(model = file, contentDescription = "Dish ${index + 1}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(topStart = 6.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "#${index + 1}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onDoneDishByDish,
                colors = ButtonDefaults.buttonColors(containerColor = BiteyOrange, contentColor = StickerDieCutWhite),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(text = "Done (${capturedDishes.size} dish${if (capturedDishes.size > 1) "es" else ""}) →", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                            .clickable { onCapture() }
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
