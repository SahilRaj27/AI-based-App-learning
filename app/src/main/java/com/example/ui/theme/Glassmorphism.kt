package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance ambient mesh background with floating neon light orbs
 * that creates the depth necessary for translucent frosted glassmorphism.
 */
@Composable
fun FuturisticMeshBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    content: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_orbs")
    
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )

    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    val baseBg = if (isDark) CosmicBackgroundDark else Color(0xFFF1F5F9)
    val orbColor1 = if (isDark) Color(0x356366F1) else Color(0x25818CF8)
    val orbColor2 = if (isDark) Color(0x3006B6D4) else Color(0x2038BDF8)
    val orbColor3 = if (isDark) Color(0x258B5CF6) else Color(0x18C084FC)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base cosmic dark / clean light canvas
            drawRect(color = baseBg)

            // Orb 1: Upper right electric indigo / violet
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor1, Color.Transparent),
                    center = Offset(size.width * 0.85f + orbOffset1, size.height * 0.15f + orbOffset2),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.85f + orbOffset1, size.height * 0.15f + orbOffset2)
            )

            // Orb 2: Mid-left cyan neon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor2, Color.Transparent),
                    center = Offset(size.width * 0.1f + orbOffset2, size.height * 0.5f + orbOffset1),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.1f + orbOffset2, size.height * 0.5f + orbOffset1)
            )

            // Orb 3: Bottom ambient magenta / violet
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor3, Color.Transparent),
                    center = Offset(size.width * 0.6f + orbOffset1 * 0.5f, size.height * 0.85f + orbOffset2 * 0.5f),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.6f + orbOffset1 * 0.5f, size.height * 0.85f + orbOffset2 * 0.5f)
            )
        }

        content?.invoke()
    }
}

/**
 * Modifier that applies iOS-style glassmorphism with specular glare reflection
 * and crisp luminous gradient border.
 */
fun Modifier.iosGlassmorphic(
    cornerRadius: Dp = 22.dp,
    isDark: Boolean = true,
    hasSpecularSheen: Boolean = true,
    borderAlpha: Float = 0.5f,
    onClick: (() -> Unit)? = null
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)

    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCC15192C),
                Color(0xA6111425),
                Color(0x8C1A1E38)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF5FFFFFF),
                Color(0xEBFFFFFF),
                Color(0xE0F8FAFC)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.45f * borderAlpha),
                Color(0xFF818CF8).copy(alpha = 0.35f * borderAlpha),
                Color(0xFF38BDF8).copy(alpha = 0.15f * borderAlpha),
                Color.White.copy(alpha = 0.08f * borderAlpha)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFE2E8F0).copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.5f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val baseModifier = this
        .clip(shape)
        .background(backgroundBrush)
        .border(1.dp, borderBrush, shape)

    val withSheen = if (hasSpecularSheen) {
        baseModifier.drawWithContent {
            drawContent()
            // Top curved specular gloss highlight
            val sheenHeight = size.height * 0.35f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.12f else 0.25f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = sheenHeight
                ),
                size = Size(size.width, sheenHeight)
            )
        }
    } else {
        baseModifier
    }

    return if (onClick != null) {
        withSheen.clickable(
            interactionSource = MutableInteractionSource(),
            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
            onClick = onClick
        )
    } else {
        withSheen
    }
}

/**
 * Modifier for glossy iOS pill components (chips, filter items, badges)
 */
fun Modifier.iosGlossyPill(
    isSelected: Boolean = false,
    isDark: Boolean = true,
    activeColor: Color = IndigoPrimary,
    onClick: (() -> Unit)? = null
): Modifier {
    val shape = CircleShape

    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                activeColor,
                activeColor.copy(alpha = 0.85f),
                SkySecondary.copy(alpha = 0.9f)
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x35FFFFFF),
                    Color(0x18FFFFFF)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xEEFFFFFF),
                    Color(0xD9F1F5F9)
                )
            )
        }
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                activeColor.copy(alpha = 0.3f)
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    Color(0xFFCBD5E1).copy(alpha = 0.5f)
                )
            )
        }
    }

    val base = this
        .clip(shape)
        .background(backgroundBrush)
        .border(1.dp, borderBrush, shape)
        .drawWithContent {
            drawContent()
            // Top specular shine
            val sheenHeight = size.height * 0.45f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isSelected) 0.30f else 0.15f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = sheenHeight
                ),
                size = Size(size.width, sheenHeight)
            )
        }

    return if (onClick != null) {
        base.clickable(
            interactionSource = MutableInteractionSource(),
            indication = ripple(color = Color.White.copy(alpha = 0.25f)),
            onClick = onClick
        )
    } else {
        base
    }
}
