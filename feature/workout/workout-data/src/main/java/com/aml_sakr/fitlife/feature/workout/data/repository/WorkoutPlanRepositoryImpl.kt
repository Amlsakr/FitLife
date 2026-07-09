package com.aml_sakr.fitlife.feature.workout.data.repository

import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDao
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackSource
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiApiService
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiPlanResponseParser
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutPromptBuilder
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutGeminiGatewayConfiguration
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanFailureClassifier
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanMapper
import com.aml_sakr.fitlife.feature.workout.domain.WorkoutPlanDefaults
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.gemini.FitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiWorkoutProfile
import com.aml_sakr.fitlife.core.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.core.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.core.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.core.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.repository.WorkoutPlanRepository
import com.aml_sakr.fitlife.feature.workout.domain.usecase.SystemWorkoutPlanClock
import com.aml_sakr.fitlife.feature.workout.domain.usecase.WorkoutPlanClock
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketTimeoutException

class WorkoutPlanRepositoryImpl(
    private val workoutPlanDao: WorkoutPlanDao,
    private val geminiConfiguration: WorkoutGeminiGatewayConfiguration,
    private val apiService: GeminiApiService,
    private val promptBuilder: GeminiWorkoutPromptBuilder,
    private val responseParser: GeminiPlanResponseParser,
    private val fallbackSource: WorkoutPlanFallbackSource,
    private val mapper: WorkoutPlanMapper,
    private val failureClassifier: WorkoutPlanFailureClassifier,
    private val gson: Gson,
    private val roomMapper: WorkoutPlanRoomMapper = WorkoutPlanRoomMapper(gson),
    private val clock: WorkoutPlanClock = SystemWorkoutPlanClock
) : WorkoutPlanRepository, com.aml_sakr.fitlife.core.domain.repository.WorkoutPlanRepository {
    override suspend fun getCachedPlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan?, WorkoutGenerationError> {
        return try {
            val entity = workoutPlanDao.getLatestByRequestKey(
                requestKey = request.toRequestKey(),
                nowEpochMillis = clock.nowEpochMillis()
            ) ?: return Result.Success(null)

            val decodedPlan = roomMapper.toDomain(entity)
            Result.Success(decodedPlan?.takeIf { it.isValidCachedPlanFor(request) })
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Result.Failure(WorkoutGenerationError.CacheUnavailable)
        }
    }

    override suspend fun getPlanById(planId: String): Result<WorkoutPlan?, WorkoutGenerationError> {
        return try {
            val entity = workoutPlanDao.getById(planId) ?: return Result.Success(null)
            Result.Success(roomMapper.toDomain(entity))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Result.Failure(WorkoutGenerationError.CacheUnavailable)
        }
    }

    override suspend fun generateRemotePlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        return try {
            withTimeout(geminiConfiguration.timeoutMillis) {
                val attemptCount = geminiConfiguration.maxRetries + 1
                var lastFailure: WorkoutGenerationError? = null

                repeat(attemptCount) { attemptIndex ->
                    val attempt = runRemoteAttempt(request)
                    if (attempt.result is Result.Success) {
                        @Suppress("UNCHECKED_CAST")
                        return@withTimeout attempt.result as Result.Success<WorkoutPlan>
                    }

                    val failure = (attempt.result as Result.Failure).error
                    lastFailure = failure
                    val shouldRetry = attempt.retryable && attemptIndex < geminiConfiguration.maxRetries
                    if (!shouldRetry) {
                        return@withTimeout Result.Failure(failure)
                    }
                    delay(geminiConfiguration.backoffMillis * (attemptIndex + 1))
                }

                Result.Failure(lastFailure ?: WorkoutGenerationError.RemoteHttpError)
            }
        } catch (_: TimeoutCancellationException) {
            Result.Failure(WorkoutGenerationError.RemoteTimeout)
        }
    }

    override suspend fun loadFallbackPlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        return fallbackSource.load(request, generatedAtEpochMillis = clock.nowEpochMillis())
    }

    override suspend fun savePlan(plan: WorkoutPlan): Result<Unit, WorkoutGenerationError> {
        return try {
            workoutPlanDao.clearOld(clock.nowEpochMillis())
            workoutPlanDao.insert(roomMapper.toEntity(plan))
            Result.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Result.Failure(WorkoutGenerationError.PersistenceFailed)
        }
    }

    private suspend fun runRemoteAttempt(
        request: WorkoutGenerationRequest
    ): RemoteAttempt {
        val apiKey = geminiConfiguration.apiKey
        if (apiKey.isBlank() || geminiConfiguration.modelName.isBlank()) {
            return RemoteAttempt(
                result = Result.Failure(WorkoutGenerationError.RemoteNetworkError),
                retryable = false
            )
        }

        val benchmarkConfiguration = geminiConfiguration.toBenchmarkConfiguration()

        val geminiRequest = promptBuilder.buildRequest(
            profile = request.toGeminiProfile(),
            configuration = benchmarkConfiguration
        )

        return try {
            val callResult = apiService.generatePlan(
                request = geminiRequest,
                apiKey = apiKey,
                configuration = benchmarkConfiguration
            )

            when (callResult.httpStatusCode) {
                in 200..299 -> {
                    val parsed = responseParser.parse(callResult.responseBody)
                    if (!parsed.isValidPlan || parsed.plan == null) {
                        RemoteAttempt(
                            result = Result.Failure(WorkoutGenerationError.RemoteParseError),
                            retryable = false
                        )
                    } else {
                        RemoteAttempt(
                            result = Result.Success(
                                mapper.fromGeminiDraft(
                                    request = request,
                                    draft = parsed.plan,
                                    generatedAtEpochMillis = clock.nowEpochMillis(),
                                    isFallback = false
                                )
                            ),
                            retryable = false
                        )
                    }
                }

                429 -> RemoteAttempt(
                    result = Result.Failure(failureClassifier.classifyHttpStatus(429)),
                    retryable = true
                )

                in 500..599 -> RemoteAttempt(
                    result = Result.Failure(failureClassifier.classifyHttpStatus(callResult.httpStatusCode)),
                    retryable = true
                )

                else -> RemoteAttempt(
                    result = Result.Failure(failureClassifier.classifyHttpStatus(callResult.httpStatusCode)),
                    retryable = false
                )
            }
        } catch (_: SocketTimeoutException) {
            RemoteAttempt(
                result = Result.Failure(WorkoutGenerationError.RemoteTimeout),
                retryable = true
            )
        } catch (_: IOException) {
            RemoteAttempt(
                result = Result.Failure(WorkoutGenerationError.RemoteNetworkError),
                retryable = true
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            RemoteAttempt(
                result = Result.Failure(WorkoutGenerationError.RemoteNetworkError),
                retryable = false
            )
        }
    }

    private fun WorkoutGenerationRequest.toGeminiProfile(): GeminiWorkoutProfile = GeminiWorkoutProfile(
        id = userId,
        fitnessLevel = when (fitnessLevel) {
            WorkoutFitnessLevel.Beginner -> FitnessLevel.Beginner
            WorkoutFitnessLevel.Intermediate -> FitnessLevel.Intermediate
        },
        goal = goals.joinToString(separator = ", ") { it.toGoalText() },
        location = when (location) {
            WorkoutLocation.Home -> "Home"
            WorkoutLocation.Gym -> "Gym"
            WorkoutLocation.Outdoor -> "Outdoor"
        },
        availableEquipment = availableEquipment.toList(),
        days = requestedDays
    )

    private fun WorkoutGoal.toGoalText(): String = when (this) {
        WorkoutGoal.GeneralHealth -> "General health"
        WorkoutGoal.WeightLoss -> "Weight loss"
        WorkoutGoal.Strength -> "Strength"
    }

    private fun WorkoutPlan.isValidCachedPlanFor(request: WorkoutGenerationRequest): Boolean {
        return userId == request.userId &&
            fitnessLevel == request.fitnessLevel &&
            goals == request.goals &&
            location == request.location &&
            normalizedEquipmentSet(availableEquipment) == normalizedEquipmentSet(request.availableEquipment) &&
            days.size == WorkoutPlanDefaults.DefaultRequestedDays &&
            days.map { it.day }.sorted() == (1..WorkoutPlanDefaults.DefaultRequestedDays).toList() &&
            days.all { day ->
                day.title.isNotBlank() &&
                    day.durationMinutes > 0 &&
                    day.exercises.isNotEmpty() &&
                    day.exercises.all { exercise ->
                        exercise.name.isNotBlank() &&
                            exercise.sets > 0 &&
                            exercise.reps.isNotBlank() &&
                            exercise.estimatedDurationMinutes > 0
                    }
            } &&
            generatedAtEpochMillis > 0 &&
            expiresAtEpochMillis > generatedAtEpochMillis
    }

    private fun normalizedEquipmentSet(equipment: Set<String>): Set<String> =
        equipment.map { it.trim().lowercase() }.toSet()
}

private data class RemoteAttempt(
    val result: Result<WorkoutPlan, WorkoutGenerationError>,
    val retryable: Boolean
)
