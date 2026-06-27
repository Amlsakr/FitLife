package com.aml_sakr.fitlife.feature.session.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData

data class ActiveSessionState(
    val latestPoseData: PoseData? = null,
    val isCameraActive: Boolean = false,
    val isFatigued: Boolean = false,
    val error: Throwable? = null
) : UIState

sealed interface ActiveSessionEvent : UIEvent {
    data class PoseDetected(val poseData: PoseData) : ActiveSessionEvent
    data class CameraStateChanged(val isActive: Boolean) : ActiveSessionEvent
    data class ErrorOccurred(val throwable: Throwable) : ActiveSessionEvent
    data class RepCompleted(val peakPose: PoseData) : ActiveSessionEvent
    data object FatigueDetected : ActiveSessionEvent
    data object DismissFatigue : ActiveSessionEvent
}

sealed interface ActiveSessionAction : OneTimeAction {
    data object ExitSession : ActiveSessionAction
}
