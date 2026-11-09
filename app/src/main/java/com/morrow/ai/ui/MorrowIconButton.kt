package com.morrow.ai.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.morrow.ai.R

/** Morrow's illustrated icon family. Each glyph is a layered Android vector image asset. */
enum class MorrowGlyph { SPARK, SEND, MIC, SETTINGS, HISTORY, PLUS, CLOSE }

@Composable
fun MorrowGlyphIcon(
    glyph: MorrowGlyph,
    modifier: Modifier = Modifier.size(22.dp),
    tint: Color = Color(0xFFB49AFF)
) {
    val artwork = when (glyph) {
        MorrowGlyph.SPARK -> R.drawable.morrow_ic_spark
        MorrowGlyph.SEND -> R.drawable.morrow_ic_send
        MorrowGlyph.MIC -> R.drawable.morrow_ic_microphone
        MorrowGlyph.SETTINGS -> R.drawable.morrow_ic_settings
        MorrowGlyph.HISTORY -> R.drawable.morrow_ic_history
        MorrowGlyph.PLUS -> R.drawable.morrow_ic_plus
        MorrowGlyph.CLOSE -> R.drawable.morrow_ic_close
    }

    Image(
        painter = painterResource(id = artwork),
        contentDescription = null,
        modifier = modifier.sizeIn(maxWidth = 24.dp, maxHeight = 24.dp),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint, BlendMode.Modulate)
    )
}

/** Individually drawn Morrow image assets used instead of stock icon-font glyphs. */
enum class MorrowArtwork {
    SPARK, SEND, MIC, SETTINGS, HISTORY, PLUS, CLOSE,
    CHAT, HOME, SEARCH, CODE, WIDGETS, IMAGE, SCHOOL, EDIT, BOOKMARK,
    CHEVRON, BACK, FORWARD, MENU, MORE, TIMER, FILTER, GLOBE,
    COPY, SHARE, SPEAKER, REFRESH, FILE, CAMERA, STOP, CHECK, DELETE,
    LIGHTBULB, WARNING, INFO, PALETTE, SECURITY, TUNE, THUMB_UP, THUMB_DOWN, TEXT_FIELDS
}

@Composable
fun MorrowArtworkIcon(
    artwork: MorrowArtwork,
    modifier: Modifier = Modifier.size(22.dp),
    tint: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    val resource = when (artwork) {
        MorrowArtwork.SPARK -> R.drawable.morrow_ic_spark
        MorrowArtwork.SEND -> R.drawable.morrow_ic_send
        MorrowArtwork.MIC -> R.drawable.morrow_ic_microphone
        MorrowArtwork.SETTINGS -> R.drawable.morrow_ic_settings
        MorrowArtwork.HISTORY -> R.drawable.morrow_ic_history
        MorrowArtwork.PLUS -> R.drawable.morrow_ic_plus
        MorrowArtwork.CLOSE -> R.drawable.morrow_ic_close
        MorrowArtwork.CHAT -> R.drawable.morrow_ic_chat
        MorrowArtwork.HOME -> R.drawable.morrow_ic_home
        MorrowArtwork.SEARCH -> R.drawable.morrow_ic_search
        MorrowArtwork.CODE -> R.drawable.morrow_ic_code
        MorrowArtwork.WIDGETS -> R.drawable.morrow_ic_widgets
        MorrowArtwork.IMAGE -> R.drawable.morrow_ic_image
        MorrowArtwork.SCHOOL -> R.drawable.morrow_ic_school
        MorrowArtwork.EDIT -> R.drawable.morrow_ic_edit
        MorrowArtwork.BOOKMARK -> R.drawable.morrow_ic_bookmark
        MorrowArtwork.CHEVRON -> R.drawable.morrow_ic_chevron
        MorrowArtwork.BACK -> R.drawable.morrow_ic_back
        MorrowArtwork.FORWARD -> R.drawable.morrow_ic_forward
        MorrowArtwork.MENU -> R.drawable.morrow_ic_menu
        MorrowArtwork.MORE -> R.drawable.morrow_ic_more
        MorrowArtwork.TIMER -> R.drawable.morrow_ic_timer
        MorrowArtwork.FILTER -> R.drawable.morrow_ic_filter
        MorrowArtwork.GLOBE -> R.drawable.morrow_ic_globe
        MorrowArtwork.COPY -> R.drawable.morrow_ic_copy
        MorrowArtwork.SHARE -> R.drawable.morrow_ic_share
        MorrowArtwork.SPEAKER -> R.drawable.morrow_ic_speaker
        MorrowArtwork.REFRESH -> R.drawable.morrow_ic_refresh
        MorrowArtwork.FILE -> R.drawable.morrow_ic_file
        MorrowArtwork.CAMERA -> R.drawable.morrow_ic_camera
        MorrowArtwork.STOP -> R.drawable.morrow_ic_stop
        MorrowArtwork.CHECK -> R.drawable.morrow_ic_check
        MorrowArtwork.DELETE -> R.drawable.morrow_ic_delete
        MorrowArtwork.LIGHTBULB -> R.drawable.morrow_ic_lightbulb
        MorrowArtwork.WARNING -> R.drawable.morrow_ic_warning
        MorrowArtwork.INFO -> R.drawable.morrow_ic_info
        MorrowArtwork.PALETTE -> R.drawable.morrow_ic_palette
        MorrowArtwork.SECURITY -> R.drawable.morrow_ic_security
        MorrowArtwork.TUNE -> R.drawable.morrow_ic_tune
        MorrowArtwork.THUMB_UP -> R.drawable.morrow_ic_thumb_up
        MorrowArtwork.THUMB_DOWN -> R.drawable.morrow_ic_thumb_down
        MorrowArtwork.TEXT_FIELDS -> R.drawable.morrow_ic_text_fields
    }

    Image(
        painter = painterResource(id = resource),
        contentDescription = contentDescription,
        modifier = modifier.sizeIn(maxWidth = 24.dp, maxHeight = 24.dp),
        contentScale = ContentScale.Fit,
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint, BlendMode.Modulate)
    )
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
