package com.bitey.app.core.ui.neumorphic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.core.ui.theme.StickerSoftShadow

/**
 * Minimalist card modifier: hardware-accelerated, crisp subtle border, optional soft shadow.
 * Replaces heavy Neumorphic software blur with 120fps display-list drawing.
 */
fun Modifier.minimalistCard(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 1.dp
): Modifier = this.composed {
    val theme = LocalNeumorphicTheme.current
    val shape = RoundedCornerShape(cornerRadius)

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation.coerceAtMost(2.dp),
            shape = shape,
            ambientColor = Color(0x08000000),
            spotColor = Color(0x0D000000)
        )
    } else Modifier

    shadowModifier
        .clip(shape)
        .background(theme.surface)
        .border(1.dp, theme.border, shape)
}

/**
 * Recessed / inset field for input areas and tasting notes in minimalist design.
 */
fun Modifier.minimalistInset(
    cornerRadius: Dp = 14.dp
): Modifier = this.composed {
    val theme = LocalNeumorphicTheme.current
    val shape = RoundedCornerShape(cornerRadius)

    this
        .clip(shape)
        .background(theme.surfaceVariant)
        .border(1.dp, theme.border.copy(alpha = 0.6f), shape)
}

/**
 * Drop-in backward compatibility for neumorphicCard: maps to clean minimalist card.
 */
fun Modifier.neumorphicCard(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 1.dp,
    blurRadius: Dp = 4.dp
): Modifier = this.minimalistCard(cornerRadius = cornerRadius, elevation = elevation)

fun Modifier.neumorphicCard(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color,
    elevation: Dp = 1.dp,
    blurRadius: Dp = 4.dp
): Modifier = this.composed {
    val theme = LocalNeumorphicTheme.current
    val shape = RoundedCornerShape(cornerRadius)

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation.coerceAtMost(2.dp),
            shape = shape,
            ambientColor = Color(0x08000000),
            spotColor = Color(0x0D000000)
        )
    } else Modifier

    shadowModifier
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, theme.border, shape)
}

/**
 * Drop-in backward compatibility for neumorphicRaised: maps to minimalist elevated button/pill.
 */
fun Modifier.neumorphicRaised(
    cornerRadius: Dp = 16.dp,
    shadowOffset: Dp = 2.dp,
    blurRadius: Dp = 4.dp
): Modifier = this.minimalistCard(cornerRadius = cornerRadius, elevation = 1.dp)

fun Modifier.neumorphicRaised(
    cornerRadius: Dp = 16.dp,
    lightShadowColor: Color = Color.Transparent,
    darkShadowColor: Color = Color.Transparent,
    shadowOffset: Dp = 2.dp,
    blurRadius: Dp = 4.dp
): Modifier = this.minimalistCard(cornerRadius = cornerRadius, elevation = 1.dp)

/**
 * Drop-in backward compatibility for neumorphicInset: maps to clean minimalist recessed surface.
 */
fun Modifier.neumorphicInset(
    cornerRadius: Dp = 14.dp,
    depth: Dp = 2.dp,
    blurRadius: Dp = 4.dp
): Modifier = this.minimalistInset(cornerRadius = cornerRadius)

/**
 * Die-cut sticker effect:
 * Since StickerCompositor already bakes the authentic die-cut white stroke and soft drop shadow
 * directly into the transparent WebP bitmap, this modifier leaves the sticker as an organic floating
 * shape WITHOUT drawing a rectangular border, box frame, or grid around the food sticker!
 */
fun Modifier.dieCutStickerEffect(
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = StickerDieCutWhite,
    shadowColor: Color = StickerSoftShadow
): Modifier = this
