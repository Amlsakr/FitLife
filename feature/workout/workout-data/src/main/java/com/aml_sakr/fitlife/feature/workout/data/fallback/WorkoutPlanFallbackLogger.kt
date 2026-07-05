package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest

fun interface WorkoutPlanFallbackLogger {
    fun onFallbackSelected(request: WorkoutGenerationRequest, templateId: String)
}

object NoOpWorkoutPlanFallbackLogger : WorkoutPlanFallbackLogger {
    override fun onFallbackSelected(request: WorkoutGenerationRequest, templateId: String) = Unit
}
