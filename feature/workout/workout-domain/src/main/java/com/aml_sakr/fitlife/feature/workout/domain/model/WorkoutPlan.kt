package com.aml_sakr.fitlife.feature.workout.domain.model

import com.aml_sakr.fitlife.feature.workout.domain.WorkoutPlanDefaults

data class WorkoutPlan(
    val userId: String,
    val fitnessLevel: WorkoutFitnessLevel,
    val goals: Set<WorkoutGoal>,
    val location: WorkoutLocation,
    val availableEquipment: Set<String>,
    val days: List<WorkoutDay>,
    val generatedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val isFallback: Boolean
) {
    fun isFresh(nowEpochMillis: Long): Boolean = expiresAtEpochMillis > nowEpochMillis

    companion object {
        fun expiresAt(generatedAtEpochMillis: Long): Long =
            generatedAtEpochMillis + WorkoutPlanDefaults.CacheValidityMillis
    }
}
