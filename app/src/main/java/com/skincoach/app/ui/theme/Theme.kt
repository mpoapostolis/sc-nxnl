package com.skincoach.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SkinCoachColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaSoft,
    onPrimaryContainer = TerracottaDeep,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    background = Paper,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = Line,
    error = ErrorRed,
    onError = Color.White,
)

/** Skin Coach theme — a warm, editorial light theme. */
@Composable
fun SkinCoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SkinCoachColorScheme,
        typography = SkinCoachTypography,
        shapes = SkinCoachShapes,
        content = content,
    )
}
