package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent

sealed interface WorkoutHomeEvent : UIEvent {
    data object RequestPlan : WorkoutHomeEvent
}
