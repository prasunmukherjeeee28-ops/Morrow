package com.morrow.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid glass design language for Morrow.
 *
 * Surfaces are frosted: translucent white gradients layered over the dark
 * animated backdrop, edged with a specular highlight stroke that catches the
 * light on the top-left rim, like light refracting through glass. Accent
 * tints can be blended in for interactive elements.
 */

// Frosted fill stops (alpha white over the dark backdrop).
val GlassTop = Color(0x2BFFFFFF)      // ~17% — light catching the upper pane
val GlassMid = Color(0x14FFFFFF)      // ~8%  — body of the glass
val GlassDeep = Color(0x0AFFFFFF)     // ~4%  — lower pane fading into the scene
val GlassEdgeBright = Color(0x66FFFFFF) // ~40% specular rim
val GlassEdgeSoft = Color(0x1FFFFFFF)   // ~12% fading rim

/** Default frosted vertical gradient. */
fun glassFill(top: Color = GlassTop, bottom: Color = GlassDeep): Brush =
    Brush.verticalGradient(listOf(top, GlassMid, bottom))

/** Specular border gradient: bright on the top-left rim, dissolving around the edge. */
fun glassEdgeBrush(): Brush = Brush.linearGradient(
    colors = listOf(GlassEdgeBright, GlassEdgeSoft, Color.Transparent, GlassEdgeSoft.copy(alpha = 0.08f)),
    start = Offset(0f, 0f),
    end = Offset(400f, 400f)
)

/** Accent-tinted glass fill for interactive elements (chips, bubbles, hero cards). */
fun accentGlassFill(accent: Color, topAlpha: Float = 0.28f, bottomAlpha: Float = 0.10f): Brush =
    Brush.verticalGradient(
        listOf(
            accent.copy(alpha = topAlpha),
            accent.copy(alpha = (topAlpha + bottomAlpha) / 2f),
            accent.copy(alpha = bottomAlpha)
        )
    )

/**
 * Applies the full liquid-glass treatment: clip, frosted gradient fill and a
 * specular 1dp rim. Optional accent glow adds a soft colored halo behind the
 * pane for hero elements.
 */
fun Modifier.glassSurface(
    shape: Shape,
    accent: Color? = null,
    fill: Brush = glassFill(),
    edge: Brush = glassEdgeBrush(),
    accentGlow: Boolean = false
): Modifier = this
    .clip(shape)
    .then(
        if (accent != null && accentGlow) {
            Modifier.background(accent.copy(alpha = 0.10f), shape)
        } else Modifier
    )
    .background(fill, shape)
    .border(1.dp, edge, shape)

/** Convenience: pill-shaped glass, used for bars, composers and docks. */
fun Modifier.glassPill(
    accent: Color? = null,
    fill: Brush = glassFill()
): Modifier = glassSurface(RoundedCornerShape(percent = 50), accent, fill)

/** Standard glass corner radii for cards and panes. */
val GlassRadiusCard = RoundedCornerShape(24.dp)
val GlassRadiusPane = RoundedCornerShape(28.dp)
