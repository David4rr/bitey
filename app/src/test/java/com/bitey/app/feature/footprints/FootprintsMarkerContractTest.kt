package com.bitey.app.feature.footprints

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FootprintsMarkerContractTest {

    @Test
    fun footprintsScreen_usesDishImageMarkerContract() {
        val screenFile = File("src/main/java/com/bitey/app/feature/footprints/FootprintsScreen.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/FootprintsScreen.kt")
        assertTrue("FootprintsScreen exists", screenFile.exists())
        val screenText = screenFile.readText()

        assertTrue(
            "FootprintsScreen imports and uses DishMarkerRenderer",
            screenText.contains("DishMarkerRenderer.getOrCreateDishMarkerIcon")
        )
        assertTrue(
            "FootprintsScreen anchors marker correctly at center bottom",
            screenText.contains("Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM")
        )
        assertTrue(
            "FootprintsScreen loads dish markers off main thread using Dispatchers.IO",
            screenText.contains("withContext(Dispatchers.IO)")
        )
        assertFalse(
            "FootprintsScreen no longer uses generic static customMarkerIcon for dishes",
            screenText.contains("val customMarkerIcon = remember")
        )
    }

    @Test
    fun dishMarkerRenderer_structureAndCachingContract() {
        val rendererFile = File("src/main/java/com/bitey/app/feature/footprints/component/DishMarkerRenderer.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/component/DishMarkerRenderer.kt")
        assertTrue("DishMarkerRenderer exists", rendererFile.exists())
        val rendererText = rendererFile.readText()

        assertTrue(
            "DishMarkerRenderer has LRU cache",
            rendererText.contains("LruCache<String, BitmapDrawable>")
        )
        assertTrue(
            "DishMarkerRenderer supports downsampling via calculateInSampleSize",
            rendererText.contains("ImageTransformUtils.calculateInSampleSize")
        )
        assertTrue(
            "DishMarkerRenderer draws die-cut pin path",
            rendererText.contains("val pinPath = Path()")
        )
        assertTrue(
            "DishMarkerRenderer supports sticker mode aspect fit",
            rendererText.contains("isSticker") && rendererText.contains("canvas.clipPath(clipPath)")
        )
        assertTrue(
            "DishMarkerRenderer provides cache clear method",
            rendererText.contains("fun clearCache()")
        )
    }

    @Test
    fun newEntryViewModel_capturesLocationForDishContract() {
        val vmFile = File("src/main/java/com/bitey/app/feature/entry/NewEntryViewModel.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/entry/NewEntryViewModel.kt")
        assertTrue("NewEntryViewModel exists", vmFile.exists())
        val vmText = vmFile.readText()

        assertTrue(
            "Proactively fetches location on init",
            vmText.contains("init {") && vmText.contains("fetchLocation()")
        )
        assertTrue(
            "Extracts EXIF location from picked images",
            vmText.contains("exifMetadata.latitude") && vmText.contains("exifMetadata.longitude")
        )
        assertTrue(
            "Provides fallback location acquisition before saving",
            vmText.contains("locationProvider.getCurrentLocation")
        )
        assertTrue(
            "Saves latitude and longitude to PlateEntryEntity",
            vmText.contains("latitude = lat") && vmText.contains("longitude = lng")
        )
    }

    @Test
    fun footprintsMap_aestheticStylingAndNoSquareZoomContract() {
        val utilsFile = File("src/main/java/com/bitey/app/feature/footprints/component/FootprintsMapUtils.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/component/FootprintsMapUtils.kt")
        assertTrue("FootprintsMapUtils exists", utilsFile.exists())
        val utilsText = utilsFile.readText()

        assertTrue(
            "Disables OSMDroid default square zoom buttons",
            utilsText.contains("zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)")
        )
        assertTrue(
            "Uses keyless Mapnik tile source",
            utilsText.contains("TileSourceFactory.MAPNIK")
        )
        assertFalse(
            "Does not use Carto which requires API key",
            utilsText.contains("CARTO_VOYAGER")
        )
        assertTrue(
            "Applies aesthetic warm/dark map color filtering",
            utilsText.contains("applyAestheticMapStyle")
        )

        val controlsFile = File("src/main/java/com/bitey/app/feature/footprints/component/MapControlsColumn.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/component/MapControlsColumn.kt")
        assertTrue("MapControlsColumn exists", controlsFile.exists())
        val controlsText = controlsFile.readText()

        assertFalse("Removed button-based Zoom In", controlsText.contains("onZoomIn"))
        assertFalse("Removed button-based Zoom Out", controlsText.contains("onZoomOut"))
        assertTrue("Retained circular tactile Recenter button", controlsText.contains("onRecenter"))
        assertTrue("Enables multi-touch controls for finger gestures", utilsText.contains("setMultiTouchControls(true)"))
        assertTrue("Prevents touch interception on multi-touch", utilsText.contains("requestDisallowInterceptTouchEvent"))
    }

    @Test
    fun footprintsScreen_emptyStateNotInCenterAndAutoResolvesContract() {
        val screenFile = File("src/main/java/com/bitey/app/feature/footprints/FootprintsScreen.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/FootprintsScreen.kt")
        val screenText = screenFile.readText()

        assertFalse(
            "Empty state is NOT aligned to Center blocking map",
            screenText.contains("FootprintsEmptyState(modifier = Modifier.align(Alignment.Center))")
        )
        assertFalse(
            "Carousel removed to keep map clean",
            screenText.contains("MapSpotCarouselItem")
        )

        val vmFile = File("src/main/java/com/bitey/app/feature/footprints/FootprintsViewModel.kt").takeIf { it.exists() }
            ?: File("app/src/main/java/com/bitey/app/feature/footprints/FootprintsViewModel.kt")
        val vmText = vmFile.readText()

        assertTrue(
            "FootprintsViewModel auto-resolves missing dish locations so existing dishes appear on map",
            vmText.contains("autoResolveMissingLocations")
        )
    }
}
