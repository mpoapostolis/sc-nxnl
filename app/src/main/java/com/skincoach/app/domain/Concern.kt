package com.skincoach.app.domain

/** A skin concern that the analyzer scores. */
enum class Concern(val label: String) {
    ACNE("Acne"),
    FINE_LINES("Fine lines"),
    PORES("Pores"),
    DARK_SPOTS("Dark spots"),
    REDNESS("Redness"),
    OILINESS("Oiliness"),
}
