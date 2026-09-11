package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bitey.app.core.image.CompositedSticker
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.entry.StickerStyle

@Composable
fun StickerReviewCard(
    sticker: CompositedSticker,
    originalImage: ProcessedImage,
    style: StickerStyle,
    onChangeStyle: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = theme.surfaceVariant,
            contentColor = BiteyOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BiteyOrange,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sticker View", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Original Photo", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .minimalistCard(cornerRadius = 24.dp, elevation = 2.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == 0) {
                AsyncImage(
                    model = sticker.file,
                    contentDescription = "Die-Cut Food Sticker",
                    modifier = Modifier.fillMaxSize().dieCutStickerEffect(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = originalImage.file,
                    contentDescription = "Original Photo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        StickerInfoCard(sticker = sticker, style = style)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteyOrange,
                contentColor = StickerDieCutWhite
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(text = "Save & Continue to Journal Entry", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onChangeStyle,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = theme.inkSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Change Sticker Style or Crop",
                style = MaterialTheme.typography.labelLarge,
                color = theme.inkPrimary
            )
        }
    }
}
