package com.skincoach.app.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import com.skincoach.app.ui.theme.Line
import com.skincoach.app.ui.theme.Paper
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import com.skincoach.app.ui.theme.TerracottaSoft
import java.time.LocalDate
import java.util.Calendar

private val Concerns = listOf(
    "Acne", "Fine lines", "Pores", "Dark spots", "Redness", "Oiliness",
)

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
            val center = Offset(size.width * 0.5f, size.height * 0.26f)
            val radius = size.minDimension * 1.05f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaSoft.copy(alpha = 0.75f), Color.Transparent),
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
                .padding(horizontal = 24.dp, vertical = 10.dp),
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

            Spacer(Modifier.weight(0.35f))

            Box(
                modifier = Modifier.size(172.dp).reveal(stagger(80)),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedCoachMascot(Modifier.size(118.dp), MascotMood.Cheerful)
                Sparkle(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 8.dp)
                        .size(22.dp),
                    color = Terracotta,
                )
                Sparkle(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 22.dp, start = 12.dp)
                        .size(14.dp),
                    color = TerracottaDeep,
                )
                Sparkle(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(10.dp),
                    color = Terracotta,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Lumi says hello, in her own warm little voice
            Box(
                modifier = Modifier
                    .reveal(stagger(140))
                    .clip(CircleShape)
                    .background(Cloud)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) {
                Text(
                    text = "${timeGreeting()} — i'm lumi ✨",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                )
            }

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier.reveal(stagger(200)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Your skin,", style = MaterialTheme.typography.displayMedium, color = Ink)
                Text("scored.", style = MaterialTheme.typography.displayMedium, color = Terracotta)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Take a selfie — get your Skin Score and a routine made just for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .reveal(stagger(260)),
            )

            Spacer(Modifier.height(30.dp))

            ConcernSection(modifier = Modifier.reveal(stagger(320)))

            Spacer(Modifier.weight(1f))

            ScanButton(onClick = onScanClick, modifier = Modifier.reveal(stagger(390)))

            Spacer(Modifier.height(12.dp))

            TodayButton(
                streak = streak,
                onClick = onTodayClick,
                modifier = Modifier.reveal(stagger(430)),
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = "My progress",
                onClick = onHistoryClick,
                modifier = Modifier.reveal(stagger(470)),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Free  ·  No sign-up",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().reveal(stagger(500)),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Private — your photos never leave your device",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().reveal(stagger(500)),
            )

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ConcernSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "WHAT IT READS",
            style = MaterialTheme.typography.labelMedium,
            color = InkFaint,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            Concerns.take(3).forEach { ConcernPill(it) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            Concerns.drop(3).forEach { ConcernPill(it) }
        }
    }
}

@Composable
private fun ConcernPill(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Line, CircleShape)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    }
}

@Composable
private fun ScanButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
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
            .height(54.dp)
            .softShadow(corner = 27.dp, elevation = 5.dp)
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

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .border(1.5.dp, Line, CircleShape)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Ink)
    }
}
