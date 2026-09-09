package com.bitey.app.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.neumorphic.neumorphicCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.BiteyWarmYellow
import com.bitey.app.core.ui.theme.InkPrimary
import com.bitey.app.core.ui.theme.InkSecondary
import com.bitey.app.core.ui.theme.SoftBackground
import com.bitey.app.core.ui.theme.StickerDieCutWhite

@Composable
fun JournalScreen(
    onNavigateToNewEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Header with cute badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bitey",
                    style = MaterialTheme.typography.headlineLarge,
                    color = BiteyOrange
                )
                Text(
                    text = "Stick your yummy memories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .neumorphicCard(cornerRadius = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Favorites",
                    tint = BiteyOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Welcome / Empty State Scrapbook Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicCard(cornerRadius = 24.dp, elevation = 6.dp)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Die-cut sticker sample preview
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .dieCutStickerEffect(cornerRadius = 48.dp)
                        .background(BiteyWarmYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = "Food Sticker",
                        tint = StickerDieCutWhite,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your journal is waiting",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "What did you savor today? Pick a photo from your album or take a snap to cut out your first food sticker.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToNewEntry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiteyOrange,
                        contentColor = StickerDieCutWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paste Today's Bite",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
