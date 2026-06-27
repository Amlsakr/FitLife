package com.aml_sakr.fitlife.feature.session.ui

import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.session.domain.pose.AnalyzePoseUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectFatigueUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectRepUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.FatigueStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the active workout session.
 * Handles pose detection events and updates the UI state.
 * AC 4, 7, 8 compliance:
 * - Handles fatigue detection via [DetectFatigueUseCase].
 * - Implements dismissal cooldown (5 reps).
 * - Logs analytics events via [AnalyticsLogger].
 */
@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    val analyzePoseUseCase: AnalyzePoseUseCase,
    private val detectFatigueUseCase: DetectFatigueUseCase,
    private val detectRepUseCase: DetectRepUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseMviViewModel<ActiveSessionState, ActiveSessionEvent, ActiveSessionAction>(
    ActiveSessionState()
) {
    private var repsSinceDismissal = -1
    private var totalReps = 0

    override fun handleEvent(event: ActiveSessionEvent) {
        when (event) {
            is ActiveSessionEvent.PoseDetected -> {
                setState { copy(latestPoseData = event.poseData) }
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
        }
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
