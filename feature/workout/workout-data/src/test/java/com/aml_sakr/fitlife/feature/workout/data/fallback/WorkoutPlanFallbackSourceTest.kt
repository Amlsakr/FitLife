package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanMapper
import com.aml_sakr.fitlife.core.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.core.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.core.domain.model.WorkoutLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

class WorkoutPlanFallbackSourceTest {
    @Test
    fun `loads fallback plan from asset catalog`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader(fallbackCatalogJson()),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertTrue(result is Result.Success)
        assertEquals(7, (result as Result.Success).data.days.size)
        assertTrue(result.data.isFallback)
    }

    @Test
    fun `production fallback asset supports beginner home bodyweight and chair profile`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader(productionFallbackAssetJson()),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertTrue(result is Result.Success)
        assertEquals(7, (result as Result.Success).data.days.size)
        assertTrue(result.data.isFallback)
    }

    @Test
    fun `returns safe failure when fallback templates are missing`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader("""{"templates":[]}"""),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.NoMatchingFallbackTemplate), result)
    }

    @Test
    fun `returns safe failure when fallback catalog is null`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader("null"),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.FallbackAssetUnavailable), result)
    }

    @Test
    fun `returns safe failure when fallback catalog omits templates`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader("{}"),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.FallbackAssetUnavailable), result)
    }

    @Test
    fun `returns safe failure when fallback template omits required equipment`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader(
                """
                {
                  "templates": [
                    {
                      "id": "missing-equipment",
                      "fitnessLevel": "Beginner",
                      "location": "Home",
                      "days": [${fallbackDaysJson()}]
                    }
                  ]
                }
                """.trimIndent()
            ),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.FallbackAssetUnavailable), result)
    }

    @Test
    fun `returns safe failure when fallback template is not a seven day plan`() {
        val source = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader(
                """
                {
                  "templates": [
                    {
                      "id": "six-day-plan",
                      "fitnessLevel": "Beginner",
                      "location": "Home",
                      "requiredEquipment": ["bodyweight", "chair"],
                      "days": [
                        ${(1..6).joinToString(separator = ",") { fallbackDayJson(it) }}
                      ]
                    }
                  ]
                }
                """.trimIndent()
            ),
            selector = WorkoutPlanFallbackSelector(),
            mapper = WorkoutPlanMapper()
        )

        val result = source.load(request(), generatedAtEpochMillis = 1_000L)

        assertEquals(Result.Failure(com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError.FallbackAssetUnavailable), result)
    }

    private fun request() = WorkoutGenerationRequest(
        userId = "user-1",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight", "chair"),
        requestedDays = 7
    )

    private fun fallbackCatalogJson(): String = """
        {
          "templates": [
            {
              "id": "beginner-home-bodyweight",
              "fitnessLevel": "Beginner",
              "location": "Home",
              "requiredEquipment": ["bodyweight", "chair"],
              "days": [
                ${fallbackDaysJson()}
              ]
            }
          ]
        }
    """.trimIndent()

    private fun fallbackDaysJson(): String =
        (1..7).joinToString(separator = ",") { day -> fallbackDayJson(day) }

    private fun fallbackDayJson(day: Int): String = """
        {
          "day": $day,
          "title": "Day $day",
          "durationMinutes": 30,
          "exercises": [
            { "name": "Squat", "sets": 3, "reps": "10", "estimatedDurationMinutes": 8 }
          ]
        }
    """.trimIndent()

    private fun productionFallbackAssetJson(): String {
        val candidates = listOf(
            File("src/main/assets/fallback_workout_plans.json"),
            File("feature/workout/workout-data/src/main/assets/fallback_workout_plans.json")
        )
        return candidates.first { it.exists() }.readText()
    }
}

private class FakeAssetReader(
    private val content: String
) : WorkoutPlanAssetReader {
    override fun open(path: String) = ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8))
}
