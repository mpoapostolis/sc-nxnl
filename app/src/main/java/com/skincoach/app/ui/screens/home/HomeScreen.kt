package com.skincoach.app.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skincoach.app.data.RoutineRepository
import com.skincoach.app.data.currentStreak
import com.skincoach.app.ui.components.AnimatedCoachMascot
import com.skincoach.app.ui.components.MascotMood
import com.skincoach.app.ui.components.Sparkle
import com.skincoach.app.ui.components.bounceClick
import com.skincoach.app.ui.components.reveal
import com.skincoach.app.ui.components.softShadow
import com.skincoach.app.ui.components.stagger
import com.skincoach.app.ui.theme.Cloud
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.InkFaint
import com.skincoach.app.ui.theme.InkSoft
import com.skincoach.app.ui.theme.Paper
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import com.skincoach.app.ui.theme.TerracottaSoft
import java.time.LocalDate
import java.util.Calendar

/** A warm, time-aware hello in Lumi's voice. */
private fun timeGreeting(): String =
    when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "good morning"
        in 12..17 -> "good afternoon"
        in 18..21 -> "good evening"
        else -> "hi, night owl"
    }

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onTodayClick: () -> Unit,
) {
    val context = LocalContext.current
    val routineRepo = remember { RoutineRepository(context) }
    val completedDays by routineRepo.observeCompletedDays()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val streak = currentStreak(completedDays.toSet(), remember { LocalDate.now().toEpochDay() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.28f)
            val radius = size.minDimension * 1.1f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaSoft.copy(alpha = 0.7f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.reveal(stagger(0)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).background(Terracotta, CircleShape))
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "SKIN COACH",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSoft,
                )
            }

            Spacer(Modifier.weight(0.5f))

            Box(
                modifier = Modifier.size(186.dp).reveal(stagger(90)),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedCoachMascot(Modifier.size(126.dp), MascotMood.Cheerful)
                Sparkle(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 6.dp)
                        .size(21.dp),
                    color = Terracotta,
                )
                Sparkle(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 26.dp, start = 10.dp)
                        .size(13.dp),
                    color = TerracottaDeep,
                )
            }

            Spacer(Modifier.height(26.dp))

            Column(
                modifier = Modifier.reveal(stagger(170)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Your skin,", style = MaterialTheme.typography.displayMedium, color = Ink)
                Text("scored.", style = MaterialTheme.typography.displayMedium, color = Terracotta)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "${timeGreeting()} — let's check in on your skin ✨",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .reveal(stagger(240)),
            )

            Spacer(Modifier.weight(1f))

            ScanButton(onClick = onScanClick, modifier = Modifier.reveal(stagger(330)))

            Spacer(Modifier.height(14.dp))

            TodayButton(
                streak = streak,
                onClick = onTodayClick,
                modifier = Modifier.reveal(stagger(385)),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "My progress",
                style = MaterialTheme.typography.titleMedium,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .reveal(stagger(435))
                    .clip(CircleShape)
                    .bounceClick { onHistoryClick() }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Free  ·  No sign-up  ·  Private on your device",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().reveal(stagger(480)),
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ScanButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Terracotta, CircleShape)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Scan my skin", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("→", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

@Composable
private fun TodayButton(streak: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .softShadow(corner = 28.dp, elevation = 6.dp)
            .clip(CircleShape)
            .background(Cloud)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (streak > 0) {
                Text(text = "🔥", fontSize = 17.sp)
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = Terracotta,
                )
                Spacer(Modifier.width(9.dp))
            }
            Text(
                text = "Today's routine",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
        }
    }
}
