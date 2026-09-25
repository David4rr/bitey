package com.bitey.app.feature.journal.component

import android.content.Context
import android.hardware.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import kotlin.math.*

@Composable
fun GravitySticker(
    imageFiles: List<File>,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(1f) }

    DisposableEffect(lifecycleOwner, isGravityEnabled) {
        if (!isGravityEnabled) {
            onDispose { }
        } else {
            val sm = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            var isRegistered = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent?) {
                    val vals = e?.values ?: return
                    val targetX = (-vals[0] / 9.8f).coerceIn(-1f, 1f)
                    val targetY = (vals[1] / 9.8f).coerceIn(-1f, 1f)
                    val newX = tiltX + (targetX - tiltX) * 0.2f
                    val newY = tiltY + (targetY - tiltY) * 0.2f
                    if (abs(newX - tiltX) > 0.012f || abs(newY - tiltY) > 0.012f) {
                        tiltX = newX
                        tiltY = newY
                    }
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }

            fun register() {
                if (!isRegistered && sensor != null) {
                    sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                    isRegistered = true
                }
            }

            fun unregister() {
                if (isRegistered) {
                    sm?.unregisterListener(listener)
                    isRegistered = false
                }
            }

            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> register()
                    Lifecycle.Event.ON_PAUSE,
                    Lifecycle.Event.ON_STOP,
                    Lifecycle.Event.ON_DESTROY -> unregister()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                register()
            }

            onDispose {
                unregister()
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().onSizeChanged { containerSize = it }, contentAlignment = Alignment.Center) {
        val count = imageFiles.size
        val containerWidthDp = with(density) { containerSize.width.toDp() }
        val stickerSizeDp = when (count) {
            1 -> if (containerWidthDp > 0.dp) (containerWidthDp - 12.dp).coerceIn(240.dp, 290.dp) else 255.dp
            2 -> if (containerWidthDp > 0.dp) (containerWidthDp * 0.62f).coerceIn(160.dp, 190.dp) else 170.dp
            3 -> if (containerWidthDp > 0.dp) (containerWidthDp * 0.52f).coerceIn(135.dp, 165.dp) else 145.dp
            else -> if (containerWidthDp > 0.dp) (containerWidthDp * 0.44f).coerceIn(115.dp, 145.dp) else 125.dp
        }
        imageFiles.forEachIndexed { index, file ->
            val initialOffset = when (count) {
                1 -> Pair(0f, 0f)
                2 -> if (index == 0) Pair(-38f * density.density, 10f * density.density) else Pair(38f * density.density, -10f * density.density)
                3 -> when (index) {
                    0 -> Pair(0f, -32f * density.density)
                    1 -> Pair(-42f * density.density, 30f * density.density)
                    else -> Pair(42f * density.density, 26f * density.density)
                }
                else -> {
                    val a = (index * 2 * Math.PI / count) - Math.PI / 2
                    Pair((40.0 * cos(a) * density.density).toFloat(), (40.0 * sin(a) * density.density).toFloat())
                }
            }
            ModularStickerItem(
                file = file, index = index, totalCount = count, initialOffset = initialOffset,
                stickerSizeDp = stickerSizeDp, containerSize = containerSize,
                tiltX = tiltX, tiltY = tiltY, isGravityEnabled = isGravityEnabled,
                isStickerMode = isStickerMode, contentDescription = contentDescription, onClick = onClick
            )
        }
    }
}


@Composable
fun GravitySticker(
    imageFile: File,
    isStickerMode: Boolean,
    isGravityEnabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = GravitySticker(listOf(imageFile), isStickerMode, isGravityEnabled, contentDescription, onClick, modifier)
