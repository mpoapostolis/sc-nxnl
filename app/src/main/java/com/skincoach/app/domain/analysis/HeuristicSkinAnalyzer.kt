package com.skincoach.app.domain.analysis

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.skincoach.app.domain.AnalysisResult
import com.skincoach.app.domain.Concern
import com.skincoach.app.domain.ConcernScore
import com.skincoach.app.domain.SkinAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * On-device skin analyzer. Locates the face with ML Kit, turns away photos that
 * are too poor to score fairly, then samples skin patches (forehead, cheeks,
 * nose, chin) and derives scores from real measurements.
 *
 * Consistency — the same face scoring the same way twice — comes from:
 *  - a **quality gate**: a blurry, dark, over-lit or angled photo is sent back,
 *    so a score is only ever computed on comparable input;
 *  - **lighting-normalized** metrics, expressed relative to the skin's own
 *    brightness, with texture ignoring hair/shadow outliers;
 *  - **medians over generous patches**, so sampling noise averages away.
 *
 * It is a heuristic, not a trained dermatology model. Everything runs locally.
 */
class HeuristicSkinAnalyzer : SkinAnalyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build(),
    )

    override suspend fun analyze(bitmap: Bitmap): AnalysisResult {
        val face = try {
            detectFace(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            return AnalysisResult.Failed
        } ?: return AnalysisResult.NoFace

        // Quality gate — framing and head angle (cheap, straight from the face).
        val box = face.boundingBox
        if (box.width() < bitmap.width * 0.24f || box.width() < 130) {
            return AnalysisResult.LowQuality(
                "Hold the phone a little closer — your face is too small to read clearly.",
            )
        }
        if (abs(face.headEulerAngleY) > 16f ||
            abs(face.headEulerAngleZ) > 14f ||
            abs(face.headEulerAngleX) > 18f
        ) {
            return AnalysisResult.LowQuality(
                "Look straight at the camera and keep your head level, then scan again.",
            )
        }

        return withContext(Dispatchers.Default) {
            val sampled = samplePatches(bitmap, face)
                ?: return@withContext AnalysisResult.NoFace

            // Quality gate — lighting and focus (needs the sampled pixels).
            val brightness = sampled.patches.map { it.medianLum }.average()
            val sharpness = sampled.patches.map { it.sharpness }.average()
            when {
                brightness < 60.0 -> return@withContext AnalysisResult.LowQuality(
                    "It's a little dark — move to softer, brighter light and scan again.",
                )
                brightness > 232.0 -> return@withContext AnalysisResult.LowQuality(
                    "That's a bit too bright — ease away from the strong light and scan again.",
                )
                sharpness < 0.011 -> return@withContext AnalysisResult.LowQuality(
                    "That came out blurry — hold still, let the camera focus, and scan again.",
                )
            }

            AnalysisResult.Success(score(aggregate(sampled)), cropToFace(bitmap, face))
        }
    }

    private suspend fun detectFace(bitmap: Bitmap): Face? =
        suspendCancellableCoroutine { cont ->
            detector.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { faces -> cont.resume(faces.firstOrNull()) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private fun samplePatches(bitmap: Bitmap, face: Face): Sampled? {
        val box = face.boundingBox
        val w = box.width()
        val h = box.height()

        fun landmark(type: Int, fallbackX: Int, fallbackY: Int): Pair<Int, Int> {
            val p = face.getLandmark(type)?.position
            return if (p != null) p.x.toInt() to p.y.toInt() else fallbackX to fallbackY
        }

        // Generous patches — more pixels means less sampling noise.
        val side = (w * 0.19f).toInt().coerceAtLeast(14)
        // The forehead patch sits well below the hairline so it samples skin, not hair.
        val forehead = samplePatch(bitmap, box.centerX(), box.top + (h * 0.27f).toInt(), side)
        val leftCheek = landmark(FaceLandmark.LEFT_CHEEK, box.left + (w * 0.24f).toInt(), box.centerY())
            .let { samplePatch(bitmap, it.first, it.second, side) }
        val rightCheek = landmark(FaceLandmark.RIGHT_CHEEK, box.right - (w * 0.24f).toInt(), box.centerY())
            .let { samplePatch(bitmap, it.first, it.second, side) }
        val nose = landmark(FaceLandmark.NOSE_BASE, box.centerX(), box.centerY())
            .let { samplePatch(bitmap, it.first, it.second, side) }
        val chin = samplePatch(bitmap, box.centerX(), box.bottom - (h * 0.14f).toInt(), side)

        val patches = listOfNotNull(forehead, leftCheek, rightCheek, nose, chin)
        return if (patches.size < 3) null else Sampled(patches, forehead)
    }

    private fun samplePatch(bitmap: Bitmap, cx: Int, cy: Int, side: Int): PatchStats? {
        val half = side / 2
        val x = (cx - half).coerceIn(0, bitmap.width - 1)
        val y = (cy - half).coerceIn(0, bitmap.height - 1)
        val pw = side.coerceAtMost(bitmap.width - x)
        val ph = side.coerceAtMost(bitmap.height - y)
        if (pw < 8 || ph < 8) return null

        val pixels = IntArray(pw * ph)
        bitmap.getPixels(pixels, 0, pw, x, y, pw, ph)
        val n = pixels.size

        val lums = DoubleArray(n)
        val reds = DoubleArray(n)
        val sats = DoubleArray(n)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = 0.299 * r + 0.587 * g + 0.114 * b
            lums[i] = lum
            // Redness as a ratio to brightness — independent of exposure.
            reds[i] = (r - (g + b) / 2.0) / (lum + 1.0)
            val mx = maxOf(r, g, b)
            val mn = minOf(r, g, b)
            sats[i] = if (mx > 0) (mx - mn).toDouble() / mx else 0.0
        }

        val medianLum = median(lums)
        val medianRed = median(reds)

        // Texture — luminance spread, but only over skin-range pixels. Hair,
        // brows and hard shadows fall outside the band, so stubble and stray
        // hairs can't masquerade as pores or fine lines.
        val lo = medianLum * 0.72
        val hi = medianLum * 1.32
        var texVar = 0.0
        var texCount = 0
        for (l in lums) {
            if (l in lo..hi) {
                val d = l - medianLum
                texVar += d * d
                texCount++
            }
        }
        val texStd = if (texCount > 8) sqrt(texVar / texCount) else 0.0

        // Shine — bright, low-saturation pixels, relative to the patch's own skin.
        val shineThreshold = medianLum * 1.40
        var shine = 0
        for (i in 0 until n) {
            if (lums[i] > shineThreshold && sats[i] < 0.20) shine++
        }

        // Blemishes — pixels that are notably RED, not merely dark. Requiring
        // redness keeps neutral shadows and dark hair from counting as acne.
        var blemish = 0
        for (i in 0 until n) {
            if (reds[i] > medianRed + 0.13) blemish++
        }

        return PatchStats(
            medianLum = medianLum,
            medianRedness = medianRed,
            relativeTexture = texStd / (medianLum + 1.0),
            shineFraction = shine.toDouble() / n,
            blemishFraction = blemish.toDouble() / n,
            toneSpread = lowFrequencySpread(lums, pw, ph, medianLum),
            sharpness = highFrequencyEnergy(lums, pw, ph, medianLum),
        )
    }

    /**
     * Regional tone variation *within* a patch — the patch is split into a grid
     * and the spread of block brightnesses is measured. Staying inside one small
     * patch, it reflects mottling and spots rather than the room's light direction.
     */
    private fun lowFrequencySpread(lums: DoubleArray, pw: Int, ph: Int, median: Double): Double {
        val grid = 4
        if (pw < grid * 2 || ph < grid * 2) return 0.0
        val bw = pw / grid
        val bh = ph / grid
        val blockMeans = DoubleArray(grid * grid)
        for (by in 0 until grid) {
            for (bx in 0 until grid) {
                var sum = 0.0
                var count = 0
                for (yy in by * bh until (by + 1) * bh) {
                    for (xx in bx * bw until (bx + 1) * bw) {
                        sum += lums[yy * pw + xx]
                        count++
                    }
                }
                blockMeans[by * grid + bx] = if (count > 0) sum / count else median
            }
        }
        val mean = blockMeans.average()
        val variance = blockMeans.sumOf { (it - mean) * (it - mean) } / blockMeans.size
        return sqrt(variance) / (median + 1.0)
    }

    /**
     * High-frequency energy — the average brightness step between neighbouring
     * pixels. An in-focus photo carries real micro-detail; a blurry one is smooth.
     */
    private fun highFrequencyEnergy(lums: DoubleArray, pw: Int, ph: Int, median: Double): Double {
        var sum = 0.0
        var count = 0
        for (yy in 0 until ph) {
            for (xx in 0 until pw - 1) {
                sum += abs(lums[yy * pw + xx] - lums[yy * pw + xx + 1])
                count++
            }
        }
        for (yy in 0 until ph - 1) {
            for (xx in 0 until pw) {
                sum += abs(lums[yy * pw + xx] - lums[(yy + 1) * pw + xx])
                count++
            }
        }
        return if (count > 0) (sum / count) / (median + 1.0) else 0.0
    }

    /** Crops the photo down to the face (with padding) for display and history. */
    private fun cropToFace(bitmap: Bitmap, face: Face): Bitmap {
        val box = face.boundingBox
        val padX = (box.width() * 0.22f).toInt()
        val padY = (box.height() * 0.30f).toInt()
        val left = (box.left - padX).coerceAtLeast(0)
        val top = (box.top - padY).coerceAtLeast(0)
        val right = (box.right + padX).coerceAtMost(bitmap.width)
        val bottom = (box.bottom + padY).coerceAtMost(bitmap.height)
        val w = (right - left).coerceAtLeast(1)
        val h = (bottom - top).coerceAtLeast(1)
        return runCatching {
            Bitmap.createBitmap(bitmap, left, top, w, h)
        }.getOrDefault(bitmap)
    }

    private fun aggregate(sampled: Sampled): Metrics {
        val patches = sampled.patches
        return Metrics(
            redness = patches.map { it.medianRedness }.average(),
            shine = patches.map { it.shineFraction }.average(),
            texture = patches.map { it.relativeTexture }.average(),
            toneUnevenness = patches.map { it.toneSpread }.average(),
            blemish = patches.map { it.blemishFraction }.average(),
            foreheadTexture = sampled.forehead?.relativeTexture
                ?: patches.map { it.relativeTexture }.average(),
        )
    }

    private fun score(m: Metrics): SkinAnalysis {
        val concerns = listOf(
            ConcernScore(Concern.REDNESS, mapScore(m.redness, best = 0.07, worst = 0.24)),
            ConcernScore(Concern.OILINESS, mapScore(m.shine, best = 0.0, worst = 0.16)),
            ConcernScore(Concern.PORES, mapScore(m.texture, best = 0.035, worst = 0.14)),
            ConcernScore(Concern.DARK_SPOTS, mapScore(m.toneUnevenness, best = 0.015, worst = 0.10)),
            ConcernScore(Concern.ACNE, mapScore(m.blemish, best = 0.0, worst = 0.10)),
            ConcernScore(Concern.FINE_LINES, mapScore(m.foreheadTexture, best = 0.04, worst = 0.15)),
        )
        val weights = mapOf(
            Concern.REDNESS to 1.0, Concern.OILINESS to 1.0,
            Concern.PORES to 1.0, Concern.DARK_SPOTS to 1.0,
            Concern.ACNE to 0.8, Concern.FINE_LINES to 0.6,
        )
        val weightedSum = concerns.sumOf { it.score * (weights[it.concern] ?: 1.0) }
        val totalWeight = concerns.sumOf { weights[it.concern] ?: 1.0 }
        val overall = (weightedSum / totalWeight).roundToInt().coerceIn(0, 100)
        return SkinAnalysis(overallScore = overall, concerns = concerns)
    }

    /** Maps a normalized measurement to a 55-96 score — a tight, encouraging band. */
    private fun mapScore(value: Double, best: Double, worst: Double): Int {
        val t = ((value - best) / (worst - best)).coerceIn(0.0, 1.0)
        return (96 - t * 41).roundToInt()
    }

    private fun median(values: DoubleArray): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.copyOf()
        sorted.sort()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid]
        }
    }

    private class Sampled(
        val patches: List<PatchStats>,
        val forehead: PatchStats?,
    )

    private data class PatchStats(
        val medianLum: Double,
        val medianRedness: Double,
        val relativeTexture: Double,
        val shineFraction: Double,
        val blemishFraction: Double,
        val toneSpread: Double,
        val sharpness: Double,
    )

    private data class Metrics(
        val redness: Double,
        val shine: Double,
        val texture: Double,
        val toneUnevenness: Double,
        val blemish: Double,
        val foreheadTexture: Double,
    )

    private companion object {
        const val TAG = "HeuristicSkinAnalyzer"
    }
}
