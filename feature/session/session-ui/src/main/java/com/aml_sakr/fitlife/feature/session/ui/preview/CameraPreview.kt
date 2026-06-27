package com.aml_sakr.fitlife.feature.session.ui.preview

import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraPreview"

/**
 * A reusable CameraX preview and analysis component.
 *
 * AC 2, 3, 6, 8 compliance:
 * - Lifecycle-bound using [LocalLifecycleOwner].
 * - Non-blocking [ProcessCameraProvider] initialization.
 * - Supports [ImageAnalysis] for real-time pose detection.
 * - Uses [PreviewView.ScaleType.FILL_CENTER] for distortion-free output.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onStateChanged: (SessionCameraPreviewState) -> Unit = {},
    providerOverride: CameraPreviewProvider? = null,
    analyzer: ImageAnalysis.Analyzer? = null,
    retryKey: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    // Patch: Manage executor lifecycle strictly within the component.
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val preview = remember { Preview.Builder().build() }
    val imageAnalysis = remember(analyzer) {
        analyzer?.let {
            ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480)) // AC 2 (from spike)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(analyzerExecutor, it)
                }
        }
    }
    
    var provider by remember { mutableStateOf<CameraPreviewProvider?>(providerOverride) }

    LaunchedEffect(context, providerOverride, retryKey) {
        if (providerOverride != null) {
            provider = providerOverride
            return@LaunchedEffect
        }
        
        onStateChanged(SessionCameraPreviewState.Loading)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                if (cameraProviderFuture.isDone && !cameraProviderFuture.isCancelled) {
                    provider = DefaultCameraPreviewProvider(cameraProviderFuture.get())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get ProcessCameraProvider", e)
                onStateChanged(SessionCameraPreviewState.Error(e))
            }
        }, mainExecutor)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                preview.setSurfaceProvider(surfaceProvider)
            }
        },
        modifier = modifier,
        update = { _ ->
            provider?.bindPreviewAndAnalysis(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            ) { result ->
                result.fold(
                    onSuccess = { onStateChanged(SessionCameraPreviewState.Active) },
                    onFailure = { onStateChanged(SessionCameraPreviewState.Error(it)) }
                )
            }
        }
    )

    DisposableEffect(lifecycleOwner, provider, imageAnalysis) {
        onDispose {
            try {
                provider?.unbindAll(preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding use cases", e)
            } finally {
                analyzerExecutor.shutdownNow() // Use shutdownNow to interrupt pending analysis
            }
        }
    }
}

private class DefaultCameraPreviewProvider(
    private val cameraProvider: ProcessCameraProvider
) : CameraPreviewProvider {
    override fun bindPreviewAndAnalysis(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        preview: Preview,
        imageAnalysis: ImageAnalysis?,
        onResult: (Result<Unit>) -> Unit
    ) {
        try {
            val useCases = mutableListOf<UseCase>(preview)
            imageAnalysis?.let { useCases.add(it) }

            // Patch: Avoid unbindAll() which can break other features. 
            // Unbind only the use cases we are about to bind.
            if (cameraProvider.isBound(preview)) {
                cameraProvider.unbind(preview)
            }
            imageAnalysis?.let {
                if (cameraProvider.isBound(it)) {
                    cameraProvider.unbind(it)
                }
            }
            
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *useCases.toTypedArray()
            )
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }

    override fun unbindAll(preview: Preview, imageAnalysis: ImageAnalysis?) {
        cameraProvider.unbind(preview)
        imageAnalysis?.let { cameraProvider.unbind(it) }
    }
}
