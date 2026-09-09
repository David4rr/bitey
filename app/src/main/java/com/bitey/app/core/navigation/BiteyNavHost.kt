package com.bitey.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            FateTableScreen()
        }

        composable(Screen.Scrapbook.route) {
            ScrapbookScreen()
        }

        composable(Screen.NewEntry.route) {
            NewEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
