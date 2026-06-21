package com.aml_sakr.fitlife.feature.workout.domain.model

import com.aml_sakr.fitlife.feature.workout.domain.WorkoutPlanDefaults

data class WorkoutGenerationRequest(
    val userId: String,
    val fitnessLevel: WorkoutFitnessLevel,
    val goals: Set<WorkoutGoal>,
    val location: WorkoutLocation,
    val availableEquipment: Set<String>,
    val requestedDays: Int = WorkoutPlanDefaults.DefaultRequestedDays
) {
    init {
        require(requestedDays == WorkoutPlanDefaults.DefaultRequestedDays) {
            "Workout generation currently supports exactly ${WorkoutPlanDefaults.DefaultRequestedDays} days."
        }
    }
}
