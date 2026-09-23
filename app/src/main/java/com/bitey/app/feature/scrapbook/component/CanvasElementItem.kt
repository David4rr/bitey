package com.bitey.app.feature.scrapbook.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.CherryBombOneFontFamily
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.scrapbook.ScrapbookThemeUtils
import com.bitey.app.feature.scrapbook.model.CanvasElement
import com.bitey.app.feature.scrapbook.model.CanvasElementType
import java.io.File

/**
 * Renders individual canvas elements with authentic typography, die-cut food sticker effects,
 * and custom accessory cards.
 */
@Composable
fun CanvasElementItem(
    element: CanvasElement,
    backgroundColorHex: Long = 0xFFF4F1EA,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (element.type) {
            CanvasElementType.FOOD_STICKER -> {
                val file = element.imagePath?.let { File(it) }
                if (file != null && file.exists()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = element.text,
                                modifier = Modifier.fillMaxSize().dieCutStickerEffect(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        element.text?.takeIf { it.isNotBlank() }?.let { title ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontFamily = CherryBombOneFontFamily,
                                fontSize = 13.sp,
                                color = ScrapbookThemeUtils.getCanvasPrimaryTextColor(backgroundColorHex),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            CanvasElementType.DATE_STAMP -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = element.text ?: "TODAY",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(element.primaryColorHex)
                        )
                        element.subtitle?.let { sub ->
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(element.primaryColorHex)
                            )
                        }
                    }
                }
            }
            CanvasElementType.WASHI_TAPE -> {
                Box(
                    modifier = Modifier
                        .background(Color(element.primaryColorHex).copy(alpha = 0.85f))
                        .border(1.dp, Color(0x33FFFFFF))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StickerDieCutWhite
                    )
                }
            }
            CanvasElementType.LOCATION_TAG -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(StickerDieCutWhite)
                        .border(2.dp, Color(element.primaryColorHex), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "Location",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                }
            }
            CanvasElementType.RATING_BADGE -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(element.primaryColorHex))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "5.0 ★",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StickerDieCutWhite
                    )
                }
            }
            CanvasElementType.MOOD_CHIP -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(StickerDieCutWhite)
                        .border(1.5.dp, Color(element.primaryColorHex), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = element.text ?: "CHEF'S KISS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(element.primaryColorHex)
                    )
                }
            }
        }
    }
}
