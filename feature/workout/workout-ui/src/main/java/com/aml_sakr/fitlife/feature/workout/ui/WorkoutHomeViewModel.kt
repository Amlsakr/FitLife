package com.aml_sakr.fitlife.feature.workout.ui

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.workout.domain.WorkoutPlanDefaults
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.usecase.GenerateWorkoutPlanUseCase
import kotlinx.coroutines.launch

class WorkoutHomeViewModel(
    private val generateWorkoutPlan: GenerateWorkoutPlanUseCase,
    private val request: WorkoutGenerationRequest,
    initialState: WorkoutHomeState = WorkoutHomeState.Empty
) : BaseMviViewModel<WorkoutHomeState, WorkoutHomeEvent, WorkoutHomeAction>(initialState) {
    override fun handleEvent(event: WorkoutHomeEvent) {
        when (event) {
            WorkoutHomeEvent.RequestPlan -> requestPlan()
        }
    }

    private fun requestPlan() {
        val currentState = state.value
        if (!currentState.canRequestPlan) return

        setState {
            when (currentState) {
                WorkoutHomeState.Empty -> WorkoutHomeState.Loading
                WorkoutHomeState.Loading -> WorkoutHomeState.Loading
                is WorkoutHomeState.Success -> currentState.copy(isRefreshing = true)
                is WorkoutHomeState.Error -> currentState.copy(isRetrying = true)
            }
        }

        viewModelScope.launch {
            when (val result = generateWorkoutPlan(request)) {
                is Result.Success -> handlePlanResult(result.value)
                is Result.Failure -> handleFailure(result.error)
            }
        }
    }

    private fun handlePlanResult(plan: WorkoutPlan) {
        if (plan.days.size != WorkoutPlanDefaults.DefaultRequestedDays) {
            val message = WorkoutHomeCopy.InvalidPlanMessage
            setState { WorkoutHomeState.Error(message) }
            sendAction(WorkoutHomeAction.ShowMessage(message))
            return
        }

        setState { WorkoutHomeState.Success(plan) }
    }

    private fun handleFailure(error: WorkoutGenerationError) {
        val message = error.toSafeMessage()
        setState { WorkoutHomeState.Error(message) }
        sendAction(WorkoutHomeAction.ShowMessage(message))
    }

    private fun WorkoutGenerationError.toSafeMessage(): String =
        when (this) {
            WorkoutGenerationError.RemoteNetworkError,
            WorkoutGenerationError.RemoteTimeout -> WorkoutHomeCopy.ConnectionErrorMessage
            WorkoutGenerationError.RemoteRateLimited -> WorkoutHomeCopy.BusyErrorMessage
            WorkoutGenerationError.NoMatchingFallbackTemplate -> WorkoutHomeCopy.ProfileMismatchMessage
            WorkoutGenerationError.CacheUnavailable,
            WorkoutGenerationError.FallbackAssetUnavailable,
            WorkoutGenerationError.PersistenceFailed,
            WorkoutGenerationError.RemoteHttpError,
            WorkoutGenerationError.RemoteParseError -> WorkoutHomeCopy.GenericErrorMessage
        }
}
