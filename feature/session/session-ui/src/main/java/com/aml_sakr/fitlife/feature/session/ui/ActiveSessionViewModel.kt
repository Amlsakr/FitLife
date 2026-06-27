package com.aml_sakr.fitlife.feature.session.ui

import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel

/**
 * ViewModel for the active workout session.
 * Handles pose detection events and updates the UI state.
 */
class ActiveSessionViewModel : BaseMviViewModel<ActiveSessionState, ActiveSessionEvent, ActiveSessionAction>(
    ActiveSessionState()
) {
    override fun handleEvent(event: ActiveSessionEvent) {
        when (event) {
            is ActiveSessionEvent.PoseDetected -> {
                setState { copy(latestPoseData = event.poseData) }
            }
            is ActiveSessionEvent.CameraStateChanged -> {
                setState { copy(isCameraActive = event.isActive) }
            }
            is ActiveSessionEvent.ErrorOccurred -> {
                setState { copy(error = event.throwable) }
            }
        }
    }
}
