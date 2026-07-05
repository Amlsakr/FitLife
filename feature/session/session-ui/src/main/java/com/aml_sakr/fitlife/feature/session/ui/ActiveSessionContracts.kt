package com.aml_sakr.fitlife.feature.session.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData

data class ActiveSessionState(
    val latestPoseData: PoseData? = null,
    val currentExerciseName: String? = null,
    val currentExerciseLottiePath: String? = null,
    val totalReps: Int = 0,
    val totalSets: Int = 0,
    val fatigueEventCount: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val planId: String = "",
    val workoutDayId: String = "",
    val userId: String = "",
    val alternatives: List<ExerciseAlternative> = emptyList(),
    val isEquipmentSheetLoading: Boolean = false,
    val isEquipmentSheetVisible: Boolean = false,
    val isCameraActive: Boolean = false,
    val isFatigued: Boolean = false,
    val isAudioOnlyMode: Boolean = false,
    val isManualLightingOverride: Boolean = false,
    val isFinishing: Boolean = false,
    val error: Throwable? = null
) : UIState

sealed interface ActiveSessionEvent : UIEvent {
    data class PoseDetected(val poseData: PoseData) : ActiveSessionEvent
    data class CameraStateChanged(val isActive: Boolean) : ActiveSessionEvent
    data class ErrorOccurred(val throwable: Throwable) : ActiveSessionEvent
    data class RepCompleted(val peakPose: PoseData) : ActiveSessionEvent
    data object FatigueDetected : ActiveSessionEvent
    data object DismissFatigue : ActiveSessionEvent
    data object ToggleAudioOnlyMode : ActiveSessionEvent
    data object OnEquipmentUnavailable : ActiveSessionEvent
    data class OnAlternativeSelected(val alternative: ExerciseAlternative) : ActiveSessionEvent
    data object DismissEquipmentSheet : ActiveSessionEvent
    data class Initialize(val userId: String, val planId: String, val workoutDayId: String) : ActiveSessionEvent
    data object FinishSession : ActiveSessionEvent
}

sealed interface ActiveSessionAction : OneTimeAction {
    data object ExitSession : ActiveSessionAction
    data class NavigateToSummary(val sessionId: String) : ActiveSessionAction
    data class Announce(val message: String) : ActiveSessionAction
}
