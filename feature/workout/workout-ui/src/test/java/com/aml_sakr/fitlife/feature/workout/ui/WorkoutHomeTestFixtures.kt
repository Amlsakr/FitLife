package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

object WorkoutHomeTestFixtures {
    fun sampleRequest(): WorkoutGenerationRequest =
        WorkoutGenerationRequest(
            userId = "user-1",
            fitnessLevel = WorkoutFitnessLevel.Beginner,
            goals = setOf(WorkoutGoal.GeneralHealth),
            location = WorkoutLocation.Home,
            availableEquipment = setOf("bodyweight", "chair")
        )

    fun samplePlan(
        dayCount: Int = 7,
        dayTitlePrefix: String = "Day"
    ): WorkoutPlan =
        WorkoutPlan(
            userId = "user-1",
            fitnessLevel = WorkoutFitnessLevel.Beginner,
            goals = setOf(WorkoutGoal.GeneralHealth),
            location = WorkoutLocation.Home,
            availableEquipment = setOf("bodyweight", "chair"),
            days = (1..dayCount).map { day ->
                WorkoutDay(
                    day = day,
                    title = "$dayTitlePrefix $day",
                    durationMinutes = 30,
                    exercises = listOf(
                        WorkoutExercise(
                            name = "Bodyweight squat",
                            sets = 3,
                            reps = "10",
                            estimatedDurationMinutes = 8
                        )
                    )
                )
            },
            generatedAtEpochMillis = 1_000L,
            expiresAtEpochMillis = 2_000L,
            isFallback = false
        )
}
