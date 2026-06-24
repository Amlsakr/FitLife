package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction

sealed interface WorkoutHomeAction : OneTimeAction {
    data class ShowMessage(val message: String) : WorkoutHomeAction
}
