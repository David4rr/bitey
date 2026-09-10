package com.bitey.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Journal : Screen("journal", "Journal", Icons.AutoMirrored.Rounded.MenuBook)
    data object Footprints : Screen("footprints", "Footprints", Icons.Rounded.Place)
    data object NewEntry : Screen("new_entry", "Capture", Icons.Rounded.CameraAlt)
    data object FateTable : Screen("fate_table", "Fate's Table", Icons.Rounded.AutoAwesome)
    data object Scrapbook : Screen("scrapbook", "Scrapbook", Icons.Rounded.Collections)

    companion object {
        val bottomNavItems: List<Screen> by lazy {
            listOf(
                Journal,
                Footprints,
                NewEntry,
                FateTable,
                Scrapbook
            )
        }
    }
}
