package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun ImagePreviewCard(
    processedImage: ProcessedImage,
    onGenerateSticker: () -> Unit,
    onOpenStyleOptions: () -> Unit,
    onContinueWithoutSticker: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = processedImage.file,
                contentDescription = "Processed Food Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ExifMetadataCard(processedImage = processedImage)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGenerateSticker,
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
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Generate Food Sticker (AI)",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onOpenStyleOptions,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Crop,
                    contentDescription = null,
                    tint = theme.inkSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Crop Styles",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.inkPrimary
                )
            }

            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = theme.inkSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Photo",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.inkPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onContinueWithoutSticker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip sticker & continue with original photo",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkSecondary
            )
        }
    }
}
