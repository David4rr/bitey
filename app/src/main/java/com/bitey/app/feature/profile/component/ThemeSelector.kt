package com.bitey.app.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.ThemeMode

@Composable
fun ThemeSelector(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = theme.inkPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionPill(
                mode = ThemeMode.AUTO,
                isSelected = currentThemeMode == ThemeMode.AUTO,
                icon = Icons.Rounded.BrightnessAuto,
                onClick = { onThemeModeChanged(ThemeMode.AUTO) },
                modifier = Modifier.weight(1f)
            )
            ThemeOptionPill(
                mode = ThemeMode.LIGHT,
                isSelected = currentThemeMode == ThemeMode.LIGHT,
                icon = Icons.Rounded.LightMode,
                onClick = { onThemeModeChanged(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOptionPill(
                mode = ThemeMode.DARK,
                isSelected = currentThemeMode == ThemeMode.DARK,
                icon = Icons.Rounded.DarkMode,
                onClick = { onThemeModeChanged(ThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOptionPill(
    mode: ThemeMode,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BiteyOrange else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) BiteyOrange else theme.border,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = mode.label,
                tint = if (isSelected) Color.White else theme.inkSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = mode.label.split(" ").first(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else theme.inkSecondary,
                fontSize = 11.sp
            )
        }
    }
}
