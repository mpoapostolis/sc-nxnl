package com.skincoach.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.skincoach.app.R

/** Fraunces — an editorial serif for display moments (the app name, the Skin Score). */
val Fraunces = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
)

/** Hanken Grotesk — a refined grotesk for body and UI. */
val Hanken = FontFamily(
    Font(R.font.hanken_grotesk_regular, FontWeight.Normal),
    Font(R.font.hanken_grotesk_medium, FontWeight.Medium),
    Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.hanken_grotesk_bold, FontWeight.Bold),
)

val SkinCoachTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 52.sp, lineHeight = 54.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Normal,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp,
    ),
)
