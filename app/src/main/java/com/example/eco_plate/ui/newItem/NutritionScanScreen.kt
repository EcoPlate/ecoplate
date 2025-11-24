package com.example.eco_plate.ui.newItem

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NutritionScanScreen(
    onTextFound: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Set up and request camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // This state will hold the full text found by the recognizer
    var detectedText by remember { mutableStateOf("") }
    // This state will hold the bounding boxes of the detected text
    var textRects by remember { mutableStateOf(emptyList<Rect>()) }


    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    startTextRecognition(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onTextUpdated = { fullText, rects ->
                            detectedText = fullText
                            textRects = rects
                        }
                    )
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Canvas to draw bounding boxes over the camera preview
            Canvas(modifier = Modifier.fillMaxSize()) {
                textRects.forEach { rect ->
                    drawRect(
                        color = Color.Red,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        } else {
            // Show a message if permission is denied
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required to scan nutrition facts.")
            }
        }

        // UI controls at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A simple display of the currently detected text (optional, but good for debugging)
            if (detectedText.isNotBlank()) {
                Text(
                    text = "Detected Text: ${detectedText.take(100)}...", // Show a preview
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom=100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onBack) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onTextFound(detectedText) },
                    enabled = detectedText.isNotBlank() // Only enable if text has been found
                ) {
                    Text("Use This Text")
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun startTextRecognition(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onTextUpdated: (String, List<Rect>) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val fullText = visionText.text
                                // Transform ML Kit bounding boxes to Jetpack Compose Rects
                                val rects = visionText.textBlocks.mapNotNull { block ->
                                    block.boundingBox?.let { box ->
                                        // You might need to add scaling logic here if the preview and analysis resolutions differ
                                        Rect(
                                            left = box.left.toFloat(),
                                            top = box.top.toFloat(),
                                            right = box.right.toFloat(),
                                            bottom = box.bottom.toFloat()
                                        )
                                    }
                                }
                                onTextUpdated(fullText, rects)
                            }
                            .addOnFailureListener { e ->
                                Log.e("NutritionScanner", "Text recognition failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close() // IMPORTANT: Always close the ImageProxy
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e("NutritionScanner", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

