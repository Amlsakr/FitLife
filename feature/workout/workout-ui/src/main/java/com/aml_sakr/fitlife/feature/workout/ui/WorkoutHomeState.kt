package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

sealed interface WorkoutHomeState : UIState {
    val renderBranch: WorkoutHomeRenderBranch
    val primaryCtaLabel: String
    val canRequestPlan: Boolean
    val isRequestInFlight: Boolean

    data object Loading : WorkoutHomeState {
        override val renderBranch: WorkoutHomeRenderBranch = WorkoutHomeRenderBranch.Loading
        override val primaryCtaLabel: String = "Generating"
        override val canRequestPlan: Boolean = false
        override val isRequestInFlight: Boolean = true
    }

    data object Empty : WorkoutHomeState {
        override val renderBranch: WorkoutHomeRenderBranch = WorkoutHomeRenderBranch.Empty
        override val primaryCtaLabel: String = WorkoutHomeCopy.EmptyCtaLabel
        override val canRequestPlan: Boolean = true
        override val isRequestInFlight: Boolean = false
    }

    data class Success(
        val plan: WorkoutPlan,
        val isRefreshing: Boolean = false
    ) : WorkoutHomeState {
        override val renderBranch: WorkoutHomeRenderBranch = WorkoutHomeRenderBranch.Success
        override val primaryCtaLabel: String = if (isRefreshing) "Refreshing" else "Refresh plan"
        override val canRequestPlan: Boolean = !isRefreshing
        override val isRequestInFlight: Boolean = isRefreshing
    }

    data class Error(
        val message: String,
        val isRetrying: Boolean = false
    ) : WorkoutHomeState {
        override val renderBranch: WorkoutHomeRenderBranch = WorkoutHomeRenderBranch.Error
        override val primaryCtaLabel: String = if (isRetrying) "Retrying" else "Try again"
        override val canRequestPlan: Boolean = !isRetrying
        override val isRequestInFlight: Boolean = isRetrying
    }
}

enum class WorkoutHomeRenderBranch {
    Loading,
    Success,
    Empty,
    Error
}

object WorkoutHomeCopy {
    const val EmptyCtaLabel = "Generate a plan"
    const val GenericErrorMessage = "We couldn't prepare your workout plan. Please try again."
    const val ConnectionErrorMessage = "We couldn't generate your plan. Check your connection and try again."
    const val BusyErrorMessage = "Plan generation is busy right now. Try again in a moment."
    const val ProfileMismatchMessage = "We couldn't find a plan for this profile yet. Try updating your goals."
    const val InvalidPlanMessage = "We couldn't prepare a complete 7-day workout plan. Please try again."
}
