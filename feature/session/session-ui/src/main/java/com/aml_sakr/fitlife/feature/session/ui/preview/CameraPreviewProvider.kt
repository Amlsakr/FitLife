package com.aml_sakr.fitlife.feature.session.ui.preview

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner

/**
 * A provider interface to abstract CameraX binding for testing.
 */
interface CameraPreviewProvider {
    fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        preview: Preview,
        onResult: (Result<Unit>) -> Unit
    )

    fun unbindPreview(preview: Preview)
}
