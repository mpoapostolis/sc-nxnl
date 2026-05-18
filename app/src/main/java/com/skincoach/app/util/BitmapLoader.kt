package com.skincoach.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Loads a photo from disk: downsampled to keep memory sane and rotated
 * upright according to its EXIF orientation.
 */
fun loadOrientedBitmap(path: String, maxSize: Int = 1024): Bitmap? {
    val file = File(path)
    if (!file.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    val largest = max(bounds.outWidth, bounds.outHeight)
    while (largest / sample > maxSize) sample *= 2

    val decoded = BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply { inSampleSize = sample },
    ) ?: return null

    val rotation = runCatching {
        when (
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }.getOrDefault(0f)

    if (rotation == 0f) return decoded
    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
}

/** Saves a bitmap as a JPEG in app storage; returns its file path, or null on failure. */
fun saveBitmapToFile(context: Context, bitmap: Bitmap, name: String): String? = runCatching {
    val file = File(context.filesDir, name)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    file.absolutePath
}.getOrNull()
