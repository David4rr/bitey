package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.util.Locale

@Composable
fun LocationInputCard(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    latitude: Double?,
    longitude: Double?,
    isLocating: Boolean,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = BiteyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Establishment & Location",
                        style = MaterialTheme.typography.titleSmall,
                        color = theme.inkPrimary
                    )
                }

                IconButton(
                    onClick = onRefreshLocation,
                    enabled = !isLocating,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = BiteyOrange
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Refresh Location",
                            tint = theme.inkSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                placeholder = { Text("Restaurant or street name", color = theme.inkMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BiteyOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = theme.surfaceVariant,
                    unfocusedContainerColor = theme.surfaceVariant,
                    focusedTextColor = theme.inkPrimary,
                    unfocusedTextColor = theme.inkPrimary
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )

            if (latitude != null && longitude != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = String.format(Locale.US, "GPS: %.4f, %.4f", latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.inkMuted
                )
            }
        }
    }
}

@Composable
fun PalateNotesCard(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNeumorphicTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimalistCard(cornerRadius = 20.dp, elevation = 1.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Palate Impressions",
                style = MaterialTheme.typography.titleSmall,
                color = theme.inkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Textures, aroma, dining companions, or memory",
                style = MaterialTheme.typography.bodySmall,
                color = theme.inkMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Crispy skin, rich broth with hint of lime...", color = theme.inkMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BiteyOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = theme.surfaceVariant,
                    unfocusedContainerColor = theme.surfaceVariant,
                    focusedTextColor = theme.inkPrimary,
                    unfocusedTextColor = theme.inkPrimary
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 5
            )
        }
    }
}
