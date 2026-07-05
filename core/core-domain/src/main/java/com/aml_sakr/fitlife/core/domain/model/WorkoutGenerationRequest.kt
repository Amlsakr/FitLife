package com.aml_sakr.fitlife.core.domain.model

data class WorkoutGenerationRequest(
    val userId: String,
    val fitnessLevel: WorkoutFitnessLevel,
    val goals: Set<WorkoutGoal>,
    val location: WorkoutLocation,
    val availableEquipment: Set<String>,
    val requestedDays: Int
)
