package com.bitey.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitey.app.core.navigation.BiteyNavHost
import com.bitey.app.core.navigation.Screen
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.feature.camera.CameraCaptureBottomSheet
import com.bitey.app.ui.component.BiteyBottomNavigationBar

@Composable
fun BiteyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val theme = LocalNeumorphicTheme.current

    var showCameraSheet by remember { mutableStateOf(false) }

    // Show bottom bar on primary browsing tabs; hide during full-screen capture flow
    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Journal.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = theme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                BiteyBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenLiveCamera = {
                        showCameraSheet = true
                    }
                )
            }
        }
    ) { innerPadding ->
        BiteyNavHost(
            navController = navController,
            paddingValues = innerPadding,
            onOpenLiveCamera = { showCameraSheet = true }
        )
    }

    // Live Camera Preview Bottom Sheet Dialog
    if (showCameraSheet) {
        CameraCaptureBottomSheet(
            onDismissRequest = { showCameraSheet = false },
            onEntryCapturedAndPinned = { _, imagePath, stickerPath, timestamp, lat, lng ->
                showCameraSheet = false
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
}
