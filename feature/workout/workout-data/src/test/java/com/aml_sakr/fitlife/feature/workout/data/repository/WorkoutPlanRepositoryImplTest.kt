package com.aml_sakr.fitlife.feature.workout.data.repository

import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDao
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanEntity
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanAssetReader
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackSource
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiApiCallResult
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiApiService
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiPlanResponseParser
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutDayDraft
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutExerciseDraft
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutPlanDraft
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutPromptBuilder
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutGeminiGatewayConfiguration
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanFailureClassifier
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanMapper
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.core.domain.model.WorkoutDay
import com.aml_sakr.fitlife.core.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.core.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.core.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.core.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.core.domain.model.WorkoutPlan
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutPlanRepositoryImplTest {
    private val gson = Gson()

    @Test
    fun `save plan clears stale rows before inserting mapped entity`() = runTest {
        val dao = RecordingWorkoutPlanDao()
        val repository = repository(workoutPlanDao = dao)
        val plan = samplePlan(
            generatedAtEpochMillis = 1_000L,
            expiresAtEpochMillis = 1_000L + 24L * 60L * 60L * 1000L,
            isFallback = false
        )

        assertEquals(Result.Success(Unit), repository.savePlan(plan))
        assertEquals(1_000L, dao.clearedAt)
        assertNotNull(dao.insertedEntity)
        assertEquals(plan.userId, dao.insertedEntity?.userId)
        assertEquals(plan.toRequestKey(), dao.insertedEntity?.requestKey)
        assertEquals(gson.toJson(plan), dao.insertedEntity?.planJson)
    }

    @Test
    fun `treats malformed stored payload as a cache miss`() = runTest {
        val dao = RecordingWorkoutPlanDao()
        val repository = repository(workoutPlanDao = dao)
        dao.latestByRequestKey = malformedCachedEntity()

        val cached = repository.getCachedPlan(request())

        assertEquals(Result.Success(null), cached)
    }

    @Test
    fun `returns cache miss when dao returns no matching row`() = runTest {
        val dao = RecordingWorkoutPlanDao()
        val repository = repository(workoutPlanDao = dao)

        val cached = repository.getCachedPlan(request())

        assertEquals(Result.Success(null), cached)
        assertEquals(request().toRequestKey(), dao.latestRequestKey)
        assertEquals(1_000L, dao.latestRequestNowEpochMillis)
    }

    @Test
    fun `retries bounded transient rate limits with backoff before succeeding`() = runTest {
        val apiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 429, responseBody = "", responseSizeChars = 0),
                GeminiApiCallResult(httpStatusCode = 429, responseBody = "", responseSizeChars = 0),
                GeminiApiCallResult(httpStatusCode = 200, responseBody = generateSuccessResponse(), responseSizeChars = 1_024)
            )
        )
        val repository = repository(apiService = apiService)

        val result = repository.generateRemotePlan(request())

        assertTrue(result is Result.Success)
        assertEquals(3, apiService.callCount)
        assertEquals(750L, currentTime)
        assertFalse((result as Result.Success).data.isFallback)
    }

    @Test
    fun `returns timeout failure after bounded retries on transport timeout`() = runTest {
        val apiService = ThrowingTimeoutGeminiApiService()
        val repository = repository(
            apiService = apiService,
            configuration = WorkoutGeminiGatewayConfiguration(
                apiKey = "test-key",
                modelName = "models/test-model",
                timeoutMillis = 5_000L,
                maxRetries = 2,
                backoffMillis = 250L
            )
        )

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteTimeout), result)
        assertEquals(3, apiService.callCount)
        assertEquals(750L, currentTime)
    }

    @Test
    fun `returns timeout failure when the overall timeout budget is exceeded`() = runTest {
        val apiService = HangingGeminiApiService(
            responseDelayMillis = 6_000L
        )
        val repository = repository(
            apiService = apiService,
            configuration = WorkoutGeminiGatewayConfiguration(
                apiKey = "test-key",
                modelName = "models/test-model",
                timeoutMillis = 5_000L,
                maxRetries = 2,
                backoffMillis = 250L
            )
        )

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteTimeout), result)
        assertEquals(1, apiService.callCount)
        assertEquals(5_000L, currentTime)
    }

    @Test
    fun `returns network failure without crashing when model name is missing`() = runTest {
        val apiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 200, responseBody = generateSuccessResponse(), responseSizeChars = 1_024)
            )
        )
        val repository = repository(
            apiService = apiService,
            configuration = WorkoutGeminiGatewayConfiguration(
                apiKey = "test-key",
                modelName = "",
                maxRetries = 2,
                backoffMillis = 250L
            )
        )

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteNetworkError), result)
        assertEquals(0, apiService.callCount)
    }

    @Test
    fun `returns http failure for non retryable non success responses`() = runTest {
        val apiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 400, responseBody = """{"error":"bad request"}""", responseSizeChars = 23)
            )
        )
        val repository = repository(apiService = apiService)

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteHttpError), result)
        assertEquals(1, apiService.callCount)
    }

    @Test
    fun `returns http failure after bounded retries on server errors`() = runTest {
        val apiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 500, responseBody = """{"error":"server"}""", responseSizeChars = 18)
            )
        )
        val repository = repository(apiService = apiService)

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteHttpError), result)
        assertEquals(3, apiService.callCount)
        assertEquals(750L, currentTime)
    }

    @Test
    fun `returns parse failure for malformed successful responses`() = runTest {
        val apiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 200, responseBody = """{"candidates":[]}""", responseSizeChars = 17)
            )
        )
        val repository = repository(apiService = apiService)

        val result = repository.generateRemotePlan(request())

        assertEquals(Result.Failure(WorkoutGenerationError.RemoteParseError), result)
        assertEquals(1, apiService.callCount)
    }

    private fun repository(
        workoutPlanDao: WorkoutPlanDao = RecordingWorkoutPlanDao(),
        apiService: GeminiApiService = SequencedGeminiApiService(
            responses = listOf(
                GeminiApiCallResult(httpStatusCode = 200, responseBody = generateSuccessResponse(), responseSizeChars = 1_024)
            )
        ),
        configuration: WorkoutGeminiGatewayConfiguration = WorkoutGeminiGatewayConfiguration(
            apiKey = "test-key",
            modelName = "models/test-model",
            maxRetries = 2,
            backoffMillis = 250L
        )
    ): WorkoutPlanRepositoryImpl = WorkoutPlanRepositoryImpl(
        workoutPlanDao = workoutPlanDao,
        geminiConfiguration = configuration,
        apiService = apiService,
        promptBuilder = GeminiWorkoutPromptBuilder(),
        responseParser = GeminiPlanResponseParser(),
        fallbackSource = WorkoutPlanFallbackSource(
            assetReader = FakeAssetReader(fallbackCatalogJson()),
            mapper = WorkoutPlanMapper(),
            gson = gson
        ),
        mapper = WorkoutPlanMapper(),
        failureClassifier = WorkoutPlanFailureClassifier(),
        gson = gson,
        clock = FixedWorkoutPlanClock(1_000L)
    )

    private fun request() = WorkoutGenerationRequest(
        userId = "user-1",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight", "chair"),
        requestedDays = 7
    )

    private fun samplePlan(
        generatedAtEpochMillis: Long,
        expiresAtEpochMillis: Long,
        isFallback: Boolean
    ) = WorkoutPlan(
        userId = "user-1",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight", "chair"),
        days = (1..7).map { day ->
            WorkoutDay(
                day = day,
                title = "Day $day",
                durationMinutes = 30,
                exercises = listOf(
                    WorkoutExercise(
                        name = "Squat",
                        sets = 3,
                        reps = "10",
                        estimatedDurationMinutes = 8
                    )
                )
            )
        },
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = expiresAtEpochMillis,
        isFallback = isFallback
    )

    private fun malformedCachedEntity(): WorkoutPlanEntity = WorkoutPlanEntity(
        planId = "broken-plan",
        userId = request().userId,
        requestKey = request().toRequestKey(),
        generatedAtEpochMillis = 1_000L,
        expiresAtEpochMillis = 1_000L + 24L * 60L * 60L * 1000L,
        fitnessLevel = request().fitnessLevel.name,
        location = request().location.name,
        requestedDays = request().requestedDays,
        goalNames = request().goals.map { it.name },
        equipmentNames = request().availableEquipment.map { it.lowercase() },
        weekNumber = 1,
        isFallback = false,
        planJson = """{"not":"a-workout-plan"}"""
    )

    private fun generateSuccessResponse(): String {
        val planJson = gson.toJson(
            GeminiWorkoutPlanDraft(
                days = (1..7).map { day ->
                    GeminiWorkoutDayDraft(
                        day = day,
                        title = "Day $day",
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
                }
            )
        )
        return """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": ${gson.toJson(planJson)} }
                    ]
                  }
                }
              ],
              "usageMetadata": {
                "promptTokenCount": 100,
                "candidatesTokenCount": 200
              }
            }
        """.trimIndent()
    }

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
        (1..7).joinToString(separator = ",") { day ->
            """
            {
              "day": $day,
              "title": "Day $day",
              "durationMinutes": 30,
              "exercises": [
                { "name": "Squat", "sets": 3, "reps": "10", "estimatedDurationMinutes": 8 }
              ]
            }
            """.trimIndent()
        }
}

