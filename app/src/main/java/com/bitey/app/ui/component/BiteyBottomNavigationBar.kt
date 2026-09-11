package com.bitey.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.navigation.Screen
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun BiteyBottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    onOpenLiveCamera: () -> Unit
) {
    val theme = LocalNeumorphicTheme.current

    // Compact iPhone-style floating dock: Pill (Journal + Profile) + Separate Camera Button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // The Navigation Pill (Journal + Profile only)
            Surface(
                shape = CircleShape,
                color = theme.surface,
                border = BorderStroke(1.dp, theme.border),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .width(152.dp)
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        val animatedBgColor by animateColorAsState(
                            targetValue = if (selected) BiteyOrange.copy(alpha = 0.12f) else Color.Transparent,
                            label = "navBg"
                        )
                        val animatedIconColor by animateColorAsState(
                            targetValue = if (selected) BiteyOrange else theme.inkMuted,
                            label = "navIcon"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(animatedBgColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!selected) {
                                        onNavigateToRoute(screen.route)
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            if (screen.icon != null) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = animatedIconColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) BiteyOrange else theme.inkMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Separate Camera Capture Button (outside the pill)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "cameraScale"
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(BiteyOrange)
                    .border(1.dp, theme.border, CircleShape)
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
