package com.aml_sakr.fitlife.feature.workout.domain.model

data class WorkoutDay(
    val day: Int,
    val title: String,
    val durationMinutes: Int,
    val exercises: List<WorkoutExercise>
)
