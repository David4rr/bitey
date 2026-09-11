package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.util.Locale

@Composable
fun RatingAndPriceCard(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    currency: String,
    priceString: String,
    onPriceChange: (String) -> Unit,
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
                Text(
                    text = "Rating",
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.inkPrimary
                )
                Text(
                    text = String.format(Locale.US, "%.1f / 5.0", rating),
                    style = MaterialTheme.typography.labelMedium,
                    color = BiteyOrange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive 5-star row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..5) {
                    val isStarFilled = rating >= i.toFloat()
                    IconButton(
                        onClick = { onRatingChange(i.toFloat()) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isStarFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Star $i",
                            tint = if (isStarFilled) BiteyOrange else theme.inkMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Paid,
                    contentDescription = null,
                    tint = BiteyMint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Price (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.inkPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = priceString,
                    onValueChange = onPriceChange,
                    placeholder = { Text("Amount (e.g. 65000)", color = theme.inkMuted) },
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}
