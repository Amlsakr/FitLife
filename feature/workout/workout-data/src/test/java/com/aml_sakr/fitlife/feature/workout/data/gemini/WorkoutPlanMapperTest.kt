package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackDay
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackExercise
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackTemplate
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutPlanMapperTest {
    private val mapper = WorkoutPlanMapper()

    @Test
    fun `maps gemini draft into production workout plan`() {
        val request = request()
        val plan = mapper.fromGeminiDraft(
            request = request,
            draft = GeminiWorkoutPlanDraft(
                days = listOf(
                    GeminiWorkoutDayDraft(
                        day = 1,
                        title = "Day 1",
                        durationMinutes = 30,
                        exercises = listOf(
                            GeminiWorkoutExerciseDraft(
                                name = "Squat",
                                sets = 3,
                                reps = "10",
                                estimatedDurationMinutes = 8
                            )
                        )
                    )
                )
            ),
            generatedAtEpochMillis = 1_000L
        )

        assertEquals(request.userId, plan.userId)
        assertFalse(plan.isFallback)
        assertEquals(1, plan.days.size)
        assertTrue(plan.isFresh(1_500L))
    }

    @Test
    fun `maps fallback template into production workout plan`() {
        val request = request()
        val plan = mapper.fromFallbackTemplate(
            request = request,
            template = WorkoutPlanFallbackTemplate(
                id = "fallback-1",
                fitnessLevel = WorkoutFitnessLevel.Beginner,
                location = WorkoutLocation.Home,
                requiredEquipment = listOf("bodyweight"),
                days = listOf(
                    WorkoutPlanFallbackDay(
                        day = 1,
                        title = "Day 1",
                        durationMinutes = 30,
                        exercises = listOf(
                            WorkoutPlanFallbackExercise(
                                name = "Squat",
                                sets = 3,
                                reps = "10",
                                estimatedDurationMinutes = 8
                            )
                        )
                    )
                )
            ),
            generatedAtEpochMillis = 1_000L
        )

        assertTrue(plan.isFallback)
        assertEquals(1, plan.days.size)
        assertEquals(1_000L + 24L * 60L * 60L * 1000L, plan.expiresAtEpochMillis)
    }

    private fun request() = WorkoutGenerationRequest(
        userId = "user-1",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight"),
        requestedDays = 7
    )
}