private class SequencedGeminiApiService(
    private val responses: List<GeminiApiCallResult>
) : GeminiApiService {
    var callCount: Int = 0

    override suspend fun generatePlan(
        request: com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiGenerateContentRequest,
        apiKey: String,
        configuration: com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
    ): GeminiApiCallResult {
        val response = responses[minOf(callCount, responses.lastIndex)]
        callCount += 1
        return response
    }
}

private class ThrowingTimeoutGeminiApiService : GeminiApiService {
    var callCount: Int = 0

    override suspend fun generatePlan(
        request: com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiGenerateContentRequest,
        apiKey: String,
        configuration: com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
    ): GeminiApiCallResult {
        callCount += 1
        throw java.net.SocketTimeoutException("timed out")
    }
}

private class HangingGeminiApiService(
    private val responseDelayMillis: Long
) : GeminiApiService {
    var callCount: Int = 0

    override suspend fun generatePlan(
        request: com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiGenerateContentRequest,
        apiKey: String,
        configuration: com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
    ): GeminiApiCallResult {
        callCount += 1
        delay(responseDelayMillis)
        return GeminiApiCallResult(httpStatusCode = 200, responseBody = "", responseSizeChars = 0)
    }
}

private class FakeAssetReader(
    private val content: String
) : WorkoutPlanAssetReader {
    override fun open(path: String) =
        ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8))
}

