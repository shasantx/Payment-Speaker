package com.paisabol.paymentspeaker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.paisabol.paymentspeaker.ui.theme.BgCanvas
import com.paisabol.paymentspeaker.ui.theme.BrassAccent
import com.paisabol.paymentspeaker.ui.theme.BrassTint
import com.paisabol.paymentspeaker.ui.theme.Hairline
import com.paisabol.paymentspeaker.ui.theme.HairlineStrong
import com.paisabol.paymentspeaker.ui.theme.InkMuted
import com.paisabol.paymentspeaker.ui.theme.InkPrimary
import com.paisabol.paymentspeaker.ui.theme.InkSecondary
import com.paisabol.paymentspeaker.ui.theme.SurfaceCard
import com.paisabol.paymentspeaker.ui.theme.SurfaceRaised

val StandardEaseOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Tactile hardware soundbox status deck with a subtle perforated acoustic grille.
 * Quiet matte surface with hairline depth — zero glowing gradients or abstract wallpaper.
 */
@Composable
fun HeroSpeakerCard(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor by animateColorAsState(
        targetValue = if (active) HairlineStrong else Hairline,
        animationSpec = tween(180, easing = StandardEaseOut),
        label = "deck_border"
    )
    val grilleAlpha = if (active) 0.08f else 0.035f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceCard)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        // Precision perforated acoustic speaker grille in the upper-right hardware quadrant
        Canvas(modifier = Modifier.matchParentSize()) {
            val step = 10.dp.toPx()
            val radius = 1.15.dp.toPx()
            val startX = size.width * 0.56f
            val endX = size.width - 16.dp.toPx()
            val startY = 16.dp.toPx()
            val endY = size.height * 0.62f

            var y = startY
            var row = 0
            while (y <= endY) {
                var x = startX + if (row % 2 == 1) step * 0.5f else 0f
                while (x <= endX) {
                    val normX = ((x - startX) / (endX - startX)).coerceIn(0f, 1f)
                    val normY = 1f - ((y - startY) / (endY - startY)).coerceIn(0f, 1f)
                    val fade = (normX * 0.65f + normY * 0.35f).coerceIn(0f, 1f)
                    drawCircle(
                        color = InkPrimary.copy(alpha = grilleAlpha * fade),
                        radius = radius,
                        center = Offset(x, y)
                    )
                    x += step
                }
                y += step
                row++
            }

            // Quiet brass acoustic hairline along the top edge when active
            if (active) {
                drawLine(
                    color = BrassAccent.copy(alpha = 0.65f),
                    start = Offset(20.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width * 0.34f, 1.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Primary tactile action button.
 * Off-white solid fill with dark ink for primary emphasis; raised charcoal with hairline for secondary.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    primaryStyle: Boolean = true,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(10.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = when {
            !enabled -> SurfaceCard
            primaryStyle -> InkPrimary
            else -> SurfaceRaised
        },
        animationSpec = tween(160, easing = StandardEaseOut),
        label = "btn_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> InkMuted
            primaryStyle -> BgCanvas
            else -> InkPrimary
        },
        animationSpec = tween(160, easing = StandardEaseOut),
        label = "btn_fg"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(bgColor)
            .then(
                if (!primaryStyle) {
                    Modifier.border(width = 1.dp, color = Hairline, shape = shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = if (primaryStyle) BgCanvas else InkPrimary),
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(color = contentColor)
            )
        }
    }
}

/**
 * Tactile matte control tile on a strict 4px grid.
 */
@Composable
fun QuickActionTile(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val bgColor by animateColorAsState(
        targetValue = if (active) BrassTint else SurfaceCard,
        animationSpec = tween(160, easing = StandardEaseOut),
        label = "tile_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) BrassAccent.copy(alpha = 0.35f) else Hairline,
        animationSpec = tween(160, easing = StandardEaseOut),
        label = "tile_border"
    )
    val iconColor by animateColorAsState(
        targetValue = if (active) BrassAccent else InkSecondary,
        animationSpec = tween(160, easing = StandardEaseOut),
        label = "tile_icon"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(shape)
                .background(bgColor)
                .border(width = 1.dp, color = borderColor, shape = shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (active) InkPrimary else InkSecondary
            ),
            maxLines = 1
        )
    }
}

/**
 * Quiet top header icon button sharing the control radius (10dp).
 */
@Composable
fun TopIconButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(if (active) SurfaceRaised else SurfaceCard)
            .border(
                width = 1.dp,
                color = if (active) HairlineStrong else Hairline,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) BrassAccent else InkSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Signature 4-bar acoustic emblem matching the PaisaBol hardware logo.
 * Third bar carries the matte brass accent; other bars sit in warm ivory.
 */
@Composable
fun PaisaBolMark(
    modifier: Modifier = Modifier,
    sizeDp: Int = 20,
    accentActive: Boolean = true
) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.13f
        val heights = listOf(0.38f, 0.70f, 0.96f, 0.54f)
        heights.forEachIndexed { idx, ratio ->
            val x = w * (0.14f + idx * 0.24f)
            val top = h * (1f - ratio) / 2f
            val barColor = when {
                idx == 2 && accentActive -> BrassAccent
                idx == 0 || idx == 3 -> InkPrimary.copy(alpha = 0.72f)
                else -> InkPrimary
            }
            drawLine(
                color = barColor,
                start = Offset(x, top),
                end = Offset(x, h - top),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
