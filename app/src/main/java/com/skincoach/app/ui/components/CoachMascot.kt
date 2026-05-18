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
import com.skincoach.app.ui.theme.TerracottaDeep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Lumi's moods — the coach reacts to whatever's happening. */
enum class MascotMood { Cheerful, Celebrating, Caring, Curious }

/** A soft highlight for Lumi's cheeks. */
private val LumiHighlight = Color(0xFFEDA9AD)

/**
 * Lumi — the Skin Coach mascot. A soft, dewy little face drawn on a Canvas.
 * [mood] swaps her whole expression; [eyeOpen] is 1f = open, ~0f = mid-blink;
 * [gaze] (x,y each -1f..1f) drifts her pupils so she looks around.
 */
@Composable
fun CoachMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Cheerful,
    eyeOpen: Float = 1f,
    gaze: Offset = Offset.Zero,
) {
    val tilt = if (mood == MascotMood.Curious) -8f else 0f
    Canvas(modifier.graphicsLayer { rotationZ = tilt }) {
        drawLumi(mood, eyeOpen, gaze)
    }
}

private fun DrawScope.drawLumi(mood: MascotMood, eyeOpen: Float, gaze: Offset) {
    val w = size.width
    val h = size.height
    val celebrating = mood == MascotMood.Celebrating
    val cx = w / 2f
    val cy = h / 2f
    val headR = w * 0.46f

    // head — a soft, dimensional sphere (light from the top-left)
    drawCircle(
        brush = Brush.radialGradient(
            0f to LumiHighlight,
            0.58f to Terracotta,
            1f to TerracottaDeep,
            center = Offset(w * 0.39f, h * 0.35f),
            radius = headR * 1.7f,
        ),
        radius = headR,
        center = Offset(cx, cy),
    )
    // glossy dewy highlights
    drawCircle(Color.White.copy(alpha = 0.38f), w * 0.13f, Offset(w * 0.34f, h * 0.29f))
    drawCircle(Color.White.copy(alpha = 0.22f), w * 0.055f, Offset(w * 0.62f, h * 0.23f))

    // soft blush cheeks — a gentle gradient, not a flat blob
    val cheekY = h * 0.62f
    val cheekR = w * (if (celebrating) 0.135f else 0.118f)
    val cheekAlpha = if (celebrating) 0.62f else 0.42f
    listOf(w * 0.27f, w * 0.73f).forEach { ccx ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TerracottaDeep.copy(alpha = cheekAlpha), Color.Transparent),
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

    // eyebrows
    when (mood) {
        MascotMood.Caring -> {
            // gentle, kind brows — inner ends lifted, never angry
            drawLine(
                Ink,
                Offset(leftX - w * 0.075f, eyeY - h * 0.16f),
                Offset(leftX + w * 0.065f, eyeY - h * 0.22f),
                strokeWidth = w * 0.042f, cap = StrokeCap.Round,
            )
            drawLine(
                Ink,
                Offset(rightX + w * 0.075f, eyeY - h * 0.16f),
                Offset(rightX - w * 0.065f, eyeY - h * 0.22f),
                strokeWidth = w * 0.042f, cap = StrokeCap.Round,
            )
        }
        MascotMood.Curious -> {
            // one playfully raised brow
            drawArc(
                color = Ink,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(leftX - w * 0.085f, eyeY - h * 0.31f),
                size = Size(w * 0.17f, h * 0.13f),
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round),
            )
        }
        else -> Unit
    }

    // eyes
    if (celebrating) {
        // joyful smile-curve eyes  ◡ ◡
        val eyeW = w * 0.155f
        val eyeH = h * 0.12f
        listOf(leftX, rightX).forEach { ex ->
            drawArc(
                color = Color.White,
                startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(ex - eyeW / 2f, eyeY - eyeH / 2f),
                size = Size(eyeW, eyeH),
                style = Stroke(width = w * 0.056f, cap = StrokeCap.Round),
            )
        }
    } else {
        val open = eyeOpen.coerceIn(0.07f, 1f)
        val eyeRx = w * (if (mood == MascotMood.Caring) 0.088f else 0.096f)
        val eyeRy = eyeRx * open
        val pupilDx = gaze.x * eyeRx * 0.4f
        val pupilDy = gaze.y * eyeRy * 0.4f
        listOf(leftX, rightX).forEach { ex ->
            // white of the eye
            drawOval(
                Color.White,
                Offset(ex - eyeRx, eyeY - eyeRy),
                Size(eyeRx * 2f, eyeRy * 2f),
            )
            // pupil — follows her gaze
            val pupRx = eyeRx * 0.52f
            val pupRy = eyeRy * 0.66f
            val px = ex + pupilDx
            val py = eyeY + pupilDy
            drawOval(
                Ink,
                Offset(px - pupRx, py - pupRy),
                Size(pupRx * 2f, pupRy * 2f),
            )
            // glossy catchlights — a big one and a tiny one
            if (open > 0.45f) {
                drawCircle(
                    Color.White,
                    eyeRx * 0.25f,
                    Offset(px - pupRx * 0.4f, py - pupRy * 0.52f),
                )
                drawCircle(
                    Color.White.copy(alpha = 0.7f),
                    eyeRx * 0.12f,
                    Offset(px + pupRx * 0.34f, py + pupRy * 0.36f),
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

/**
 * Lumi, alive — an idle bob with squash-&-stretch, soft breathing, a gentle
 * side-to-side sway, blinks, and eyes that drift around on their own.
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
    // eyes that wander — a tiny dart, hold, then back to centre
    val gazeX = remember { Animatable(0f) }
    val gazeY = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2600L + (0..3400).random())
            val tx = (Random.nextFloat() * 2f - 1f) * 0.75f
            val ty = (Random.nextFloat() * 2f - 1f) * 0.45f
            launch { gazeX.animateTo(tx, tween(260)) }
            gazeY.animateTo(ty, tween(260))
            delay(820)
            launch { gazeX.animateTo(0f, tween(240)) }
            gazeY.animateTo(0f, tween(240))
        }
    }
    val swayDeg = if (excited) 10f else 5f
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
        gaze = Offset(gazeX.value, gazeY.value),
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
