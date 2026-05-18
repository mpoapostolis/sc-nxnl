package com.skincoach.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.skincoach.app.ui.theme.Sage
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import com.skincoach.app.ui.theme.TerracottaSoft
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    Terracotta,
    TerracottaDeep,
    Sage,
    TerracottaSoft,
    Color(0xFFF2C46B), // warm gold
    Color(0xFFFFFFFF),
)

/** One piece of confetti — randomised launch direction, spin, size and colour. */
private class Confetto {
    val angle = -1.5708f + (Random.nextFloat() - 0.5f) * 2.7f      // mostly upward
    val speed = 360f + Random.nextFloat() * 620f
    val spinDeg = (Random.nextFloat() - 0.5f) * 760f
    val size = 8f + Random.nextFloat() * 11f
    val color = ConfettiColors[Random.nextInt(ConfettiColors.size)]
    val isDot = Random.nextFloat() < 0.34f
    val originX = 0.5f + (Random.nextFloat() - 0.5f) * 0.34f
    val originY = 0.32f + (Random.nextFloat() - 0.5f) * 0.14f
    val delay = Random.nextFloat() * 0.22f
}

/**
 * A soft confetti pop in the app's blush palette. Flip [play] to true to fire it once;
 * it erupts from the upper-middle of [modifier]'s bounds and gently rains down.
 * [intensity] (0f..1f) scales how many pieces fly — 0f draws nothing at all.
 */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    play: Boolean,
    intensity: Float = 1f,
) {
    if (!play || intensity <= 0f) return

    val count = (62f * intensity).toInt().coerceIn(12, 70)
    val pieces = remember(play, count) { List(count) { Confetto() } }
    var elapsed by remember(play) { mutableFloatStateOf(0f) }
    val duration = 2.2f

    LaunchedEffect(play) {
        val start = withFrameNanos { it }
        var now = start
        while ((now - start).toFloat() / 1e9f < duration) {
            now = withFrameNanos { it }
            elapsed = (now - start).toFloat() / 1e9f
        }
    }

    Canvas(modifier) {
        val gravity = 1250f
        pieces.forEach { p ->
            val t = elapsed - p.delay
            if (t <= 0f) return@forEach
            val life = (t / (duration - p.delay)).coerceIn(0f, 1f)
            val alpha = if (life < 0.68f) 1f else (1f - (life - 0.68f) / 0.32f).coerceAtLeast(0f)
            if (alpha <= 0f) return@forEach
            val x = size.width * p.originX + cos(p.angle) * p.speed * t
            val y = size.height * p.originY + sin(p.angle) * p.speed * t + 0.5f * gravity * t * t
            val s = p.size * density
            if (p.isDot) {
                drawCircle(p.color, s * 0.34f, Offset(x, y), alpha = alpha)
            } else {
                rotate(degrees = p.spinDeg * t, pivot = Offset(x, y)) {
                    drawRoundRect(
                        color = p.color,
                        topLeft = Offset(x - s / 2f, y - s * 0.22f),
                        size = Size(s, s * 0.44f),
                        cornerRadius = CornerRadius(s * 0.18f),
                        alpha = alpha,
                    )
                }
            }
        }
    }
}
