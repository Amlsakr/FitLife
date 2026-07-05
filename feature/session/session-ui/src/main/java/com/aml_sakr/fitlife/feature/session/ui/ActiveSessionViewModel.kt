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
import com.aml_sakr.fitlife.feature.session.domain.usecase.SaveSessionUseCase
import com.aml_sakr.fitlife.core.domain.usecase.GetWorkoutPlanUseCase
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.core.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.core.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
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
    private val rerouteEquipmentUseCase: RerouteEquipmentUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val getWorkoutPlanUseCase: GetWorkoutPlanUseCase
) : BaseMviViewModel<ActiveSessionState, ActiveSessionEvent, ActiveSessionAction>(
    ActiveSessionState()
) {
    private var repsSinceDismissal = -1
    private val poseDataFlow = MutableSharedFlow<PoseData>(extraBufferCapacity = 1)
    private var equipmentJob: kotlinx.coroutines.Job? = null
    private val isSavingSession = AtomicBoolean(false)

    init {
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
                    setState { copy(isFatigued = true, fatigueEventCount = state.value.fatigueEventCount + 1) }
                    analyticsLogger.logEvent("fatigue_detected", mapOf("rep_number" to state.value.totalReps))
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
            ActiveSessionEvent.DismissEquipmentSheet -> {
                equipmentJob?.cancel()
                setState { copy(isEquipmentSheetVisible = false, isEquipmentSheetLoading = false) }
            }
            is ActiveSessionEvent.Initialize -> {
                setState {
                    copy(
                        userId = event.userId,
                        planId = event.planId,
                        workoutDayId = event.workoutDayId
                    )
                }
                loadInitialExercise(event.planId, event.workoutDayId)
            }
            ActiveSessionEvent.FinishSession -> handleFinishSession()
        }
    }

    private fun loadInitialExercise(planId: String, workoutDayId: String) {
        viewModelScope.launch {
            when (val result = getWorkoutPlanUseCase(planId)) {
                is Result.Success<WorkoutPlan?> -> {
                    val plan = result.value ?: return@launch
                    val dayNum = workoutDayId.toIntOrNull() ?: 1
                    val day = plan.days.find { it.day == dayNum } ?: plan.days.firstOrNull()
                    val exercise = day?.exercises?.firstOrNull()
                    
                    if (exercise != null) {
                        setState {
                            copy(
                                currentExerciseName = exercise.name,
                                currentExerciseLottiePath = mapExerciseToLottiePath(exercise.name)
                            )
                        }
                    }
                }
                is Result.Failure<*> -> {
                    setState { copy(error = Exception("Failed to load workout plan")) }
                }
            }
        }
    }

    private fun mapExerciseToLottiePath(exerciseName: String): String? {
        // Temporary mapping until Exercise Library is implemented in Room
        val normalized = exerciseName.lowercase().trim().replace(Regex("[^a-z0-9]"), "_")
        return "lottie/$normalized.json"
    }

    private fun handleFinishSession() {
        if (state.value.isFinishing || isSavingSession.getAndSet(true)) return
        setState { copy(isFinishing = true) }

        val currentState = state.value
        val endTime = System.currentTimeMillis()
        val durationSeconds = ((endTime - currentState.startTime) / 1000).toInt()
        val sessionId = UUID.randomUUID().toString()

        val session = Session(
            sessionId = sessionId,
            userId = currentState.userId,
            planId = currentState.planId,
            workoutDayId = currentState.workoutDayId,
            startTime = currentState.startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            totalReps = currentState.totalReps,
            totalSets = currentState.totalSets,
            fatigueEventCount = currentState.fatigueEventCount,
            audioFallbackUsed = currentState.isAudioOnlyMode,
            completionPercentage = 1.0f,
            whatsAppShared = false
        )

        viewModelScope.launch {
            when (val result = saveSessionUseCase(session)) {
                is Result.Success<Unit> -> {
                    analyticsLogger.logEvent("session_completed", mapOf(
                        "duration_secs" to durationSeconds,
                        "total_reps" to currentState.totalReps,
                        "fatigue_events" to currentState.fatigueEventCount
                    ))
                    sendAction(ActiveSessionAction.NavigateToSummary(sessionId))
                }
                is Result.Failure<*> -> {
                    isSavingSession.set(false)
                    setState { copy(error = Exception("Save failed"), isFinishing = false) }
                }
            }
        }
    }

    private fun handleEquipmentUnavailable() {
        if (state.value.isEquipmentSheetLoading) return
        
        setState { 
            copy(
                isEquipmentSheetVisible = true, 
                isEquipmentSheetLoading = true, 
                alternatives = emptyList() 
            ) 
        }
        equipmentJob?.cancel()
        equipmentJob = viewModelScope.launch {
            // In a real implementation, equipment would be fetched from a repository/preferences
            val result = rerouteEquipmentUseCase(
                exerciseName = state.value.currentExerciseName ?: "Current Exercise",
                availableEquipment = emptySet()
            )
            when (result) {
                is Result.Success<List<ExerciseAlternative>> -> {
                    setState { copy(isEquipmentSheetLoading = false, alternatives = result.value) }
                }
                is Result.Failure<*> -> {
                    setState { 
                        copy(
                            isEquipmentSheetLoading = false, 
                            error = Exception("Failed to load alternatives")
                        ) 
                    }
                }
            }
        }
    }

    private fun handleAlternativeSelected(alternative: ExerciseAlternative) {
        val original = state.value.currentExerciseName ?: "Unknown"
        val nextLottiePath = alternative.lottieAssetPath?.takeIf { it.isNotBlank() }
        
        setState { 
            copy(
                currentExerciseName = alternative.name,
                currentExerciseLottiePath = nextLottiePath,
                isEquipmentSheetVisible = false,
                alternatives = emptyList()
            ) 
        }
        analyticsLogger.logEvent(
            "equipment_rerouted", 
            mapOf("original" to original, "alternative" to alternative.name)
        )
        
        val sanitizedName = alternative.name.replace("_", " ")
        val muscles = if (alternative.muscleGroups.isNotEmpty()) {
            ". Target muscles: ${alternative.muscleGroups.joinToString()}."
        } else "."
        
        sendAction(
            ActiveSessionAction.Announce(
                "Alternative selected: $sanitizedName$muscles"
            )
        )
    }

    private fun handleRepCompleted(event: ActiveSessionEvent.RepCompleted) {
        val newTotalReps = state.value.totalReps + 1
        setState { copy(totalReps = newTotalReps) }
        
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
