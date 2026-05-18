package com.skincoach.app.domain.analysis

import android.graphics.Bitmap
import com.skincoach.app.domain.AnalysisResult

/**
 * Analyzes a face photo and produces skin scores.
 * Implementations may run on-device or in the cloud — the rest of the app
 * depends only on this interface, so the engine can be swapped freely.
 */
interface SkinAnalyzer {
    suspend fun analyze(bitmap: Bitmap): AnalysisResult
}
