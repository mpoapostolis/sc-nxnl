package com.skincoach.app.ui.navigation

import android.net.Uri

/** Navigation routes for Skin Coach. */
object Destinations {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val HISTORY = "history"
    const val TODAY = "today"

    // Result carries the captured photo's file path.
    const val RESULT_ARG_PHOTO = "photo"
    const val RESULT_ROUTE = "result/{$RESULT_ARG_PHOTO}"

    fun result(photoPath: String): String = "result/${Uri.encode(photoPath)}"
}
