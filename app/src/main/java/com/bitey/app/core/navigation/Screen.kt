package com.bitey.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val iconResId: Int? = null
) {
    data object Journal : Screen("journal", "Journal", com.bitey.app.R.drawable.ic_doodle_menu_book)
    data object Footprints : Screen("footprints", "Footprints", com.bitey.app.R.drawable.ic_doodle_place) {
        const val ROUTE_WITH_ARGS = "footprints?targetEntryId={targetEntryId}"

        fun createRoute(targetEntryId: Long? = null): String {
            return if (targetEntryId != null) "footprints?targetEntryId=$targetEntryId" else "footprints"
        }
    }
    data object NewEntry : Screen("new_entry", "Capture", com.bitey.app.R.drawable.ic_doodle_camera_alt)
    data object FateTable : Screen("fate_table", "Fate's Table", com.bitey.app.R.drawable.ic_doodle_auto_awesome)
    data object Scrapbook : Screen("scrapbook", "Scrapbook", com.bitey.app.R.drawable.ic_doodle_collections)
    data object Profile : Screen("profile", "Profile", com.bitey.app.R.drawable.ic_doodle_person)
    data object EntryEditor : Screen("entry_editor", "Entry Editor") {
        const val ROUTE_WITH_ARGS = "entry_editor?imagePath={imagePath}&stickerPath={stickerPath}&timestamp={timestamp}&lat={lat}&lng={lng}"

        fun createRoute(
            imagePath: String,
            stickerPath: String? = null,
            timestamp: Long = System.currentTimeMillis(),
            latitude: Double? = null,
            longitude: Double? = null
        ): String {
            val encodedImage = java.net.URLEncoder.encode(imagePath, java.nio.charset.StandardCharsets.UTF_8.toString())
            val encodedSticker = stickerPath?.let { java.net.URLEncoder.encode(it, java.nio.charset.StandardCharsets.UTF_8.toString()) } ?: ""
            val latStr = latitude?.toString() ?: ""
            val lngStr = longitude?.toString() ?: ""
            return "entry_editor?imagePath=$encodedImage&stickerPath=$encodedSticker&timestamp=$timestamp&lat=$latStr&lng=$lngStr"
        }
    }

    companion object {
        val bottomNavItems: List<Screen> by lazy {
            listOf(
                Journal,
                Profile
            )
        }
    }
}
