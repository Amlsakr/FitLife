package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.aml_sakr.fitlife.core.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.core.domain.model.WorkoutLocation

data class WorkoutPlanFallbackCatalog(
    val templates: List<WorkoutPlanFallbackTemplate>?
)

data class WorkoutPlanFallbackTemplate(
    val id: String,
    val fitnessLevel: WorkoutFitnessLevel,
    val location: WorkoutLocation,
    val requiredEquipment: List<String>,
    val days: List<WorkoutPlanFallbackDay>
)

data class WorkoutPlanFallbackDay(
    val day: Int,
    val title: String,
    val durationMinutes: Int,
    val exercises: List<WorkoutPlanFallbackExercise>
)

data class WorkoutPlanFallbackExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val estimatedDurationMinutes: Int
)
