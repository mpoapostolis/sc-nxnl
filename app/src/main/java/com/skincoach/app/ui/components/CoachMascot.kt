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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.Terracotta
import kotlinx.coroutines.delay

/** Lumi's moods — the coach reacts to whatever's happening. */
enum class MascotMood { Cheerful, Celebrating, Caring, Curious }

private val LumiLight = Color(0xFFF4BCC0) // soft, bright top-left highlight
private val LumiRim = Color(0xFFC85F6B)   // gentle shading toward the edge

/**
 * Lumi — the Skin Coach mascot. A soft, dewy little face drawn on a Canvas.
 * [mood] swaps her expression; [eyeOpen] is 1f = open, ~0f = mid-blink.
 * Her eyes are always open and bright — that's what keeps her cute at any size.
 */
@Composable
fun CoachMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Cheerful,
    eyeOpen: Float = 1f,
) {
    val tilt = when (mood) {
        MascotMood.Curious -> -8f
        MascotMood.Caring -> -5f
        else -> 0f
    }
    Canvas(modifier.graphicsLayer { rotationZ = tilt }) {
        drawLumi(mood, eyeOpen)
    }
}

private fun DrawScope.drawLumi(mood: MascotMood, eyeOpen: Float) {
    val w = size.width
    val h = size.height
    val celebrating = mood == MascotMood.Celebrating
    val cx = w / 2f
    val cy = h / 2f
    val headR = w * 0.46f

    // head — a soft, bright, gently shaded sphere (light from the top-left)
    drawCircle(
        brush = Brush.radialGradient(
            0f to LumiLight,
            0.52f to Terracotta,
            1f to LumiRim,
            center = Offset(w * 0.40f, h * 0.34f),
            radius = headR * 1.9f,
        ),
        radius = headR,
        center = Offset(cx, cy),
    )
    // dewy glass highlights
    drawCircle(Color.White.copy(alpha = 0.42f), w * 0.135f, Offset(w * 0.35f, h * 0.30f))
    drawCircle(Color.White.copy(alpha = 0.25f), w * 0.055f, Offset(w * 0.63f, h * 0.24f))

    // soft blush cheeks — gentle, never blotchy
    val cheekY = h * 0.63f
    val cheekR = w * (if (celebrating) 0.105f else 0.092f)
    val cheekAlpha = if (celebrating) 0.40f else 0.30f
    listOf(w * 0.30f, w * 0.70f).forEach { ccx ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LumiRim.copy(alpha = cheekAlpha), Color.Transparent),
                center = Offset(ccx, cheekY),
                radius = cheekR,
            ),
            radius = cheekR,
            center = Offset(ccx, cheekY),
        )
    }

    val eyeY = h * 0.45f
    val leftX = w * 0.37f
    val rightX = w * 0.63f

    // one playfully raised brow — only when she's curious
    if (mood == MascotMood.Curious) {
        drawArc(
            color = Ink,
            startAngle = 200f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(leftX - w * 0.085f, eyeY - h * 0.31f),
            size = Size(w * 0.17f, h * 0.13f),
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round),
        )
    }

    // eyes — always open, round and glossy. bright eyes = always cute.
    val open = eyeOpen.coerceIn(0.07f, 1f)
    val eyeRx = w * (if (celebrating) 0.103f else 0.097f)
    val eyeRy = eyeRx * open
    listOf(leftX, rightX).forEach { ex ->
        // white of the eye
        drawOval(
            Color.White,
            Offset(ex - eyeRx, eyeY - eyeRy),
            Size(eyeRx * 2f, eyeRy * 2f),
        )
        // pupil — centred and focused
        val pupRx = eyeRx * 0.52f
        val pupRy = eyeRy * 0.66f
        drawOval(
            Ink,
            Offset(ex - pupRx, eyeY - pupRy),
            Size(pupRx * 2f, pupRy * 2f),
        )
        // glossy catchlights — a big one and a tiny one
        if (open > 0.45f) {
            drawCircle(
                Color.White,
                eyeRx * 0.27f,
                Offset(ex - pupRx * 0.42f, eyeY - pupRy * 0.55f),
            )
            drawCircle(
                Color.White.copy(alpha = 0.72f),
                eyeRx * 0.13f,
                Offset(ex + pupRx * 0.36f, eyeY + pupRy * 0.34f),
            )
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
            startAngle = 26f, sweepAngle = 128f, useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.49f),
            size = Size(w * 0.24f, h * 0.21f),
            style = Stroke(width = w * 0.052f, cap = StrokeCap.Round),
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

/**
 * Lumi, alive — an idle bob with squash-&-stretch, soft breathing, a gentle
 * side-to-side sway, and the occasional blink.
 */
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
            animation = tween(if (excited) 620 else 1900, easing = FastOutSlowInEasing),
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
    val sway by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (excited) 520 else 2700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sway",
    )
    val eyeOpen = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2200L + (0..2600).random())
            eyeOpen.animateTo(0.07f, tween(80))
            eyeOpen.animateTo(1f, tween(130))
        }
    }
    val swayDeg = if (excited) 9f else 4.5f
    CoachMascot(
        modifier = modifier.graphicsLayer {
            translationY = -bob * (if (excited) 12f else 7f).dp.toPx()
            // squash & stretch, synced to the bob — this is what reads as "alive"
            val stretch = (bob - 0.5f) * (if (excited) 0.16f else 0.075f)
            val breathe = breath * 0.02f
            scaleX = 1f - stretch + breathe
            scaleY = 1f + stretch + breathe
            rotationZ = (sway - 0.5f) * swayDeg
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
