package com.skincoach.app.domain.routine

import com.skincoach.app.domain.Concern
import com.skincoach.app.domain.SkinAnalysis

/**
 * Builds a personalized AM/PM routine from a skin analysis.
 * Rule-based and deterministic: everyone gets the essentials, and concerns
 * that scored low add targeted, ingredient-specific steps.
 */
object RoutineGenerator {

    private const val LOW_SCORE = 65

    fun generate(analysis: SkinAnalysis): Routine {
        val weak = analysis.concerns
            .filter { it.score < LOW_SCORE }
            .map { it.concern }
            .toSet()

        val morning = buildList {
            add(RoutineStep("Gentle cleanser", "Lukewarm water, no harsh scrubbing"))
            if (Concern.REDNESS in weak) {
                add(RoutineStep("Soothing serum", "Niacinamide or centella to calm redness"))
            }
            if (Concern.DARK_SPOTS in weak) {
                add(RoutineStep("Vitamin C serum", "Brightens and evens out tone"))
            }
            add(RoutineStep("Lightweight moisturizer", "Locks in hydration for the day"))
            add(RoutineStep("Sunscreen SPF 30+", "Every morning — prevents spots and lines"))
        }

        val evening = buildList {
            add(RoutineStep("Cleanser", "Remove the day's oil, sweat and SPF"))
            if (Concern.ACNE in weak || Concern.OILINESS in weak) {
                add(RoutineStep("Salicylic acid (BHA)", "2-3x a week — clears pores and excess oil"))
            }
            if (Concern.PORES in weak) {
                add(RoutineStep("Exfoliating toner", "Refines texture and the look of pores"))
            }
            if (Concern.FINE_LINES in weak) {
                add(RoutineStep("Retinol", "Start 1-2x a week — smooths fine lines over time"))
            }
            add(RoutineStep("Night moisturizer", "Richer cream to repair overnight"))
        }

        return Routine(morning = morning, evening = evening)
    }
}