private class FixedWorkoutPlanClock(
    private val nowEpochMillis: Long
) : com.aml_sakr.fitlife.feature.workout.domain.usecase.WorkoutPlanClock {
    override fun nowEpochMillis(): Long = nowEpochMillis
}

private class RecordingWorkoutPlanDao : WorkoutPlanDao {
    var insertedEntity: WorkoutPlanEntity? = null
    var latestByRequestKey: WorkoutPlanEntity? = null
    var latestByUserId: WorkoutPlanEntity? = null
    var latestById: WorkoutPlanEntity? = null
    var latestRequestKey: String? = null
    var latestRequestNowEpochMillis: Long? = null
    var latestUserId: String? = null
    var latestUserRequestKey: String? = null
    var latestUserNowEpochMillis: Long? = null
    var clearedAt: Long? = null

    override suspend fun insert(entity: WorkoutPlanEntity) {
        insertedEntity = entity
    }

    override suspend fun update(entity: WorkoutPlanEntity) {
        insertedEntity = entity
    }

    override suspend fun getById(id: String): WorkoutPlanEntity? = latestById

    override suspend fun getUnsyncedRecords(): List<WorkoutPlanEntity> = emptyList()

    override suspend fun getLatestByRequestKey(
        requestKey: String,
        nowEpochMillis: Long
    ): WorkoutPlanEntity? {
        latestRequestKey = requestKey
        latestRequestNowEpochMillis = nowEpochMillis
        return latestByRequestKey
    }

    override suspend fun getLatestByUserIdAndRequestKey(
        userId: String,
        requestKey: String,
        nowEpochMillis: Long
    ): WorkoutPlanEntity? {
        latestUserId = userId
        latestUserRequestKey = requestKey
        latestUserNowEpochMillis = nowEpochMillis
        return latestByUserId
    }

    override suspend fun clearOld(nowEpochMillis: Long): Int {
        clearedAt = nowEpochMillis
        return 0
    }
}
