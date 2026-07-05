package com.aml_sakr.fitlife.core.domain.model

data class WorkoutDay(
    val day: Int,
    val title: String,
    val durationMinutes: Int,
    val exercises: List<WorkoutExercise>
)
