package com.bitey.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    var cameraAnchorOffset by remember { mutableStateOf<Offset?>(null) }

    // Show bottom bar on primary browsing tabs; kept mounted under overlay for seamless circular collapse
    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Journal.route,
        Screen.Profile.route
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                        onOpenLiveCamera = { anchor ->
                            cameraAnchorOffset = anchor
                            showCameraSheet = true
                        }
                    )
                }
            }
        ) { innerPadding ->
            BiteyNavHost(
                navController = navController,
                paddingValues = innerPadding,
                onOpenLiveCamera = {
                    cameraAnchorOffset = null
                    showCameraSheet = true
                }
            )
        }

        // Live Camera Preview with Circular Reveal Animation
        if (showCameraSheet) {
            CameraCaptureBottomSheet(
                anchorOffset = cameraAnchorOffset,
                onDismissRequest = { showCameraSheet = false }
            )
        }
    }
}
