package com.aml_sakr.fitlife.feature.session.ui

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.session.domain.pose.AnalyzePoseUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectFatigueUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectRepUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.FatigueStatus
import com.aml_sakr.fitlife.feature.session.domain.pose.LightingStatus
import com.aml_sakr.fitlife.feature.session.domain.pose.LightingUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData
import com.aml_sakr.fitlife.feature.session.domain.equipment.RerouteEquipmentUseCase
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.aml_sakr.fitlife.core.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the active workout session.
 * Handles pose detection events and updates the UI state.
 * AC 4, 7, 8 compliance:
 * - Handles fatigue detection via [DetectFatigueUseCase].
 * - Implements dismissal cooldown (5 reps).
 * - Logs analytics events via [AnalyticsLogger].
 * - Handles lighting fallback via [LightingUseCase].
 * - Handles equipment rerouting via [RerouteEquipmentUseCase].
 */
@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    val analyzePoseUseCase: AnalyzePoseUseCase,
    private val detectFatigueUseCase: DetectFatigueUseCase,
    private val detectRepUseCase: DetectRepUseCase,
    private val lightingUseCase: LightingUseCase,
    private val analyticsLogger: AnalyticsLogger,
    private val rerouteEquipmentUseCase: RerouteEquipmentUseCase
) : BaseMviViewModel<ActiveSessionState, ActiveSessionEvent, ActiveSessionAction>(
    ActiveSessionState()
) {
    private var repsSinceDismissal = -1
    private var totalReps = 0
    private val poseDataFlow = MutableSharedFlow<PoseData>(extraBufferCapacity = 1)

    init {
        setState { copy(currentExerciseName = "Barbell Squat") }
        viewModelScope.launch {
            lightingUseCase(poseDataFlow).collect { status ->
                if (!state.value.isManualLightingOverride) {
                    val isAudioOnly = status is LightingStatus.AudioOnly
                    if (state.value.isAudioOnlyMode != isAudioOnly) {
                        setState { copy(isAudioOnlyMode = isAudioOnly) }
                        analyticsLogger.logEvent(
                            if (isAudioOnly) "lighting_fallback_triggered" else "lighting_fallback_reverted"
                        )
                        sendAction(
                            ActiveSessionAction.Announce(
                                if (isAudioOnly) "Low light detected. Switching to audio mode." 
                                else "Lighting improved. Returning to visual mode."
                            )
                        )
                    }
                }
            }
        }
    }

    override fun handleEvent(event: ActiveSessionEvent) {
        when (event) {
            is ActiveSessionEvent.PoseDetected -> {
                setState { copy(latestPoseData = event.poseData) }
                poseDataFlow.tryEmit(event.poseData)
                detectRepUseCase.processPose(event.poseData)?.let { peakPose ->
                    onEvent(ActiveSessionEvent.RepCompleted(peakPose))
                }
            }
            is ActiveSessionEvent.RepCompleted -> {
                handleRepCompleted(event)
            }
            is ActiveSessionEvent.CameraStateChanged -> {
                setState { copy(isCameraActive = event.isActive) }
            }
            is ActiveSessionEvent.ErrorOccurred -> {
                setState { copy(error = event.throwable) }
            }
            ActiveSessionEvent.FatigueDetected -> {
                if (!state.value.isFatigued) {
                    setState { copy(isFatigued = true) }
                    analyticsLogger.logEvent("fatigue_detected", mapOf("rep_number" to totalReps))
                }
            }
            ActiveSessionEvent.DismissFatigue -> {
                setState { copy(isFatigued = false) }
                repsSinceDismissal = 0
                analyticsLogger.logEvent("fatigue_dismissed")
            }
            ActiveSessionEvent.ToggleAudioOnlyMode -> {
                if (state.value.isManualLightingOverride) {
                    // Reset to Auto mode
                    setState { copy(isManualLightingOverride = false) }
                    analyticsLogger.logEvent("manual_audio_only_reset_to_auto")
                    sendAction(ActiveSessionAction.Announce("Automatic lighting detection resumed."))
                } else {
                    // Enable Manual mode and toggle
                    val nextMode = !state.value.isAudioOnlyMode
                    setState { 
                        copy(
                            isAudioOnlyMode = nextMode,
                            isManualLightingOverride = true 
                        )
                    }
                    analyticsLogger.logEvent("manual_audio_only_toggle", mapOf("enabled" to nextMode))
                    sendAction(
                        ActiveSessionAction.Announce(
                            if (nextMode) "Manual audio mode enabled." else "Manual visual mode enabled."
                        )
                    )
                }
            }
            ActiveSessionEvent.OnEquipmentUnavailable -> handleEquipmentUnavailable()
            is ActiveSessionEvent.OnAlternativeSelected -> handleAlternativeSelected(event.alternative)
            ActiveSessionEvent.DismissEquipmentSheet -> setState { copy(isEquipmentSheetVisible = false) }
        }
    }

    private fun handleEquipmentUnavailable() {
        setState { 
            copy(
                isEquipmentSheetVisible = true, 
                isEquipmentSheetLoading = true, 
                alternatives = emptyList() 
            ) 
        }
        viewModelScope.launch {
            // In a real implementation, equipment would be fetched from a repository/preferences
            val result = rerouteEquipmentUseCase(
                exerciseName = state.value.currentExerciseName ?: "Current Exercise",
                availableEquipment = emptySet()
            )
            when (result) {
                is Result.Success -> {
                    setState { copy(isEquipmentSheetLoading = false, alternatives = result.value) }
                }
                is Result.Failure -> {
                    setState { 
                        copy(
                            isEquipmentSheetLoading = false, 
                            error = Exception(result.error.message) 
                        ) 
                    }
                }
            }
        }
    }

    private fun handleAlternativeSelected(alternative: ExerciseAlternative) {
        val original = state.value.currentExerciseName ?: "Unknown"
        setState { 
            copy(
                currentExerciseName = alternative.name,
                isEquipmentSheetVisible = false,
                alternatives = emptyList()
            ) 
        }
        analyticsLogger.logEvent(
            "equipment_rerouted", 
            mapOf("original" to original, "alternative" to alternative.name)
        )
        sendAction(
            ActiveSessionAction.Announce(
                "Alternative selected: ${alternative.name}. Target muscles: ${alternative.muscleGroups.joinToString()}."
            )
        )
    }

    private fun handleRepCompleted(event: ActiveSessionEvent.RepCompleted) {
        totalReps++
        
        // Cooldown logic: Re-triggers only if fatigue is detected in next 5 reps, not immediately.
        // We interpret this as: don't show the warning for at least 5 reps after dismissal.
        if (repsSinceDismissal >= 0) {
            repsSinceDismissal++
            if (repsSinceDismissal > 5) {
                repsSinceDismissal = -1
            }
        }

        val status = detectFatigueUseCase.analyzeRep(event.peakPose)
        if (status == FatigueStatus.FATIGUED && repsSinceDismissal == -1) {
            onEvent(ActiveSessionEvent.FatigueDetected)
        }
    }

    override fun onCleared() {
        super.onCleared()
        detectFatigueUseCase.reset()
        detectRepUseCase.reset()
    }
}
