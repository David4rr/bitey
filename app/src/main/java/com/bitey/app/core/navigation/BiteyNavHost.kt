package com.bitey.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bitey.app.feature.entry.EntryEditorScreen
import com.bitey.app.feature.entry.NewEntryScreen
import com.bitey.app.feature.fatetable.FateTableScreen
import com.bitey.app.feature.footprints.FootprintsScreen
import com.bitey.app.feature.journal.JournalScreen
import com.bitey.app.feature.scrapbook.ScrapbookScreen
@Composable
fun BiteyNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Journal.route,
        modifier = modifier.padding(paddingValues)
    ) {
        composable(Screen.Journal.route) {
            JournalScreen(
                onNavigateToNewEntry = {
                    navController.navigate(Screen.NewEntry.route)
                }
            )
        }

        composable(Screen.Footprints.route) {
            FootprintsScreen()
        }

        composable(Screen.FateTable.route) {
            FateTableScreen(
                onNavigateToNewEntry = {
                    navController.navigate(Screen.NewEntry.route)
                }
            )
        }

        composable(Screen.Scrapbook.route) {
            ScrapbookScreen()
        }

        composable(Screen.NewEntry.route) {
            NewEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { imagePath, stickerPath, timestamp, lat, lng ->
                    navController.navigate(
                        Screen.EntryEditor.createRoute(
                            imagePath = imagePath,
                            stickerPath = stickerPath,
                            timestamp = timestamp,
                            latitude = lat,
                            longitude = lng
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.EntryEditor.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType },
                navArgument("stickerPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("timestamp") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("lat") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lng") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            EntryEditorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEntrySaved = {
                    navController.navigate(Screen.Journal.route) {
                        popUpTo(Screen.Journal.route) { inclusive = false }
                    }
                }
            )
        }
    }
}
