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
 * Shared Morphing Search Bar:
 * Morphs between Title + compact search icon and expanded input bar.
 * Defers state reads to layout/draw phases to eliminate recomposition bottlenecks during animation.
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
    onSearch: ((String) -> Unit)? = null,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val theme = LocalNeumorphicTheme.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val closeSearch: () -> Unit = {
        focusManager.clearFocus()
        keyboard?.hide()
        onSearchActiveChange(false)
        onSearchQueryChange("")
    }

    BackHandler(enabled = isSearchActive, onBack = closeSearch)

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(120)
            try {
                focusRequester.requestFocus()
                keyboard?.show()
            } catch (_: Exception) {}
        } else {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "SearchMorph"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Title on the left: graphicsLayer defers alpha and translation without recomposing
        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val p = progress.coerceIn(0f, 1f)
                alpha = (1f - p * 2.2f).coerceIn(0f, 1f)
                translationX = -24.dp.toPx() * p
            },
            contentAlignment = Alignment.CenterStart
        ) { titleContent() }

        val rCollapsed = 20.dp
        val rExpanded = 12.dp

        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val p = progress.coerceIn(0f, 1f)
                    val w = lerp(40.dp.roundToPx().toFloat(), constraints.maxWidth.toFloat(), p).roundToInt()
                    val h = lerp(40.dp.toPx(), 44.dp.toPx(), p).roundToInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h))
                    layout(w, h) { placeable.place(0, 0) }
                }
                .drawBehind {
                    val p = progress.coerceIn(0f, 1f)
                    if (p > 0.02f) {
                        val cr = CornerRadius(lerp(rCollapsed.toPx(), rExpanded.toPx(), p))
                        drawRoundRect(color = theme.surface, cornerRadius = cr)
                        drawRoundRect(color = BiteyOrange.copy(alpha = p), cornerRadius = cr, style = Stroke(width = 1.dp.toPx()))
                    }
                }
                .then(
                    if (!isSearchActive) {
                        Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onSearchActiveChange(true)
                        }
                    } else Modifier
                ),
            contentAlignment = if (isSearchActive) Alignment.CenterStart else Alignment.Center
        ) {
            if (!isSearchActive && progress < 0.1f) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) BiteyOrange else theme.inkPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (isSearchActive || progress >= 0.1f) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp).graphicsLayer { alpha = progress.coerceIn(0f, 1f) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = BiteyOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = theme.inkPrimary),
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
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = theme.inkSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = closeSearch, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close", tint = theme.inkPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
