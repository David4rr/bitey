package com.bitey.app.feature.camera.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun InteractiveStickerCutoutView(
    sourceFilePath: String?,
    stickerFilePath: String?,
    isProcessing: Boolean,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceScale = remember { Animatable(1f) }
    val wiggleRotation = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "scanProgress"
    )

    val hasSticker = stickerFilePath != null
    val stickerAlpha by animateFloatAsState(targetValue = if (hasSticker) 1f else 0f, animationSpec = tween(600), label = "stickerAlpha")
    val bgAlpha by animateFloatAsState(targetValue = if (hasSticker) 0f else 1f, animationSpec = tween(600), label = "bgAlpha")

    fun triggerInteractiveBounce() {
        coroutineScope.launch {
            bounceScale.animateTo(1.15f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            wiggleRotation.animateTo(6f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
            wiggleRotation.animateTo(-4f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
            wiggleRotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            bounceScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    LaunchedEffect(stickerFilePath) {
        if (stickerFilePath != null) triggerInteractiveBounce()
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF141416)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(260.dp).graphicsLayer {
                scaleX = bounceScale.value; scaleY = bounceScale.value; rotationZ = wiggleRotation.value
            }.pointerInput(Unit) { detectTapGestures { triggerInteractiveBounce() } },
            contentAlignment = Alignment.Center
        ) {
            // Background photo that dissolves away
            if (sourceFilePath != null && bgAlpha > 0.01f) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer { alpha = bgAlpha }
                        .clip(RoundedCornerShape(28.dp))
                        .border(2.dp, BiteyOrange.copy(alpha = 0.6f * bgAlpha), RoundedCornerShape(28.dp))
                ) {
                    AsyncImage(model = File(sourceFilePath), contentDescription = "Captured Dish", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.fillMaxSize().drawWithContent {
                        drawContent()
                        val yPos = size.height * scanProgress
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, BiteyOrange.copy(alpha = 0.45f), StickerDieCutWhite.copy(alpha = 0.8f), BiteyOrange.copy(alpha = 0.45f), Color.Transparent),
                                startY = yPos - 60f, endY = yPos + 60f
                            ),
                            blendMode = BlendMode.Screen
                        )
                    })
                }
            }

            // Cutout die-cut sticker that fades in and bounces
            if (stickerFilePath != null && stickerAlpha > 0.01f) {
                AsyncImage(
                    model = File(stickerFilePath), contentDescription = "Interactive Die-Cut Sticker",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer { alpha = stickerAlpha }
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            if (isProcessing) {
                CircularProgressIndicator(color = BiteyOrange, strokeWidth = 2.5.dp, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isProcessing) statusText else "Tap to bounce sticker!",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = StickerDieCutWhite.copy(alpha = 0.9f), fontSize = 14.sp
            )
        }
    }
}
