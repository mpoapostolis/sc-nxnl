package com.skincoach.app.data

import android.content.Context
import com.skincoach.app.data.db.RoutineDayEntity
import com.skincoach.app.data.db.ScanEntity
import com.skincoach.app.data.db.SkinCoachDatabase
import com.skincoach.app.domain.Concern
import com.skincoach.app.domain.ConcernScore
import com.skincoach.app.domain.SkinAnalysis
import com.skincoach.app.domain.routine.Routine
import com.skincoach.app.domain.routine.RoutineGenerator
import kotlinx.coroutines.flow.Flow

private const val SEPARATOR = "\n"

/** Saves and retrieves the daily routine check-ins that power the streak. */
class RoutineRepository(context: Context) {

    private val db = SkinCoachDatabase.get(context)
    private val dao = db.routineDayDao()
    private val scanDao = db.scanDao()

    /** The routine to follow today, derived from the most recent scan (null if none yet). */
    suspend fun todayRoutine(): Routine? {
        val scan = scanDao.latest() ?: return null
        return RoutineGenerator.generate(scan.toAnalysis())
    }

    /** This day's check-in record, or null if nothing's been ticked yet. */
    fun observeDay(epochDay: Long): Flow<RoutineDayEntity?> = dao.observe(epochDay)

    /** Every day the daily routine was fully completed — drives the streak. */
    fun observeCompletedDays(): Flow<List<Long>> = dao.observeCompletedDays()

    /** The set of step titles checked for a given day's record. */
    fun checkedTitlesOf(day: RoutineDayEntity?): Set<String> =
        day?.checkedTitles
            ?.split(SEPARATOR)
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()

    /**
     * Toggles one step for [epochDay] and recomputes whether the day is complete.
     * [requiredTitles] are the daily-essential steps that gate completion.
     */
    suspend fun toggleStep(epochDay: Long, title: String, requiredTitles: Set<String>) {
        val checked = checkedTitlesOf(dao.getOnce(epochDay)).toMutableSet()
        if (title in checked) checked.remove(title) else checked.add(title)
        val completed = requiredTitles.isNotEmpty() && checked.containsAll(requiredTitles)
        dao.upsert(
            RoutineDayEntity(
                epochDay = epochDay,
                checkedTitles = checked.joinToString(SEPARATOR),
                completed = completed,
            ),
        )
    }
}

private fun ScanEntity.toAnalysis(): SkinAnalysis = SkinAnalysis(
    overallScore = overallScore,
    concerns = listOf(
        ConcernScore(Concern.ACNE, acne),
        ConcernScore(Concern.FINE_LINES, fineLines),
        ConcernScore(Concern.PORES, pores),
        ConcernScore(Concern.DARK_SPOTS, darkSpots),
        ConcernScore(Concern.REDNESS, redness),
        ConcernScore(Concern.OILINESS, oiliness),
    ),
)

/**
 * The current run of completed days ending at today — or yesterday, since today
 * may still be in progress. One missed day is forgiven; a second breaks the streak.
 */
fun currentStreak(completedDays: Set<Long>, today: Long): Int {
    val anchor = when {
        today in completedDays -> today
        (today - 1) in completedDays -> today - 1
        (today - 2) in completedDays -> today - 2
        else -> return 0
    }
    return runEndingAt(completedDays, anchor)
}

/** The longest streak ever reached. */
fun bestStreak(completedDays: Set<Long>): Int =
    completedDays.maxOfOrNull { runEndingAt(completedDays, it) } ?: 0

/** Walks back from [end], counting completed days, forgiving a single gap. */
private fun runEndingAt(days: Set<Long>, end: Long): Int {
    if (end !in days) return 0
    var count = 0
    var graceUsed = false
    var day = end
    while (day >= 0L) {
        when {
            day in days -> {
                count++
                day--
            }
            !graceUsed && (day - 1) in days -> {
                graceUsed = true
                day--
            }
            else -> return count
        }
    }
    return count
}
