package com.skincoach.app.ui.screens.capture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.skincoach.app.ui.theme.Ink
import com.skincoach.app.ui.theme.Terracotta
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

@Composable
fun CaptureScreen(onClose: () -> Unit, onCaptured: (String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        if (hasPermission) {
            CameraContent(onClose = onClose, onCaptured = onCaptured)
        } else {
            PermissionPrompt(
                onAllow = { launcher.launch(Manifest.permission.CAMERA) },
                onSettings = { context.openAppSettings() },
                onClose = onClose,
            )
        }
    }
}

@Composable
private fun CameraContent(onClose: () -> Unit, onCaptured: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    var framing by remember { mutableStateOf(FramingState.SEARCHING) }
    var bound by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            runCatching {
                val provider = future.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(
                    analysisExecutor,
                    FaceFramingAnalyzer { state -> if (!captured) framing = state },
                )
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    analysis,
                )
            }.onSuccess { bound = true }
                .onFailure { Log.e("CaptureScreen", "Camera bind failed", it) }
        }, ContextCompat.getMainExecutor(context))
    }

    // Auto-capture once the face has been steadily well-framed.
    LaunchedEffect(framing, bound) {
        if (bound && !captured && framing == FramingState.READY) {
            delay(1300)
            if (!captured && framing == FramingState.READY) {
                captured = true
                capturePhoto(
                    imageCapture = imageCapture,
                    context = context,
                    onSaved = { onCaptured(it) },
                    onError = { captured = false },
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        FramingOverlay(ready = framing.ready)

        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✕",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start).clickable(onClick = onClose),
            )
            Spacer(Modifier.weight(1f))
            GuidancePill(framing)
            Spacer(Modifier.height(22.dp))
            ShutterButton(
                enabled = bound && !captured,
                onClick = {
                    if (!captured) {
                        captured = true
                        capturePhoto(
                            imageCapture = imageCapture,
                            context = context,
                            onSaved = { onCaptured(it) },
                            onError = { captured = false },
                        )
                    }
                },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Auto-captures when you're framed — or tap the button",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FramingOverlay(ready: Boolean) {
    val ovalColor by animateColorAsState(
        targetValue = if (ready) Terracotta else Color.White.copy(alpha = 0.92f),
        label = "ovalColor",
    )
    Canvas(Modifier.fillMaxSize()) {
        val ovalWidth = size.width * 0.66f
        val ovalHeight = ovalWidth * 1.34f
        val left = (size.width - ovalWidth) / 2f
        val top = size.height * 0.17f
        val ovalRect = Rect(left, top, left + ovalWidth, top + ovalHeight)

        val scrim = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addOval(ovalRect)
            fillType = PathFillType.EvenOdd
        }
        drawPath(scrim, color = Color.Black.copy(alpha = 0.46f))
        drawOval(
            color = ovalColor,
            topLeft = Offset(left, top),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

@Composable
private fun GuidancePill(state: FramingState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (state.ready) Terracotta else Color.Black.copy(alpha = 0.58f))
            .padding(horizontal = 22.dp, vertical = 13.dp),
    ) {
        Text(
            text = state.message,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .size(78.dp)
            .border(3.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(if (enabled) Color.White else Color.White.copy(alpha = 0.45f))
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    )
}

@Composable
private fun PermissionPrompt(
    onAllow: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
    ) {
        Text(
            text = "✕",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.clickable(onClick = onClose),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "Camera access",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Skin Coach needs your camera to take the selfie it analyzes. " +
                "Photos stay on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Terracotta, CircleShape)
                .clickable(onClick = onAllow),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Allow camera",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Open settings",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onSettings),
        )
        Spacer(Modifier.weight(1f))
    }
}

private fun capturePhoto(
    imageCapture: ImageCapture,
    context: Context,
    onSaved: (String) -> Unit,
    onError: () -> Unit,
) {
    val file = File(context.filesDir, "scan_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                onSaved(file.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CaptureScreen", "Capture failed", exception)
                onError()
            }
        },
    )
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
