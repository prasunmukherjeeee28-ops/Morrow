package com.morrow.ai.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Morrow's original violet line icon set with a springy press response. */
enum class MorrowGlyph { SPARK, SEND, MIC, SETTINGS, HISTORY, PLUS, CLOSE }

@Composable
fun MorrowIconButton(
    glyph: MorrowGlyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color(0xFFB49AFF),
    contentDescription: String = glyph.name.lowercase()
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "morrowIconTap"
    )
    Box(
        modifier = modifier.size(size).scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(iconSize)) {
            val s = this.size.minDimension
            val stroke = Stroke(width = s * .095f, cap = StrokeCap.Round)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
                drawLine(tint, androidx.compose.ui.geometry.Offset(x1*s,y1*s), androidx.compose.ui.geometry.Offset(x2*s,y2*s), stroke.width, cap = StrokeCap.Round)
            when (glyph) {
                MorrowGlyph.SPARK -> {
                    line(.5f,.05f,.5f,.95f); line(.05f,.5f,.95f,.5f)
                    line(.19f,.19f,.81f,.81f); line(.81f,.19f,.19f,.81f)
                }
                MorrowGlyph.SEND -> {
                    line(.12f,.5f,.88f,.5f); line(.55f,.16f,.88f,.5f); line(.55f,.84f,.88f,.5f)
                }
                MorrowGlyph.MIC -> {
                    drawRoundRect(tint, androidx.compose.ui.geometry.Offset(.35f*s,.08f*s), androidx.compose.ui.Size(.3f*s,.55f*s), androidx.compose.ui.geometry.CornerRadius(.15f*s), style=stroke)
                    drawArc(tint, 0f, 180f, false, androidx.compose.ui.geometry.Offset(.2f*s,.25f*s), androidx.compose.ui.Size(.6f*s,.55f*s), style=stroke)
                    line(.5f,.8f,.5f,.98f); line(.3f,.98f,.7f,.98f)
                }
                MorrowGlyph.SETTINGS -> {
                    drawCircle(tint, radius=.25f*s, style=stroke)
                    drawCircle(tint, radius=.08f*s)
                    for (i in 0..7) { val a=i*Math.PI/4; line(.5f+(.29*Math.cos(a)).toFloat(),.5f+(.29*Math.sin(a)).toFloat(),.5f+(.43*Math.cos(a)).toFloat(),.5f+(.43*Math.sin(a)).toFloat()) }
                }
                MorrowGlyph.HISTORY -> {
                    drawCircle(tint, radius=.42f*s, style=stroke)
                    line(.5f,.22f,.5f,.5f); line(.5f,.5f,.72f,.62f)
                    line(.08f,.25f,.08f,.05f); line(.08f,.05f,.28f,.05f)
                }
                MorrowGlyph.PLUS -> { line(.5f,.12f,.5f,.88f); line(.12f,.5f,.88f,.5f) }
                MorrowGlyph.CLOSE -> { line(.2f,.2f,.8f,.8f); line(.8f,.2f,.2f,.8f) }
            }
        }
    }
}
