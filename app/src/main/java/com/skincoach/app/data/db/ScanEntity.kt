package com.skincoach.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One saved scan. Concern scores are stored as columns for simple querying. */
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val photoPath: String,
    val overallScore: Int,
    val acne: Int,
    val fineLines: Int,
    val pores: Int,
    val darkSpots: Int,
    val redness: Int,
    val oiliness: Int,
)
