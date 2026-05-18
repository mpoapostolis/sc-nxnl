package com.skincoach.app.ui.screens.result

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skincoach.app.data.ScanRepository
import com.skincoach.app.domain.AnalysisResult
import com.skincoach.app.domain.ConcernScore
import com.skincoach.app.domain.SkinAnalysis
import com.skincoach.app.domain.SkincareTips
import com.skincoach.app.domain.analysis.HeuristicSkinAnalyzer
import com.skincoach.app.domain.routine.Routine
import com.skincoach.app.domain.routine.RoutineGenerator
import com.skincoach.app.domain.routine.RoutineStep
import com.skincoach.app.ui.components.AnimatedCoachMascot
import com.skincoach.app.ui.components.CoachMascot
import com.skincoach.app.ui.components.ConfettiBurst
import com.skincoach.app.ui.components.MascotMood
import com.skincoach.app.ui.components.Sparkle
import com.skincoach.app.ui.components.bounceClick
import com.skincoach.app.ui.components.reveal
import com.skincoach.app.ui.components.stagger
import com.skincoach.app.ui.theme.Cloud
import com.skincoach.app.ui.theme.ErrorRed
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.InkFaint
import com.skincoach.app.ui.theme.InkSoft
import com.skincoach.app.ui.theme.OvalShape
import com.skincoach.app.ui.theme.Paper
import com.skincoach.app.ui.theme.Sage
import com.skincoach.app.ui.theme.Sand
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import com.skincoach.app.ui.theme.TerracottaSoft
import com.skincoach.app.util.loadOrientedBitmap
import com.skincoach.app.util.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Ready(
        val photo: ImageBitmap,
        val analysis: SkinAnalysis,
        val routine: Routine,
    ) : ResultUiState
    data object NoFace : ResultUiState
    data object Failed : ResultUiState
    data class Retake(val message: String) : ResultUiState
}

@Composable
fun ResultScreen(
    photoPath: String?,
    onRescan: () -> Unit,
    onDone: () -> Unit,
) {
    val analyzer = remember { HeuristicSkinAnalyzer() }
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val state by produceState<ResultUiState>(ResultUiState.Loading, photoPath) {
        val path = photoPath
        if (path == null) {
            value = ResultUiState.Failed
            return@produceState
        }
        val bitmap = withContext(Dispatchers.IO) { loadOrientedBitmap(path) }
        if (bitmap == null) {
            value = ResultUiState.Failed
            return@produceState
        }
        value = when (val result = analyzer.analyze(bitmap)) {
            is AnalysisResult.Success -> {
                val cropPath = withContext(Dispatchers.IO) {
                    saveBitmapToFile(
                        context,
                        result.faceCrop,
                        "face_${System.currentTimeMillis()}.jpg",
                    )
                }
                runCatching { repository.save(cropPath ?: path, result.analysis) }
                ResultUiState.Ready(
                    photo = result.faceCrop.asImageBitmap(),
                    analysis = result.analysis,
                    routine = RoutineGenerator.generate(result.analysis),
                )
            }
            AnalysisResult.NoFace -> ResultUiState.NoFace
            AnalysisResult.Failed -> ResultUiState.Failed
            is AnalysisResult.LowQuality -> ResultUiState.Retake(result.message)
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        when (val s = state) {
            ResultUiState.Loading -> LoadingState()
            is ResultUiState.Ready -> ResultContent(s, onRescan, onDone)
            ResultUiState.NoFace -> ErrorState(
                title = "I couldn't find your face",
                message = "Make sure your whole face is in frame with even lighting, then let's try again.",
                onRescan = onRescan,
            )
            ResultUiState.Failed -> ErrorState(
                title = "That one didn't work",
                message = "Something went wrong reading the photo. Let's take another together.",
                onRescan = onRescan,
            )
            is ResultUiState.Retake -> ErrorState(
                title = "Let's retake that one",
                message = s.message,
                onRescan = onRescan,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    val transition = rememberInfiniteTransition(label = "loading")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit",
    )
    val lines = listOf(
        "finding your face…",
        "reading your tone & glow…",
        "measuring texture & clarity…",
        "almost there ✨",
    )
    var lineIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1300)
            lineIndex = (lineIndex + 1) % lines.size
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = orbit }) {
                Sparkle(
                    modifier = Modifier.align(Alignment.TopCenter).size(17.dp),
                    color = Terracotta,
                )
                Sparkle(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(12.dp),
                    color = TerracottaDeep,
                )
                Sparkle(
                    modifier = Modifier.align(Alignment.CenterStart).size(10.dp),
                    color = Terracotta,
                )
            }
            CoachMascot(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse },
                mood = MascotMood.Curious,
            )
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = "Reading your skin",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Spacer(Modifier.height(8.dp))
        Crossfade(targetState = lines[lineIndex], label = "loadingLine") { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ErrorState(title: String, message: String, onRescan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedCoachMascot(Modifier.size(78.dp), MascotMood.Caring)
        Spacer(Modifier.height(22.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f),
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Scan again", onClick = onRescan)
    }
}

@Composable
private fun ResultContent(
    state: ResultUiState.Ready,
    onRescan: () -> Unit,
    onDone: () -> Unit,
) {
    val score = state.analysis.overallScore
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "reveal",
    )

    var celebrate by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        delay(1120)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        celebrate = true
    }

    val focus = state.analysis.concerns.minByOrNull { it.score }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "TODAY'S GLOW CHECK",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                modifier = Modifier.fillMaxWidth().reveal(stagger(0)),
            )
            Spacer(Modifier.height(18.dp))
            Image(
                bitmap = state.photo,
                contentDescription = "Your scan",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .reveal(stagger(90))
                    .size(width = 96.dp, height = 122.dp)
                    .clip(OvalShape),
            )
            Spacer(Modifier.height(22.dp))
            ScoreRing(
                score = score,
                grade = state.analysis.grade,
                progress = progress,
            )
            Spacer(Modifier.height(20.dp))
            LumiSays(
                mood = moodForScore(score),
                line = lumiLine(score),
                modifier = Modifier.reveal(stagger(1000)),
            )
            if (focus != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Let's give your ${focus.concern.label.lowercase()} a little love.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(38.dp))
            SectionLabel("THE BREAKDOWN")
            Spacer(Modifier.height(18.dp))
            state.analysis.concerns.forEachIndexed { index, concern ->
                ConcernRow(concern, index)
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("YOUR ROUTINE")
            Spacer(Modifier.height(18.dp))
            RoutineSection("Morning", state.routine.morning)
            Spacer(Modifier.height(22.dp))
            RoutineSection("Evening", state.routine.evening)

            Spacer(Modifier.height(30.dp))
            TipsSection(state.analysis)

            Spacer(Modifier.height(34.dp))
            PrimaryButton(text = "Scan again", onClick = onRescan)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Back to home",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onDone() },
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "An at-a-glance glow check — not a medical diagnosis. For real concerns, see a dermatologist.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Analyzed privately on your device — nothing is uploaded.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
        }

        ConfettiBurst(
            modifier = Modifier.fillMaxSize(),
            play = celebrate,
            intensity = confettiFor(score),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = InkFaint,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LumiSays(mood: MascotMood, line: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Cloud)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedCoachMascot(Modifier.size(46.dp), mood)
        Spacer(Modifier.width(12.dp))
        Text(
            text = line,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
        )
    }
}

