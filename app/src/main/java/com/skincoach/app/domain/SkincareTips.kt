package com.skincoach.app.domain

/**
 * A bank of short, dermatology-aligned skincare tips for the coach to share.
 * Sourced from mainstream guidance (AAD, Mayo Clinic, Harvard Health).
 */
object SkincareTips {

    val general: List<String> = listOf(
        "Cleanse twice a day with a gentle, fragrance-free cleanser and lukewarm water — never hot.",
        "Moisturize while your skin is still slightly damp — it locks in hydration far better.",
        "Wear broad-spectrum SPF 30+ every day, even when it's cloudy — UV passes through clouds and windows.",
        "Consistency beats intensity — a simple routine you actually keep up with wins every time.",
        "Don't over-exfoliate: 1-3 times a week is plenty. Stinging or redness means ease off.",
        "Be gentle — pat your skin dry and skip harsh scrubbing. Your skin barrier will thank you.",
        "Patch-test new products on your jawline for a few days before going all-in.",
    )

    private val byConcern: Map<Concern, List<String>> = mapOf(
        Concern.ACNE to listOf(
            "Look for benzoyl peroxide — even a gentle 2.5% formula fights acne bacteria well.",
            "Salicylic acid dives into pores and clears out what clogs them.",
            "Choose products labelled 'non-comedogenic' so they won't clog your pores.",
            "Give any acne treatment at least 4 weeks — real results take time, so don't give up early.",
            "Resist popping — it pushes bacteria deeper and can leave scars and dark marks.",
            "Wash pillowcases often and keep your phone screen clean — both touch your face daily.",
        ),
        Concern.REDNESS to listOf(
            "Choose 'fragrance-free' products — fragrance is a top irritant for reactive skin.",
            "Look for calming niacinamide, ceramides or centella to ease redness over time.",
            "Skip toners and alcohol-based products — they often sting and dry out sensitive skin.",
            "Cleanse gently with a mild, creamy cleanser and just your fingertips.",
            "Daily SPF is essential — sun is a common trigger for flushing and flare-ups.",
            "Notice your triggers — spicy food, alcohol, heat, stress — and ease off where you can.",
        ),
        Concern.OILINESS to listOf(
            "Still moisturize — skipping it makes skin pump out even more oil. Use a light, oil-free formula.",
            "Don't over-wash — stripping your skin signals it to make more oil. Twice a day is ideal.",
            "Niacinamide can help balance oil production over time.",
            "Keep blotting papers handy — press gently to lift shine without disturbing makeup.",
            "Choose 'non-comedogenic' and 'matte' products to control shine.",
            "Try not to touch your face — hands transfer oil and bacteria all day long.",
        ),
        Concern.PORES to listOf(
            "Pore size is mostly genetic — you can't shrink them for good, but you can make them far less visible.",
            "Keep pores clear — clogged pores stretch and start to look bigger.",
            "Chemical exfoliants like salicylic or glycolic acid smooth texture more gently than scrubs.",
            "Retinoids boost cell turnover and collagen, refining pores over time.",
            "Wear sunscreen daily — sun damage breaks down the collagen that keeps pores tight.",
        ),
        Concern.DARK_SPOTS to listOf(
            "Daily sunscreen is the #1 rule — sun deepens existing spots and creates new ones.",
            "Vitamin C in the morning helps brighten and even out your tone.",
            "Look for fading ingredients like niacinamide, azelaic acid or tranexamic acid.",
            "Be patient — visible fading usually takes 8-16 weeks of consistent use.",
            "Treat the cause — calming acne and not picking stops new dark marks forming.",
        ),
        Concern.FINE_LINES to listOf(
            "Daily sunscreen is the most powerful anti-aging step — UV causes most premature lines.",
            "A retinoid is the gold-standard ingredient for smoothing fine lines and boosting collagen.",
            "Start retinol slowly — once a week, low strength — and build up as your skin adjusts.",
            "Apply retinoids at night — they can make skin more sun-sensitive.",
            "Layer hyaluronic acid on damp skin to plump fine lines and lock in moisture.",
            "Don't skip moisturizer — hydrated skin looks plumper and softens the look of lines.",
        ),
    )

    /** Tips for a specific concern (falls back to general tips if none). */
    fun forConcern(concern: Concern): List<String> = byConcern[concern] ?: general
}
