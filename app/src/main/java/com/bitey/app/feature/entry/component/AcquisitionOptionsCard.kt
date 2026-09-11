package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun AcquisitionOptionsCard(
    onPickFromGallery: () -> Unit,
    onCaptureLivePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "How would you like to add your food?",
                style = MaterialTheme.typography.titleMedium,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Device album does not require camera access",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPickFromGallery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange,
                    contentColor = StickerDieCutWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pick from Device Album",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onCaptureLivePhoto,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = BiteyMint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Take a Live Photo",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.inkPrimary
                )
            }
        }
    }
}
