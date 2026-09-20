package com.bitey.app.feature.footprints.component

import android.content.Context
import android.view.MotionEvent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import org.osmdroid.views.CustomZoomButtonsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    fun applyAestheticMapStyle(mapView: MapView, isDark: Boolean) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        if (isDark) {
            val darkMatrix = ColorMatrix().apply {
                set(floatArrayOf(
                    -0.80f,  0.00f,  0.00f, 0f, 230f,
                     0.00f, -0.80f,  0.00f, 0f, 230f,
                     0.00f,  0.00f, -0.80f, 0f, 230f,
                     0.00f,  0.00f,  0.00f, 1f,   0f
                ))
            }
            mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(darkMatrix))
        } else {
            val warmPastelMatrix = ColorMatrix().apply {
                set(floatArrayOf(
                    0.65f, 0.25f, 0.10f, 0f, 20f,
                    0.20f, 0.65f, 0.15f, 0f, 18f,
                    0.15f, 0.20f, 0.65f, 0f, 12f,
                    0.00f, 0.00f, 0.00f, 1f,  0f
                ))
            }
            mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(warmPastelMatrix))
        }
    }


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
fun rememberMapViewWithLifecycle(isDark: Boolean = false): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            minZoomLevel = 3.0
            maxZoomLevel = 20.0
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            tileProvider.clearTileCache()
            setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }
    }

    LaunchedEffect(isDark) {
        FootprintsMapUtils.applyAestheticMapStyle(mapView, isDark)
        mapView.invalidate()
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
