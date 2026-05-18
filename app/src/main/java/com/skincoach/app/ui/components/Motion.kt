package com.skincoach.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A staggered entrance value 0f → 1f that springs in after [delayMillis].
 * Pair with [reveal] for a soft cascade of cards as a screen loads.
 */
@Composable
fun stagger(delayMillis: Int): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessLow),
        )
    }
    return anim.value
}

/** Fades and lifts content gently into place — drive it with [stagger]. */
fun Modifier.reveal(progress: Float): Modifier = graphicsLayer {
    alpha = progress.coerceIn(0f, 1f)
    translationY = (1f - progress) * 26.dp.toPx()
}

/**
 * A clickable that springs down on press and bounces back, with a soft haptic tap.
 * One modifier makes every button in the app feel gummy and alive.
 */
fun Modifier.bounceClick(
    pressScale: Float = 0.94f,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressScale else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "bounce",
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
}
