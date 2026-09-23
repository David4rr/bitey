package com.bitey.app.feature.journal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bitey.app.core.image.CropTransparentTransformation
import com.bitey.app.core.ui.neumorphic.dieCutStickerEffect
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue

@Composable
fun StickerCarousel(
    stickers: List<String>,
    initialIndex: Int = 0,
    isStickerMode: Boolean = true,
    onStickerClick: (index: Int) -> Unit = {},
    onActiveIndexChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (stickers.isEmpty()) return

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (stickers.size - 1).coerceAtLeast(0)),
        pageCount = { stickers.size }
    )
    val theme = LocalNeumorphicTheme.current
    val context = LocalContext.current

    LaunchedEffect(pagerState.currentPage) {
        onActiveIndexChange?.invoke(pagerState.currentPage)
    }

    LaunchedEffect(initialIndex) {
        if (pagerState.currentPage != initialIndex && initialIndex in stickers.indices) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    Box(
        modifier = modifier.size(165.dp),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 14.dp),
            pageSpacing = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val fraction = (1f - pageOffset).coerceIn(0f, 1f)
            val scale = lerp(0.80f, 1.0f, fraction)
            val alpha = lerp(0.60f, 1.0f, fraction)

            val file = remember(stickers, page) { File(stickers[page]) }
            val imageRequest = remember(file, isStickerMode) {
                ImageRequest.Builder(context)
                    .data(file)
                    .apply {
                        if (isStickerMode) {
                            transformations(CropTransparentTransformation())
                        }
                    }
                    .crossfade(true)
                    .build()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onStickerClick(page) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Sticker #${page + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isStickerMode) Modifier.dieCutStickerEffect()
                            else Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                        ),
                    contentScale = if (isStickerMode) ContentScale.Fit else ContentScale.Crop
                )
            }
        }

        // Indicator dots positioned higher, neatly inside the carousel container
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            stickers.indices.forEach { idx ->
                val isActive = idx == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (isActive) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (isActive) BiteyOrange else theme.inkMuted.copy(alpha = 0.35f))
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                        }
                )
            }
        }
    }
}
