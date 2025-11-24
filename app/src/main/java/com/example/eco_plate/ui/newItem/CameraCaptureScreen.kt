package com.example.eco_plate.ui.newItem

import android.Manifest // <-- Add this import
import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi // <-- Add this import
import com.google.accompanist.permissions.isGranted // <-- Add this import
import com.google.accompanist.permissions.rememberPermissionState // <-- Add this import
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class) // <-- Add this annotation
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraCaptureScreen(
    onPictureTaken: (File) -> Unit,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // Camera Permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    if (cameraPermissionState.status.isGranted) {
        // --- CAMERA UI
        CameraPreview(
            lifecycleOwner = lifecycleOwner,
            onPictureTaken = onPictureTaken,
            onBack = onBack
        )
    } else {
        // --- PERMISSION DENIED UI ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to use this feature.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
    }
}

// I've extracted the camera logic into a private composable for clarity
@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onPictureTaken: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            previewView.setBackgroundColor(android.graphics.Color.BLACK)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder().build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                //IFFY PADDING GUESS
                .padding(bottom=100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onBack) { Text("Cancel") }

                Button(onClick = {
                    val file = createImageFile(context)
                    imageCapture?.takePicture(
                        ImageCapture.OutputFileOptions.Builder(file).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraCapture", "Photo capture failed: ${exc.message}")
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                Log.d("CameraCapture", "Photo saved: ${file.absolutePath}")
                                onPictureTaken(file)
                            }
                        }
                    )
                }) {
                    Text("Take Photo")
                }
            }
        }
    }
}


private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File(storageDir, "ITEM_$timestamp.jpg")
}
