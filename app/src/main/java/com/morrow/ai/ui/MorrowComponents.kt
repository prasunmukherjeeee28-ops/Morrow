package com.morrow.ai.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos

/** Filled pill button with a subtle press-scale animation. */
@Composable
fun MorrowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        label = "primaryButtonScale"
    )
    val background by animateColorAsState(
        targetValue = if (enabled) MorrowAccent else MorrowAccent.copy(alpha = 0.3f),
        label = "primaryButtonBackground"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MorrowRadiusPill)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Outlined pill button with no fill. */
@Composable
fun MorrowGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(MorrowRadiusPill)
            .border(1.dp, MorrowBorder, MorrowRadiusPill)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) MorrowText else MorrowMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Rounded surface container. Pass onClick to make the whole card tappable. */
@Composable
fun MorrowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .clip(MorrowRadiusMedium)
        .background(MorrowSurface)
        .border(1.dp, MorrowBorder, MorrowRadiusMedium)
    val clickableBase = if (onClick != null) base.clickable(onClick = onClick) else base

    Column(
        modifier = clickableBase.padding(16.dp),
        content = content
    )
}

/** Selectable pill chip. Selected state shows a violet fill and stroke. */
@Composable
fun MorrowChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateColorAsState(
        targetValue = if (selected) MorrowAccent.copy(alpha = 0.22f) else MorrowChipFill,
        label = "chipFill"
    )
    val stroke by animateColorAsState(
        targetValue = if (selected) MorrowAccent else MorrowChipBorder,
        label = "chipStroke"
    )

    Box(
        modifier = modifier
            .clip(MorrowRadiusPill)
            .background(fill)
            .border(1.dp, stroke, MorrowRadiusPill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) MorrowText else MorrowMuted,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Small uppercase label used to group content. */
@Composable
fun MorrowSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text.uppercase(),
        color = MorrowMuted,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
    )
}

/** Settings-style row with a title, optional subtitle, and optional trailing value. */
@Composable
fun MorrowSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null
) {
    val base = modifier
        .fillMaxWidth()
        .clip(MorrowRadiusSmall)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 12.dp, vertical = 12.dp)

    Row(
        modifier = base,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MorrowText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = MorrowMuted, fontSize = 12.sp)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            Text(trailing, color = MorrowAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Pill-shaped text input that matches the Morrow composer. */
@Composable
fun MorrowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MorrowRadiusPill)
            .background(MorrowSurface)
            .border(1.dp, MorrowBorder, MorrowRadiusPill)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = MorrowText, fontSize = 15.sp, lineHeight = 21.sp),
            cursorBrush = SolidColor(MorrowAccent),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(placeholder, color = MorrowMuted, fontSize = 14.sp)
                    }
                    inner()
                }
            }
        )
    }
}

/** Three bouncing dots, for loading states. */
@Composable
fun MorrowLoadingDots(
    modifier: Modifier = Modifier,
    color: Color = MorrowAccent
) {
    val transition = rememberInfiniteTransition(label = "loadingDots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingDotsPhase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val p = (phase + index * 0.2f) % 1f
            val wave = 0.5f - 0.5f * cos(2f * PI.toFloat() * p)
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer {
                        alpha = 0.3f + 0.7f * wave
                        scaleX = 0.8f + 0.4f * wave
                        scaleY = 0.8f + 0.4f * wave
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/** Centered placeholder for empty lists or screens, with an optional action below. */
@Composable
fun MorrowEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = MorrowText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = MorrowMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}