@Composable
private fun ScoreRing(score: Int, grade: String, progress: Float) {
    val pop = remember { Animatable(1f) }
    val done = progress >= 1f
    LaunchedEffect(done) {
        if (done) {
            pop.animateTo(1.13f, tween(120, easing = FastOutSlowInEasing))
            pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 320f))
        }
    }
    Box(Modifier.size(216.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Sand,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = Terracotta,
                startAngle = -90f,
                sweepAngle = progress * (score / 100f) * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
        ) {
            Text(
                text = "${(progress * score).roundToInt()}",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 70.sp, lineHeight = 72.sp),
                color = Ink,
            )
            Text(
                text = grade.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Terracotta,
            )
        }
    }
}

@Composable
private fun ConcernRow(item: ConcernScore, index: Int) {
    val fill = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(520L + index * 95L)
        fill.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.62f, stiffness = 210f),
        )
    }
    val shown = fill.value.coerceIn(0f, 1f)
    val barColor = when {
        item.score >= 75 -> Sage
        item.score >= 55 -> Terracotta
        else -> ErrorRed
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = shown
                translationY = (1f - shown) * 14.dp.toPx()
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.concern.label,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Text(
                text = "${(shown * item.score).roundToInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = InkSoft,
            )
        }
        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(CircleShape)
                .background(Sand),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((shown * item.score / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun RoutineSection(title: String, steps: List<RoutineStep>) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Terracotta,
        )
        Spacer(Modifier.height(12.dp))
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(TerracottaSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TerracottaDeep,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = step.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsSection(analysis: SkinAnalysis) {
    val worst = analysis.concerns.sortedBy { it.score }.take(2)
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoachMascot(Modifier.size(32.dp), MascotMood.Cheerful)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "LUMI'S TIPS",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
            )
        }
        Spacer(Modifier.height(16.dp))
        worst.forEach { concernScore ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Cloud)
                    .padding(16.dp),
            ) {
                Text(
                    text = "For your ${concernScore.concern.label.lowercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                SkincareTips.forConcern(concernScore.concern).take(2).forEach { tip ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("•", style = MaterialTheme.typography.bodyMedium, color = Terracotta)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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

private fun moodForScore(score: Int): MascotMood = when {
    score >= 78 -> MascotMood.Celebrating
    score >= 58 -> MascotMood.Cheerful
    else -> MascotMood.Caring
}

private fun confettiFor(score: Int): Float = when {
    score >= 78 -> 1f
    score >= 60 -> 0.55f
    else -> 0f
}

private fun lumiLine(score: Int): String = when {
    score >= 85 -> "you're absolutely glowing today ✨"
    score >= 78 -> "ooh, lovely skin day — keep it going!"
    score >= 65 -> "looking good — let's nudge this even higher"
    score >= 50 -> "a solid base — we'll grow this together 🌱"
    else -> "skin has off days — i've got you 🌸"
}
