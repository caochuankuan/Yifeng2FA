package com.compose.yifeng2fa.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onScanResult: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                val rawValue = barcodes[0].rawValue
                                if (rawValue != null) {
                                    onScanResult(rawValue)
                                } else {
                                    Toast.makeText(context, "未找到二维码", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "未找到二维码", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "扫描图片失败", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "处理图片出错", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = BarcodeScanning.getClient(options)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val executor = Executors.newSingleThreadExecutor()
                        var isScanned = false

                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            if (isScanned) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty() && !isScanned) {
                                            val rawValue = barcodes[0].rawValue
                                            if (rawValue != null) {
                                                isScanned = true
                                                imageAnalysis.clearAnalyzer()
                                                onScanResult(rawValue)
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

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scan Overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                                RoundedCornerShape(24.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "将二维码对准框内",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Bottom center buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Gallery button
                    FilledTonalIconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "从相册选择",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "需要相机权限",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "请授予相机权限以扫描二维码。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("授予权限")
                }
            }
        }
    }
}
