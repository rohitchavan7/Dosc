package com.r.dosc.presentation.scanning.components

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.r.dosc.presentation.scanning.ScanningViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

@Composable
fun CameraView(
    modifier: Modifier,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    scanningViewModel: ScanningViewModel
) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    LaunchedEffect(lensFacing) {
        val cameraProvider = scanningViewModel.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)

        scanningViewModel.captureImage.collectLatest { click ->
            when (click) {
                true -> {
                    takePhoto(
                        imageCapture = imageCapture,
                        outputDir = scanningViewModel.getTempOutputDirectory(),
                        executorService = scanningViewModel.getCameraExecutor(),
                        onImageCaptured = { uri ->
                            onImageCaptured(uri)
                        },
                        onError = onError

                    )
                    scanningViewModel.clickImage(false)

                }
                else -> Unit
            }
        }

    }

    AndroidView(
        {
            previewView
        },
        modifier = modifier
    )
}

private fun takePhoto(
    imageCapture: ImageCapture,
    outputDir: File,
    executorService: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    val photoOutputTempFile = File(
        outputDir,
        SimpleDateFormat(
            "yyy-MM-dd-HH-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoOutputTempFile).build()

    imageCapture.takePicture(
        outputOptions,
        executorService,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val saveUri: Uri = Uri.fromFile(photoOutputTempFile)
                onImageCaptured(saveUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })

}


