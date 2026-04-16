package com.example.academia.ui.dashboard

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(onScanSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = Executors.newSingleThreadExecutor()
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val scanner = BarcodeScanning.getClient()
                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(scanner, imageProxy, onScanSuccess)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QRScanner", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Adobe Scan Style Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val rectSize = 280.dp.toPx()
                val rectLeft = (canvasWidth - rectSize) / 2
                val rectTop = (canvasHeight - rectSize) / 2
                
                // Darken the whole screen
                drawRect(Color.Black.copy(alpha = 0.6f))
                
                // Cut out the inner rectangle
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = androidx.compose.ui.geometry.Offset(rectLeft, rectTop),
                    size = androidx.compose.ui.geometry.Size(rectSize, rectSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                )

                // Draw neon borders
                val borderPath = androidx.compose.ui.graphics.Path()
                val cornerLength = 40.dp.toPx()
                val strokeW = 4.dp.toPx()
                val neonColor = Color(0xFF00E5FF) // Cyan / neon blue
                
                // Top-Left
                borderPath.moveTo(rectLeft, rectTop + cornerLength)
                borderPath.lineTo(rectLeft, rectTop + 24.dp.toPx()) // Before corner curve
                borderPath.arcTo(
                    rect = androidx.compose.ui.geometry.Rect(rectLeft, rectTop, rectLeft + 48.dp.toPx(), rectTop + 48.dp.toPx()),
                    startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                borderPath.lineTo(rectLeft + cornerLength, rectTop)
                
                // Top-Right
                borderPath.moveTo(rectLeft + rectSize - cornerLength, rectTop)
                borderPath.lineTo(rectLeft + rectSize - 24.dp.toPx(), rectTop)
                borderPath.arcTo(
                    rect = androidx.compose.ui.geometry.Rect(rectLeft + rectSize - 48.dp.toPx(), rectTop, rectLeft + rectSize, rectTop + 48.dp.toPx()),
                    startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                borderPath.lineTo(rectLeft + rectSize, rectTop + cornerLength)
                
                // Bottom-Left
                borderPath.moveTo(rectLeft, rectTop + rectSize - cornerLength)
                borderPath.lineTo(rectLeft, rectTop + rectSize - 24.dp.toPx())
                borderPath.arcTo(
                    rect = androidx.compose.ui.geometry.Rect(rectLeft, rectTop + rectSize - 48.dp.toPx(), rectLeft + 48.dp.toPx(), rectTop + rectSize),
                    startAngleDegrees = 180f, sweepAngleDegrees = -90f, forceMoveTo = false
                )
                borderPath.lineTo(rectLeft + cornerLength, rectTop + rectSize)
                
                // Bottom-Right
                borderPath.moveTo(rectLeft + rectSize - cornerLength, rectTop + rectSize)
                borderPath.lineTo(rectLeft + rectSize - 24.dp.toPx(), rectTop + rectSize)
                borderPath.arcTo(
                    rect = androidx.compose.ui.geometry.Rect(rectLeft + rectSize - 48.dp.toPx(), rectTop + rectSize - 48.dp.toPx(), rectLeft + rectSize, rectTop + rectSize),
                    startAngleDegrees = 90f, sweepAngleDegrees = -90f, forceMoveTo = false
                )
                borderPath.lineTo(rectLeft + rectSize, rectTop + rectSize - cornerLength)
                
                drawPath(
                    path = borderPath,
                    color = neonColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            
            // Text Header overlay
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan Certificate",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Align the QR code within the frame",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onScanSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { 
                        onScanSuccess(it) 
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}