package com.bitey.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitey.app.core.navigation.BiteyNavHost
import com.bitey.app.core.navigation.Screen
import com.bitey.app.core.ui.neumorphic.neumorphicRaised
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.camera.CameraCaptureBottomSheet
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
        Screen.Footprints.route,
        Screen.FateTable.route,
        Screen.Scrapbook.route
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
            paddingValues = innerPadding
        )
    }

    // Live Camera Preview Bottom Sheet Dialog
    if (showCameraSheet) {
        CameraCaptureBottomSheet(
            onDismissRequest = { showCameraSheet = false },
            onEntryCapturedAndPinned = { savedId, imagePath, stickerPath, timestamp, lat, lng ->
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

@Composable
private fun BiteyBottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    onOpenLiveCamera: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current

    // Clean, flat bottom bar surface without neumorphic card wrapper
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    if (screen == Screen.NewEntry) {
                        // Elevated center tactile button for rapid photo capture / bite adding
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .neumorphicRaised(cornerRadius = 25.dp, shadowOffset = 3.dp, blurRadius = 6.dp)
                                .clip(CircleShape)
                                .background(BiteyOrange)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    onOpenLiveCamera()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Capture Food",
                                tint = StickerDieCutWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        val selected = currentRoute == screen.route
                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (!selected) {
                                        onNavigateToRoute(screen.route)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (screen.icon != null) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) BiteyOrange else theme.inkMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) theme.inkPrimary else theme.inkMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
