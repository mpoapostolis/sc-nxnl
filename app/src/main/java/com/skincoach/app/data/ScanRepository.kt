package com.skincoach.app.data

import android.content.Context
import com.skincoach.app.data.db.ScanEntity
import com.skincoach.app.data.db.SkinCoachDatabase
import com.skincoach.app.domain.Concern
import com.skincoach.app.domain.SkinAnalysis
import kotlinx.coroutines.flow.Flow

/** Saves and retrieves skin scans from the local database. */
class ScanRepository(context: Context) {

    private val dao = SkinCoachDatabase.get(context).scanDao()

    /** All scans, newest first. */
    fun observeScans(): Flow<List<ScanEntity>> = dao.observeAll()

    /** The most recent scan, or null if there are none yet. */
    suspend fun latest(): ScanEntity? = dao.latest()

    suspend fun delete(scan: ScanEntity) = dao.deleteById(scan.id)

    suspend fun save(photoPath: String, analysis: SkinAnalysis): Long {
        fun scoreOf(concern: Concern): Int =
            analysis.concerns.first { it.concern == concern }.score

        return dao.insert(
            ScanEntity(
                timestamp = System.currentTimeMillis(),
                photoPath = photoPath,
                overallScore = analysis.overallScore,
                acne = scoreOf(Concern.ACNE),
                fineLines = scoreOf(Concern.FINE_LINES),
                pores = scoreOf(Concern.PORES),
                darkSpots = scoreOf(Concern.DARK_SPOTS),
                redness = scoreOf(Concern.REDNESS),
                oiliness = scoreOf(Concern.OILINESS),
            ),
        )
    }
}
