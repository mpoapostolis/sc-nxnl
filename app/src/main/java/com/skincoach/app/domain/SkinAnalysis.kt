package com.skincoach.app.domain

import android.graphics.Bitmap

/** One concern's score, 0-100 (higher = healthier skin on that axis). */
data class ConcernScore(val concern: Concern, val score: Int)

/** The full result of analyzing a face photo. */
data class SkinAnalysis(
    val overallScore: Int,
    val concerns: List<ConcernScore>,
) {
    /** A short human grade for the overall score. */
    val grade: String
        get() = when {
            overallScore >= 85 -> "Excellent"
            overallScore >= 72 -> "Great"
            overallScore >= 58 -> "Good"
            overallScore >= 45 -> "Fair"
            else -> "Needs care"
        }
}

/** Outcome of an analysis attempt. */
sealed interface AnalysisResult {
    data class Success(val analysis: SkinAnalysis, val faceCrop: Bitmap) : AnalysisResult
    data object NoFace : AnalysisResult
    data object Failed : AnalysisResult
}
