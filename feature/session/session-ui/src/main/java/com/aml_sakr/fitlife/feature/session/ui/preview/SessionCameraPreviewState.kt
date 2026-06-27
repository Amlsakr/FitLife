package com.aml_sakr.fitlife.feature.session.ui.preview

sealed interface SessionCameraPreviewState {
    data object Loading : SessionCameraPreviewState
    data object Active : SessionCameraPreviewState
    data class Error(val throwable: Throwable) : SessionCameraPreviewState
}
