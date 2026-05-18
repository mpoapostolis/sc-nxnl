package com.skincoach.app.ui.screens.capture

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs

/** Live framing feedback for the capture screen. */
enum class FramingState(val message: String, val ready: Boolean = false) {
    SEARCHING("Center your face in the oval"),
    MULTIPLE_FACES("Just one face, please"),
    TOO_DARK("Find brighter, even light"),
    TOO_FAR("Move a little closer"),
    TOO_CLOSE("Move back a little"),
    OFF_CENTER("Center your face in the oval"),
    TILTED("Hold your head straight"),
    READY("Perfect — hold still…", ready = true),
}

/**
 * Runs ML Kit face detection on each camera frame and reports how well the
 * face is framed, so the capture screen can guide the user and auto-capture.
 */
class FaceFramingAnalyzer(
    private val onState: (FramingState) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build(),
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val rotated = rotation == 90 || rotation == 270
        val uprightWidth = if (rotated) imageProxy.height else imageProxy.width
        val uprightHeight = if (rotated) imageProxy.width else imageProxy.height
        val brightness = averageLuminance(imageProxy)

        detector.process(InputImage.fromMediaImage(mediaImage, rotation))
            .addOnSuccessListener { faces ->
                onState(evaluate(faces, uprightWidth, uprightHeight, brightness))
            }
            .addOnFailureListener { onState(FramingState.SEARCHING) }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun evaluate(
        faces: List<Face>,
        uprightWidth: Int,
        uprightHeight: Int,
        brightness: Int,
    ): FramingState {
        if (brightness in 1..54) return FramingState.TOO_DARK
        if (faces.isEmpty()) return FramingState.SEARCHING
        if (faces.size > 1) return FramingState.MULTIPLE_FACES

        val box = faces[0].boundingBox
        val widthFraction = box.width().toFloat() / uprightWidth
        val centerX = box.exactCenterX() / uprightWidth
        val centerY = box.exactCenterY() / uprightHeight
        val yaw = abs(faces[0].headEulerAngleY)
        val roll = abs(faces[0].headEulerAngleZ)

        return when {
            widthFraction < 0.34f -> FramingState.TOO_FAR
            widthFraction > 0.80f -> FramingState.TOO_CLOSE
            centerX < 0.32f || centerX > 0.68f -> FramingState.OFF_CENTER
            centerY < 0.24f || centerY > 0.66f -> FramingState.OFF_CENTER
            yaw > 14f || roll > 14f -> FramingState.TILTED
            else -> FramingState.READY
        }
    }

    /** Rough average luminance from the Y plane — used only to flag dark scenes. */
    private fun averageLuminance(imageProxy: ImageProxy): Int {
        val buffer = imageProxy.planes[0].buffer
        val size = buffer.remaining()
        if (size == 0) return 0
        val step = (size / 2048).coerceAtLeast(1)
        var sum = 0L
        var count = 0
        var i = 0
        while (i < size) {
            sum += buffer.get(i).toInt() and 0xFF
            count++
            i += step
        }
        return if (count > 0) (sum / count).toInt() else 0
    }
}
