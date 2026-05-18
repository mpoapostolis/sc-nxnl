package com.skincoach.app.ui.screens.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skincoach.app.data.ScanRepository
import com.skincoach.app.data.db.ScanEntity
import com.skincoach.app.ui.components.AnimatedCoachMascot
import com.skincoach.app.ui.components.MascotMood
import com.skincoach.app.ui.components.bounceClick
import com.skincoach.app.ui.components.softShadow
import com.skincoach.app.ui.theme.Cloud
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.InkFaint
import com.skincoach.app.ui.theme.InkSoft
import com.skincoach.app.ui.theme.OvalShape
import com.skincoach.app.ui.theme.Paper
import com.skincoach.app.ui.theme.Sage
import com.skincoach.app.ui.theme.Sand
import com.skincoach.app.ui.theme.Terracotta
import com.skincoach.app.ui.theme.TerracottaDeep
import com.skincoach.app.util.loadOrientedBitmap
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scans by repository.observeScans()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<ScanEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .softShadow(corner = 22.dp, elevation = 4.dp)
                .clip(CircleShape)
                .background(Cloud)
                .bounceClick { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "←", style = MaterialTheme.typography.titleLarge, color = Ink)
        }
        Spacer(Modifier.height(18.dp))
        Text("Your progress", style = MaterialTheme.typography.headlineLarge, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Your Skin Score, tracked over time.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
        )
        Spacer(Modifier.height(26.dp))

        if (scans.isEmpty()) {
            EmptyState()
        } else {
            CoachCard(message = coachMessage(scans), mood = coachMood(scans))
            Spacer(Modifier.height(20.dp))
            TrendCard(scans)
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "EVERY GLOW CHECK",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkFaint,
                )
                Text(
                    text = "long-press to delete",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkFaint,
                )
            }
            Spacer(Modifier.height(14.dp))
            scans.forEach { scan ->
                ScanRow(scan, onLongPress = { pendingDelete = scan })
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    pendingDelete?.let { scan ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this scan?") },
            text = { Text("It will be removed from your history and your trend.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repository.delete(scan) }
                        pendingDelete = null
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CoachCard(message: String, mood: MascotMood) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(corner = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Cloud)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedCoachMascot(Modifier.size(58.dp), mood)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "LUMI",
                style = MaterialTheme.typography.labelMedium,
                color = TerracottaDeep,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
        }
    }
}

@Composable
private fun TrendCard(scansNewestFirst: List<ScanEntity>) {
    val scores = scansNewestFirst.map { it.overallScore }.reversed()
    val current = scores.last()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(corner = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Cloud)
            .padding(20.dp),
    ) {
        Text(
            text = "SKIN SCORE OVER TIME",
            style = MaterialTheme.typography.labelMedium,
            color = InkFaint,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.displayMedium,
                color = Ink,
            )
            if (scores.size >= 2) {
                val delta = current - scores.first()
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (delta >= 0) "+$delta since your first scan" else "$delta since your first scan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (delta >= 0) Sage else TerracottaDeep,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        if (scores.size >= 2) {
            TrendChart(
                scores = scores,
                modifier = Modifier.fillMaxWidth().height(128.dp),
            )
        } else {
            Text(
                text = "Scan again in a few days — your trend line shows up once you have two scans.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
        }
    }
}

@Composable
private fun TrendChart(scores: List<Int>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        if (scores.size < 2) return@Canvas
        val lo = (scores.min() - 6).coerceAtLeast(0)
        val hi = (scores.max() + 6).coerceAtMost(100)
        val range = (hi - lo).coerceAtLeast(1)
        val n = scores.size
        val stepX = size.width / (n - 1)
        val pad = 8.dp.toPx()

        val pts = List(n) { i ->
            val norm = (scores[i] - lo).toFloat() / range
            Offset(stepX * i, size.height - pad - norm * (size.height - 2 * pad))
        }

        // a soft, smooth curve through the scores
        val line = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until n) {
                val midX = (pts[i - 1].x + pts[i].x) / 2f
                val midY = (pts[i - 1].y + pts[i].y) / 2f
                quadraticTo(pts[i - 1].x, pts[i - 1].y, midX, midY)
            }
            lineTo(pts[n - 1].x, pts[n - 1].y)
        }

        // gentle gradient fill beneath the curve
        val fill = Path().apply {
            addPath(line)
            lineTo(pts[n - 1].x, size.height)
            lineTo(pts[0].x, size.height)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(Terracotta.copy(alpha = 0.30f), Color.Transparent),
            ),
        )
        drawPath(
            path = line,
            color = Terracotta,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // a single "you are here" dot on the latest score
        val last = pts[n - 1]
        drawCircle(color = Cloud, radius = 7.dp.toPx(), center = last)
        drawCircle(color = Terracotta, radius = 4.5.dp.toPx(), center = last)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScanRow(scan: ScanEntity, onLongPress: () -> Unit) {
    val thumb = remember(scan.photoPath) {
        loadOrientedBitmap(scan.photoPath, maxSize = 256)?.asImageBitmap()
    }
    val date = remember(scan.timestamp) {
        SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(corner = 20.dp, elevation = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Cloud)
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (thumb != null) {
            Image(
                bitmap = thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 48.dp, height = 60.dp).clip(OvalShape),
            )
        } else {
            Box(
                Modifier.size(width = 48.dp, height = 60.dp).clip(OvalShape).background(Sand),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Skin Score", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(date, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        }
        Text(
            text = "${scan.overallScore}",
            style = MaterialTheme.typography.headlineMedium,
            color = Terracotta,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedCoachMascot(Modifier.size(96.dp), MascotMood.Curious)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No scans yet",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Take your first scan and I'll start tracking your skin journey here.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.82f),
        )
    }
}

private fun coachMessage(scans: List<ScanEntity>): String {
    if (scans.isEmpty()) return "Let's start your skin journey — take your first scan!"
    if (scans.size == 1) {
        return "Great start! Come back in a few days and scan again — that's when your progress shows up."
    }
    val diff = scans[0].overallScore - scans[1].overallScore
    return when {
        diff >= 4 -> "Brilliant — you're up $diff points! Whatever you're doing, keep it going."
        diff in 1..3 -> "Nice — a little up since last time. Small steps add up. Keep at it!"
        diff == 0 -> "Steady — you're holding your score. Consistency is the whole game."
        diff in -3..-1 -> "Skin has off days — don't sweat it. Stick with your routine and it comes back."
        else -> "A dip this time — totally normal. Stay with your routine, you've got this."
    }
}

private fun coachMood(scans: List<ScanEntity>): MascotMood {
    if (scans.size < 2) return MascotMood.Curious
    val diff = scans[0].overallScore - scans[1].overallScore
    return when {
        diff >= 1 -> MascotMood.Celebrating
        diff <= -1 -> MascotMood.Caring
        else -> MascotMood.Cheerful
    }
}
