package com.skincoach.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import kotlinx.coroutines.delay

/** Lumi's moods — the coach reacts to whatever's happening. */
enum class MascotMood { Cheerful, Celebrating, Caring, Curious }

/**
 * Lumi — the Skin Coach mascot. A soft, dewy little face drawn on a Canvas.
 * [mood] swaps her whole expression; [eyeOpen] is 1f = open, ~0f = mid-blink.
 */
@Composable
fun CoachMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Cheerful,
    eyeOpen: Float = 1f,
) {
    val tilt = if (mood == MascotMood.Curious) -8f else 0f
    Canvas(modifier.graphicsLayer { rotationZ = tilt }) {
        drawLumi(mood, eyeOpen)
    }
}

private fun DrawScope.drawLumi(mood: MascotMood, eyeOpen: Float) {
    val w = size.width
    val h = size.height
    val celebrating = mood == MascotMood.Celebrating

    // head
    drawCircle(Terracotta, w * 0.46f, Offset(w / 2f, h / 2f))
    // dewy highlights
    drawCircle(Color.White.copy(alpha = 0.34f), w * 0.135f, Offset(w * 0.33f, h * 0.30f))
    drawCircle(Color.White.copy(alpha = 0.20f), w * 0.052f, Offset(w * 0.63f, h * 0.24f))
    // rosy cheeks — brighter and bigger when she's celebrating
    val cheekAlpha = if (celebrating) 0.58f else 0.40f
    val cheekR = w * (if (celebrating) 0.10f else 0.085f)
    drawCircle(TerracottaDeep.copy(alpha = cheekAlpha), cheekR, Offset(w * 0.28f, h * 0.63f))
    drawCircle(TerracottaDeep.copy(alpha = cheekAlpha), cheekR, Offset(w * 0.72f, h * 0.63f))

    val eyeY = h * 0.45f
    val leftX = w * 0.37f
    val rightX = w * 0.63f

    // eyebrows
    when (mood) {
        MascotMood.Caring -> {
            // gentle, kind brows — inner ends lifted, never angry
            drawLine(
                Ink,
                Offset(leftX - w * 0.075f, eyeY - h * 0.15f),
                Offset(leftX + w * 0.065f, eyeY - h * 0.21f),
                strokeWidth = w * 0.042f, cap = StrokeCap.Round,
            )
            drawLine(
                Ink,
                Offset(rightX + w * 0.075f, eyeY - h * 0.15f),
                Offset(rightX - w * 0.065f, eyeY - h * 0.21f),
                strokeWidth = w * 0.042f, cap = StrokeCap.Round,
            )
        }
        MascotMood.Curious -> {
            // one playfully raised brow
            drawArc(
                color = Ink,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(leftX - w * 0.085f, eyeY - h * 0.30f),
                size = Size(w * 0.17f, h * 0.13f),
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round),
            )
        }
        else -> Unit
    }

    // eyes
    if (celebrating) {
        // joyful smile-curve eyes  ◡ ◡
        val eyeW = w * 0.15f
        val eyeH = h * 0.12f
        listOf(leftX, rightX).forEach { ex ->
            drawArc(
                color = Color.White,
                startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(ex - eyeW / 2f, eyeY - eyeH / 2f),
                size = Size(eyeW, eyeH),
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round),
            )
        }
    } else {
        val open = eyeOpen.coerceIn(0.07f, 1f)
        val eyeRx = w * (if (mood == MascotMood.Caring) 0.08f else 0.088f)
        val eyeRy = eyeRx * open
        listOf(leftX, rightX).forEach { cx ->
            drawOval(
                Color.White,
                Offset(cx - eyeRx, eyeY - eyeRy),
                Size(eyeRx * 2f, eyeRy * 2f),
            )
            drawOval(
                Ink,
                Offset(cx - eyeRx * 0.46f, eyeY - eyeRy * 0.62f),
                Size(eyeRx * 0.92f, eyeRy * 1.24f),
            )
            // catchlight — a tiny spark of life in each eye
            if (open > 0.5f) {
                drawCircle(
                    Color.White,
                    eyeRx * 0.2f,
                    Offset(cx - eyeRx * 0.1f, eyeY - eyeRy * 0.34f),
                )
            }
        }
    }

    // mouth
    when (mood) {
        MascotMood.Celebrating -> drawArc(
            color = Color.White,
            startAngle = 18f, sweepAngle = 144f, useCenter = false,
            topLeft = Offset(w * 0.33f, h * 0.44f),
            size = Size(w * 0.34f, h * 0.37f),
            style = Stroke(width = w * 0.058f, cap = StrokeCap.Round),
        )
        MascotMood.Caring -> drawArc(
            color = Color.White,
            startAngle = 28f, sweepAngle = 124f, useCenter = false,
            topLeft = Offset(w * 0.40f, h * 0.53f),
            size = Size(w * 0.20f, h * 0.15f),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
        )
        MascotMood.Curious -> drawArc(
            color = Color.White,
            startAngle = 34f, sweepAngle = 112f, useCenter = false,
            topLeft = Offset(w * 0.43f, h * 0.55f),
            size = Size(w * 0.15f, h * 0.12f),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
        )
        else -> drawArc(
            color = Color.White,
            startAngle = 22f, sweepAngle = 136f, useCenter = false,
            topLeft = Offset(w * 0.35f, h * 0.45f),
            size = Size(w * 0.30f, h * 0.30f),
            style = Stroke(width = w * 0.052f, cap = StrokeCap.Round),
        )
    }
}

/** Lumi, alive — a gentle idle bob with squash-&-stretch, soft breathing, and blinks. */
@Composable
fun AnimatedCoachMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Cheerful,
) {
    val transition = rememberInfiniteTransition(label = "lumi")
    val excited = mood == MascotMood.Celebrating
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (excited) 640 else 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
    val eyeOpen = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2200L + (0..2600).random())
            eyeOpen.animateTo(0.07f, tween(80))
            eyeOpen.animateTo(1f, tween(130))
        }
    }
    CoachMascot(
        modifier = modifier.graphicsLayer {
            translationY = -bob * (if (excited) 12f else 7f).dp.toPx()
            // squash & stretch, synced to the bob — this is what reads as "alive"
            val stretch = (bob - 0.5f) * (if (excited) 0.16f else 0.075f)
            val breathe = breath * 0.02f
            scaleX = 1f - stretch + breathe
            scaleY = 1f + stretch + breathe
        },
        mood = mood,
        eyeOpen = eyeOpen.value,
    )
}

/** A small decorative 4-point sparkle. */
@Composable
fun Sparkle(modifier: Modifier = Modifier, color: Color = Terracotta) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.57f, h * 0.43f, w, h * 0.5f)
            quadraticTo(w * 0.57f, h * 0.57f, w * 0.5f, h)
            quadraticTo(w * 0.43f, h * 0.57f, 0f, h * 0.5f)
            quadraticTo(w * 0.43f, h * 0.43f, w * 0.5f, 0f)
            close()
        }
        drawPath(path, color)
    }
}
