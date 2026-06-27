package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutHomeExtensionsTest {

    @Test
    fun calculateTotalReps_normalDigits() {
        val day = buildWorkoutDayWithReps("10", sets = 3)
        assertEquals(30, day.calculateTotalReps())
    }

    @Test
    fun calculateTotalReps_range() {
        val day = buildWorkoutDayWithReps("8-10", sets = 3)
        // Should parse the first number "8" and result in 24 reps
        assertEquals(24, day.calculateTotalReps())
    }

    @Test
    fun calculateTotalReps_textAndDigits() {
        val day = buildWorkoutDayWithReps("12 reps", sets = 3)
        assertEquals(36, day.calculateTotalReps())
    }

    @Test
    fun calculateTotalReps_noDigits() {
        val day = buildWorkoutDayWithReps("As many as possible", sets = 3)
        assertEquals(0, day.calculateTotalReps())
    }

    @Test
    fun calculateTotalReps_emptyString() {
        val day = buildWorkoutDayWithReps("", sets = 3)
        assertEquals(0, day.calculateTotalReps())
    }

    @Test
    fun calculateTotalReps_multipleExercises() {
        val day = WorkoutDay(
            day = 1,
            title = "Mix",
            durationMinutes = 30,
            exercises = listOf(
                WorkoutExercise("Push-up", sets = 3, reps = "10", estimatedDurationMinutes = 5),
                WorkoutExercise("Plank", sets = 2, reps = "30s", estimatedDurationMinutes = 5), // "30"
                WorkoutExercise("Squat", sets = 4, reps = "8-12", estimatedDurationMinutes = 5) // "8"
            )
        )
        // 3*10 + 2*30 + 4*8 = 30 + 60 + 32 = 122 reps
        assertEquals(122, day.calculateTotalReps())
    }

    private fun buildWorkoutDayWithReps(reps: String, sets: Int): WorkoutDay {
        return WorkoutDay(
            day = 1,
            title = "Test Day",
            durationMinutes = 30,
            exercises = listOf(
                WorkoutExercise(
                    name = "Test Exercise",
                    sets = sets,
                    reps = reps,
                    estimatedDurationMinutes = 10
                )
            )
        )
    }
}
