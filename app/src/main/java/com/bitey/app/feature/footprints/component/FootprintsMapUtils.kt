package com.bitey.app.feature.footprints.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bitey.app.core.ui.theme.BiteyOrange
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

object FootprintsMapUtils {

    fun createCustomMarkerIcon(context: Context): BitmapDrawable {
        val sizePx = 100
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(50, 0, 0, 0)
        }
        canvas.drawCircle(sizePx / 2f, sizePx * 0.45f + 4f, sizePx * 0.38f, shadowPaint)

        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BiteyOrange.toArgb()
        }
        canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.38f, pinPaint)

        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.22f, innerPaint)

        val coreDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BiteyOrange.toArgb()
        }
        canvas.drawCircle(sizePx / 2f, sizePx * 0.45f, sizePx * 0.11f, coreDotPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun createUserLocationMarkerIcon(context: Context): BitmapDrawable {
        val sizePx = 64
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(60, 33, 150, 243)
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.46f, pulsePaint)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.30f, whitePaint)

        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(33, 150, 243)
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.22f, bluePaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        mapView.onResume()
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    return mapView
}

@Composable
private fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> Unit
            }
        }
    }
