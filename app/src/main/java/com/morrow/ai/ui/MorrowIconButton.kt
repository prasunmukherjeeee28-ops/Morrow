package com.morrow.ai.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Morrow's custom glyph family. */
enum class MorrowGlyph { SPARK, SEND, MIC, SETTINGS, HISTORY, PLUS, CLOSE }

@Composable
fun MorrowGlyphIcon(
    glyph: MorrowGlyph,
    modifier: Modifier = Modifier.size(22.dp),
    tint: Color = Color(0xFFB49AFF)
) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = Stroke(width = s * .095f, cap = StrokeCap.Round)

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(x1 * s, y1 * s),
                end = androidx.compose.ui.geometry.Offset(x2 * s, y2 * s),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }

        when (glyph) {
            MorrowGlyph.SPARK -> {
                line(.5f, .05f, .5f, .95f)
                line(.05f, .5f, .95f, .5f)
                line(.19f, .19f, .81f, .81f)
                line(.81f, .19f, .19f, .81f)
            }
            MorrowGlyph.SEND -> {
                line(.12f, .5f, .88f, .5f)
                line(.55f, .16f, .88f, .5f)
                line(.55f, .84f, .88f, .5f)
            }
            MorrowGlyph.MIC -> {
                drawRoundRect(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(.35f * s, .08f * s),
                    size = androidx.compose.ui.geometry.Size(.3f * s, .55f * s),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(.15f * s),
                    style = stroke
                )
                drawArc(
                    color = tint,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(.2f * s, .25f * s),
                    size = androidx.compose.ui.geometry.Size(.6f * s, .55f * s),
                    style = stroke
                )
                line(.5f, .8f, .5f, .98f)
                line(.3f, .98f, .7f, .98f)
            }
            MorrowGlyph.SETTINGS -> {
                drawCircle(color = tint, radius = .25f * s, style = stroke)
                drawCircle(color = tint, radius = .08f * s)
                for (i in 0..7) {
                    val angle = i * Math.PI / 4
                    line(
                        .5f + (.29 * kotlin.math.cos(angle)).toFloat(),
                        .5f + (.29 * kotlin.math.sin(angle)).toFloat(),
                        .5f + (.43 * kotlin.math.cos(angle)).toFloat(),
                        .5f + (.43 * kotlin.math.sin(angle)).toFloat()
                    )
                }
            }
            MorrowGlyph.HISTORY -> {
                drawCircle(color = tint, radius = .42f * s, style = stroke)
                line(.5f, .22f, .5f, .5f)
                line(.5f, .5f, .72f, .62f)
                line(.08f, .25f, .08f, .05f)
                line(.08f, .05f, .28f, .05f)
            }
            MorrowGlyph.PLUS -> {
                line(.5f, .12f, .5f, .88f)
                line(.12f, .5f, .88f, .5f)
            }
            MorrowGlyph.CLOSE -> {
                line(.2f, .2f, .8f, .8f)
                line(.8f, .2f, .2f, .8f)
            }
        }
    }
}

/** Clickable icon surface used across Morrow; press scale, tiny tilt and violet flash. */
@Composable
fun MorrowAnimatedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(48.dp),
    enabled: Boolean = true,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) .88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morrowPressScale"
    )
    val tilt by animateFloatAsState(
        targetValue = if (pressed && enabled) -4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morrowPressTilt"
    )
    val glow by animateFloatAsState(
        targetValue = if (pressed && enabled) .18f else 0f,
        animationSpec = tween(durationMillis = 110),
        label = "morrowPressGlow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = tilt
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            if (glow > 0f) {
                drawCircle(
                    color = Color(0xFFB49AFF).copy(alpha = glow),
                    radius = size.minDimension / 2f
                )
            }
        }
        content()
    }
}

/** A Morrow glyph button with the same press motion as the rest of the app. */
@Composable
fun MorrowIconButton(
    glyph: MorrowGlyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    iconModifier: Modifier = Modifier,
    tint: Color = Color(0xFFB49AFF),
    enabled: Boolean = true,
    contentDescription: String = glyph.name.lowercase()
) {
    MorrowAnimatedIconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        enabled = enabled,
        contentDescription = contentDescription
    ) {
        MorrowGlyphIcon(
            glyph = glyph,
            modifier = iconModifier.size(iconSize),
            tint = tint
        )
    }
}

/** Use inside larger clickable rows to animate only the leading icon. */
@Composable
fun MorrowPressAnimatedIcon(
    pressed: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) .84f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morrowRowIconScale"
    )
    val tilt by animateFloatAsState(
        targetValue = if (pressed) -6f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morrowRowIconTilt"
    )
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = tilt
        },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
