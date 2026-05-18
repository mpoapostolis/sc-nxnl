package com.skincoach.app.ui.screens.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skincoach.app.data.RoutineRepository
import com.skincoach.app.data.bestStreak
import com.skincoach.app.data.currentStreak
import com.skincoach.app.domain.routine.Routine
import com.skincoach.app.domain.routine.RoutineStep
import com.skincoach.app.ui.components.AnimatedCoachMascot
import com.skincoach.app.ui.components.ConfettiBurst
import com.skincoach.app.ui.components.MascotMood
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed interface TodayUi {
    data object Loading : TodayUi
    data object NoScan : TodayUi
    data class Ready(val routine: Routine) : TodayUi
}

@Composable
fun TodayScreen(onBack: () -> Unit, onScanClick: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { RoutineRepository(context) }
    val todayDate = remember { LocalDate.now() }
    val today = remember { todayDate.toEpochDay() }

    val ui by produceState<TodayUi>(TodayUi.Loading) {
        value = repo.todayRoutine()?.let { TodayUi.Ready(it) } ?: TodayUi.NoScan
    }
    val dayRecord by repo.observeDay(today).collectAsStateWithLifecycle(initialValue = null)
    val completedDays by repo.observeCompletedDays()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Paper)) {
        when (val state = ui) {
            TodayUi.Loading -> Unit
            TodayUi.NoScan -> TodayEmptyState(onBack = onBack, onScanClick = onScanClick)
            is TodayUi.Ready -> {
                val routine = state.routine
                val requiredTitles = remember(routine) {
                    (routine.morning + routine.evening)
                        .filter { it.daily }
                        .map { it.title }
                        .toSet()
                }
                TodayContent(
                    routine = routine,
                    dateLabel = remember {
                        todayDate.format(
                            DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()),
                        )
                    },
                    checked = repo.checkedTitlesOf(dayRecord),
                    completedToday = dayRecord?.completed == true,
                    streak = currentStreak(completedDays.toSet(), today),
                    best = bestStreak(completedDays.toSet()),
                    onBack = onBack,
                    onToggle = { title ->
                        scope.launch { repo.toggleStep(today, title, requiredTitles) }
                    },
                )
            }
        }
    }
}

@Composable
private fun TodayContent(
    routine: Routine,
    dateLabel: String,
    checked: Set<String>,
    completedToday: Boolean,
    streak: Int,
    best: Int,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
) {
    var celebrate by remember { mutableStateOf(false) }
    var seenComplete by remember { mutableStateOf<Boolean?>(null) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(completedToday) {
        val prev = seenComplete
        if (prev == null) {
            seenComplete = completedToday
        } else if (!prev && completedToday) {
            seenComplete = true
            celebrate = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val morningCount = routine.morning.size
    val eveningLabelDelay = 270 + morningCount * 45
    val eveningBase = eveningLabelDelay + 45

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(14.dp))
            BackButton(onBack, Modifier.reveal(stagger(0)))
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
                modifier = Modifier.reveal(stagger(55)),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                modifier = Modifier.reveal(stagger(95)),
            )
            Spacer(Modifier.height(22.dp))

            StreakHero(
                streak = streak,
                best = best,
                completedToday = completedToday,
                modifier = Modifier.reveal(stagger(155)),
            )

            Spacer(Modifier.height(32.dp))
            SectionLabel("MORNING", Modifier.reveal(stagger(225)))
            Spacer(Modifier.height(14.dp))
            routine.morning.forEachIndexed { index, step ->
                StepRow(step, step.title in checked, 270 + index * 45, onToggle)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("EVENING", Modifier.reveal(stagger(eveningLabelDelay)))
            Spacer(Modifier.height(14.dp))
            routine.evening.forEachIndexed { index, step ->
                StepRow(step, step.title in checked, eveningBase + index * 45, onToggle)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = "Daily essentials keep your streak — the 2–3×/week extras are a bonus.",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .reveal(stagger(eveningBase + routine.evening.size * 45 + 30)),
            )
            Spacer(Modifier.height(26.dp))
        }

        ConfettiBurst(modifier = Modifier.fillMaxSize(), play = celebrate, intensity = 1f)
    }
}

@Composable
private fun StreakHero(
    streak: Int,
    best: Int,
    completedToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val mood = when {
        completedToday -> MascotMood.Celebrating
        streak > 0 -> MascotMood.Cheerful
        else -> MascotMood.Curious
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(corner = 26.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Cloud)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedCoachMascot(Modifier.size(72.dp), mood)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            if (streak > 0) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "🔥", fontSize = 30.sp)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = if (streak == 1) "day" else "days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = streakLine(streak, completedToday),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )
                if (best > streak) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "best so far · $best days",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkFaint,
                    )
                }
            } else {
                Text(
                    text = "Start your streak",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Do today's routine and watch it grow 🌱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = InkFaint,
        modifier = modifier,
    )
}

@Composable
private fun StepRow(
    step: RoutineStep,
    checked: Boolean,
    entranceDelay: Int,
    onToggle: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .reveal(stagger(entranceDelay))
            .fillMaxWidth()
            .softShadow(corner = 20.dp, elevation = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Cloud)
            .bounceClick(pressScale = 0.97f) { onToggle(step.title) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleCheck(checked)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                if (!step.daily) {
                    Spacer(Modifier.width(8.dp))
                    OccasionalTag()
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = step.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
        }
    }
}

@Composable
private fun CircleCheck(checked: Boolean) {
    val tick by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 520f),
        label = "tick",
    )
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) Terracotta else Cloud)
            .border(1.5.dp, if (checked) Terracotta else Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.graphicsLayer { scaleX = tick; scaleY = tick },
        )
    }
}

@Composable
private fun OccasionalTag() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TerracottaSoft)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            text = "2–3×/week",
            style = MaterialTheme.typography.labelMedium,
            color = TerracottaDeep,
        )
    }
}

@Composable
private fun TodayEmptyState(onBack: () -> Unit, onScanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        BackButton(onBack)
        Spacer(Modifier.weight(1f))
        AnimatedCoachMascot(
            modifier = Modifier.size(96.dp).align(Alignment.CenterHorizontally),
            mood = MascotMood.Curious,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Your routine is waiting",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Take your first scan and I'll build a routine made for your skin — then we start your streak together.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Scan my skin", onClick = onScanClick)
        Spacer(Modifier.weight(1.4f))
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .softShadow(corner = 22.dp, elevation = 4.dp)
            .clip(CircleShape)
            .background(Cloud)
            .bounceClick { onBack() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "←", style = MaterialTheme.typography.titleLarge, color = Ink)
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Terracotta, CircleShape)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

private fun streakLine(streak: Int, completedToday: Boolean): String = when {
    completedToday && streak >= 7 -> "$streak days strong — you're unstoppable ✨"
    completedToday -> "today's done — lovely. see you tomorrow 🌸"
    streak >= 7 -> "don't break it now — tick off today below"
    else -> "keep it going — tick off today below"
}
