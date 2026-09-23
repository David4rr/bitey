package com.bitey.app.feature.journal.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp

class SharedDishTransition(
    val deltaX: Float,
    val deltaY: Float,
    val initialScale: Float
) {
    fun calculateTranslationX(progress: Float): Float = lerp(deltaX, 0f, progress)
    fun calculateTranslationY(progress: Float): Float = lerp(deltaY, 0f, progress)
    fun calculateScale(progress: Float): Float = lerp(initialScale, 1f, progress)

    companion object {
        fun create(sourceRect: Rect?, targetRect: Rect?): SharedDishTransition? {
            if (sourceRect == null || targetRect == null) return null
            if (sourceRect.width <= 0f || targetRect.width <= 0f) return null
            val deltaX = sourceRect.center.x - targetRect.center.x
            val deltaY = sourceRect.center.y - targetRect.center.y
            val scale = (sourceRect.width / targetRect.width).coerceIn(0.12f, 3.0f)
            return SharedDishTransition(deltaX, deltaY, scale)
        }
    }
}

@Composable
fun rememberSharedDishTransitionProgress(
    key: Any?,
    hasSource: Boolean,
    isTargetReady: Boolean = true
): Animatable<Float, AnimationVector1D> {
    val progress = remember(key) { Animatable(if (hasSource) 0f else 1f) }
    LaunchedEffect(key, hasSource, isTargetReady) {
        if (hasSource && isTargetReady) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else if (!hasSource) {
            progress.snapTo(1f)
        }
    }
    return progress
}

fun Modifier.sharedDishTransform(
    transition: SharedDishTransition?,
    progress: Float,
    fallbackScale: Float = 1f,
    fallbackAlpha: Float = 1f
): Modifier = this.graphicsLayer {
    if (transition != null) {
        translationX = transition.calculateTranslationX(progress)
        translationY = transition.calculateTranslationY(progress)
        val s = transition.calculateScale(progress)
        scaleX = s
        scaleY = s
        alpha = 1f
    } else {
        scaleX = fallbackScale
        scaleY = fallbackScale
        alpha = fallbackAlpha
    }
}
