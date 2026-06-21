package com.aml_sakr.fitlife.feature.workout.domain.model

data class WorkoutExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val estimatedDurationMinutes: Int
)
