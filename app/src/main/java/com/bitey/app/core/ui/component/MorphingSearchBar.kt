package com.bitey.app.core.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * High-performance Morphing Search Bar:
 * Morphs between title/actions and expanded input field across multiple consumers.
 * State reads are strictly deferred to Layout/Draw phases to guarantee 0 recompositions during animation.
 */
@Composable
fun MorphingSearchBar(
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search dishes, places, tags...",
    actions: (@Composable RowScope.() -> Unit)? = null,
    onSearch: ((String) -> Unit)? = null,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val theme = LocalNeumorphicTheme.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val closeSearch: () -> Unit = remember(onSearchActiveChange, onSearchQueryChange) {
        { focusManager.clearFocus(); keyboard?.hide(); onSearchActiveChange(false); onSearchQueryChange("") }
    }

    BackHandler(enabled = isSearchActive, onBack = closeSearch)

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            try { focusRequester.requestFocus(); keyboard?.show() } catch (_: Exception) {}
        } else {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    val progressState = animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "SearchMorph"
    )

    Box(
        modifier = modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Collapsed Left Title and Actions Row: Defers alpha & translation to Draw phase
        Row(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val p = progressState.value.coerceIn(0f, 1f)
                alpha = (1f - p * 2.5f).coerceIn(0f, 1f)
                translationX = -24.dp.toPx() * p
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f, fill = false).padding(end = if (actions != null) 8.dp else 48.dp),
                contentAlignment = Alignment.CenterStart
            ) { titleContent() }
            if (actions != null) {
                Row(
                    modifier = Modifier.padding(end = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actions
                )
            }
        }

        val rCollapsed = 20.dp
        val rExpanded = 12.dp
        val surfaceColor = theme.surface

        // Morphing Search Box: Layout & Draw phases handle all frame-by-frame interpolation
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val p = progressState.value.coerceIn(0f, 1f)
                    val w = lerp(40.dp.roundToPx().toFloat(), constraints.maxWidth.toFloat(), p).roundToInt()
                    val h = lerp(40.dp.toPx(), 44.dp.toPx(), p).roundToInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h))
                    layout(w, h) { placeable.place(0, 0) }
                }
                .drawBehind {
                    val p = progressState.value.coerceIn(0f, 1f)
                    if (p > 0.01f) {
                        val cr = CornerRadius(lerp(rCollapsed.toPx(), rExpanded.toPx(), p))
                        drawRoundRect(color = surfaceColor, cornerRadius = cr)
                        drawRoundRect(color = BiteyOrange.copy(alpha = p), cornerRadius = cr, style = Stroke(width = 1.dp.toPx()))
                    }
                }
                .clickable(enabled = !isSearchActive, interactionSource = interactionSource, indication = null) {
                    onSearchActiveChange(true)
                },
            contentAlignment = if (isSearchActive) Alignment.CenterStart else Alignment.Center
        ) {
            // Collapsed Icon: Crossfades out in Draw phase
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = if (searchQuery.isNotEmpty()) BiteyOrange else theme.inkPrimary,
                modifier = Modifier.size(22.dp).graphicsLayer {
                    val p = progressState.value.coerceIn(0f, 1f)
                    alpha = (1f - p * 3f).coerceIn(0f, 1f)
                }
            )

            // Expanded Input Row: Crossfades in during Draw phase without triggering recomposition
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp).graphicsLayer {
                    val p = progressState.value.coerceIn(0f, 1f)
                    alpha = ((p - 0.2f) * 1.25f).coerceIn(0f, 1f)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    enabled = isSearchActive,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = theme.inkPrimary),
                    cursorBrush = SolidColor(BiteyOrange),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke(searchQuery); keyboard?.hide() }),
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(placeholderText, style = MaterialTheme.typography.bodyMedium, color = theme.inkMuted, maxLines = 1)
                        }
                        inner()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }, enabled = isSearchActive, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = theme.inkSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = closeSearch, enabled = isSearchActive, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close", tint = theme.inkPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
