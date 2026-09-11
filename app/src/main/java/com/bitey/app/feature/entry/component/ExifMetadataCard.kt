package com.bitey.app.feature.entry.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitey.app.core.image.ProcessedImage
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyMint
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExifMetadataCard(
    processedImage: ProcessedImage,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = BiteyMint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memory & EXIF Extracted",
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.inkPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val dateText = processedImage.exifMetadata.capturedAtMillis?.let {
                SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm", Locale.US).format(Date(it))
            } ?: "Captured Just Now"

            MetadataRow(
                icon = Icons.Rounded.CalendarToday,
                title = "Timestamp",
                value = dateText
            )

            Spacer(modifier = Modifier.height(8.dp))

            val locationText = if (processedImage.exifMetadata.latitude != null && processedImage.exifMetadata.longitude != null) {
                String.format(
                    Locale.US,
                    "%.4f, %.4f (Ready for Geocoding)",
                    processedImage.exifMetadata.latitude,
                    processedImage.exifMetadata.longitude
                )
            } else {
                "Location pinned upon capture"
            }

            MetadataRow(
                icon = Icons.Rounded.LocationOn,
                title = "Location",
                value = locationText
            )

            Spacer(modifier = Modifier.height(8.dp))

            val sizeKb = processedImage.sizeBytes / 1024
            MetadataRow(
                icon = Icons.Rounded.Info,
                title = "Format",
                value = "${processedImage.width} x ${processedImage.height} px • ${sizeKb} KB WebP"
            )
        }
    }
}
