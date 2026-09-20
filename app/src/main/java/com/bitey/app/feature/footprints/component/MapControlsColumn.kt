package com.bitey.app.feature.footprints.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
@Composable
fun MapControlsColumn(
    onRecenter: () -> Unit,
    showFitPins: Boolean,
    onFitPins: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Recenter GPS Location
        Box(
            modifier = Modifier
                .size(44.dp)
                .minimalistCard(cornerRadius = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onRecenter) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = "My Location",
                    tint = BiteyOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Fit All Visited Food Pins
        if (showFitPins) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .minimalistCard(cornerRadius = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onFitPins) {
                    Icon(
                        imageVector = Icons.Rounded.CenterFocusStrong,
                        contentDescription = "Fit All Pins",
                        tint = BiteyMint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
