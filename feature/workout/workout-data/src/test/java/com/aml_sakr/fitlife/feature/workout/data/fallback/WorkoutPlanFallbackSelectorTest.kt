package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.core.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.core.domain.model.WorkoutLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutPlanFallbackSelectorTest {
    @Test
    fun `selects best matching fallback plan for profile and equipment`() {
        val request = request()
        val logger = RecordingWorkoutPlanFallbackLogger()
        val selector = WorkoutPlanFallbackSelector(logger)

        val result = selector.select(
            request = request,
            templates = listOf(
                template(
                    id = "mismatch",
                    fitnessLevel = WorkoutFitnessLevel.Intermediate,
                    location = WorkoutLocation.Gym,
                    requiredEquipment = listOf("dumbbells")
                ),
                template(
                    id = "best",
                    fitnessLevel = WorkoutFitnessLevel.Beginner,
                    location = WorkoutLocation.Home,
                    requiredEquipment = listOf("bodyweight", "chair")
                )
            )
        )

        assertTrue(result is Result.Success)
        assertEquals("best", (result as Result.Success).data.id)
        assertEquals("best", logger.recordedTemplateId)
    }

    @Test
    fun `rejects partial equipment matches when required equipment is missing`() {
        val request = request().copy(availableEquipment = setOf("bodyweight"))
        val selector = WorkoutPlanFallbackSelector()

        val result = selector.select(
            request = request,
            templates = listOf(
                template(
                    id = "partial",
                    fitnessLevel = WorkoutFitnessLevel.Beginner,
                    location = WorkoutLocation.Home,
                    requiredEquipment = listOf("bodyweight", "chair")
                ),
                template(
                    id = "unrelated",
                    fitnessLevel = WorkoutFitnessLevel.Beginner,
                    location = WorkoutLocation.Home,
                    requiredEquipment = listOf("dumbbells")
                )
            )
        )

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.NoMatchingFallbackTemplate), result)
    }

    @Test
    fun `returns failure when no fallback template matches`() {
        val selector = WorkoutPlanFallbackSelector()

        val result = selector.select(
            request = request(),
            templates = listOf(
                template(
                    id = "mismatch",
                    fitnessLevel = WorkoutFitnessLevel.Intermediate,
                    location = WorkoutLocation.Gym,
                    requiredEquipment = listOf("dumbbells")
                )
            )
        )

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.NoMatchingFallbackTemplate), result)
    }

    private fun request() = WorkoutGenerationRequest(
        userId = "user-1",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight", "chair"),
        requestedDays = 7
    )

    private fun template(
        id: String,
        fitnessLevel: WorkoutFitnessLevel,
        location: WorkoutLocation,
        requiredEquipment: List<String>
    ) = WorkoutPlanFallbackTemplate(
        id = id,
        fitnessLevel = fitnessLevel,
        location = location,
        requiredEquipment = requiredEquipment,
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
    )
}

private class RecordingWorkoutPlanFallbackLogger : WorkoutPlanFallbackLogger {
    var recordedTemplateId: String? = null

    override fun onFallbackSelected(
        request: WorkoutGenerationRequest,
        templateId: String
    ) {
        recordedTemplateId = templateId
    }
}
