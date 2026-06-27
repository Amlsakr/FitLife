package com.aml_sakr.fitlife.feature.session.ui.preview

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

private const val TAG = "CameraPreview"

/**
 * A reusable CameraX preview component that binds a [Preview] use case to the current lifecycle.
 *
 * AC 2, 6, 8 compliance:
 * - Lifecycle-bound using [LocalLifecycleOwner].
 * - Non-blocking [ProcessCameraProvider] initialization.
 * - Targeted unbind of only the preview use case on disposal.
 * - Uses [PreviewView.ScaleType.FILL_CENTER] for distortion-free full-screen output.
 * - Supports [CameraPreviewProvider] override for testing.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onStateChanged: (SessionCameraPreviewState) -> Unit = {},
    providerOverride: CameraPreviewProvider? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    val preview = remember { Preview.Builder().build() }
    
    var provider by remember { mutableStateOf<CameraPreviewProvider?>(providerOverride) }

    // Initialize default provider if no override is given
    if (providerOverride == null && provider == null) {
        LaunchedEffect(context) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    provider = DefaultCameraPreviewProvider(cameraProviderFuture.get())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get ProcessCameraProvider", e)
                    onStateChanged(SessionCameraPreviewState.Error(e))
                }
            }, mainExecutor)
        }
    }

    // AC 6: Respect device rotation/scaling through PreviewView behavior.
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                preview.setSurfaceProvider(surfaceProvider)
            }
        },
        modifier = modifier,
        update = { _ ->
            // AC 8: Lifecycle-safe binding.
            provider?.bindPreview(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview
            ) { result ->
                result.fold(
                    onSuccess = { onStateChanged(SessionCameraPreviewState.Active) },
                    onFailure = { onStateChanged(SessionCameraPreviewState.Error(it)) }
                )
            }
        }
    )

    // AC 8: Teardown is lifecycle-safe.
    DisposableEffect(lifecycleOwner, provider) {
        onDispose {
            try {
                provider?.unbindPreview(preview)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding preview use case", e)
            }
        }
    }
}

private class DefaultCameraPreviewProvider(
    private val cameraProvider: ProcessCameraProvider
) : CameraPreviewProvider {
    override fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        preview: Preview,
        onResult: (Result<Unit>) -> Unit
    ) {
        try {
            if (!cameraProvider.isBound(preview)) {
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            }
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }

    override fun unbindPreview(preview: Preview) {
        cameraProvider.unbind(preview)
    }
}
