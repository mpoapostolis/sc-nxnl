package com.skincoach.app.domain.routine

/** A single step in a skincare routine. */
data class RoutineStep(
    val title: String,
    val detail: String,
)

/** A personalized AM/PM skincare routine. */
data class Routine(
    val morning: List<RoutineStep>,
    val evening: List<RoutineStep>,
)
