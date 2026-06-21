package com.aml_sakr.fitlife.feature.workout.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateWorkoutPlanUseCaseTest {
    @Test
    fun `returns cached plan when it is still fresh`() = runTest {
        val request = sampleRequest()
        val cachedPlan = samplePlan(
            generatedAtEpochMillis = 1_000L,
            expiresAtEpochMillis = 10_000L,
            isFallback = false
        )
        val repository = FakeWorkoutPlanRepository(
            cachedPlan = cachedPlan
        )
        val useCase = GenerateWorkoutPlanUseCase(
            repository = repository,
            clock = FixedWorkoutPlanClock(5_000L)
        )

        val result = useCase(request)

        assertEquals(Result.Success(cachedPlan), result)
        assertFalse(repository.remoteCalled)
        assertFalse(repository.fallbackCalled)
    }

    @Test
    fun `requests remote plan when cache is stale and persists remote success`() = runTest {
        val request = sampleRequest()
        val remotePlan = samplePlan(
            generatedAtEpochMillis = 20_000L,
            expiresAtEpochMillis = 30_000L,
            isFallback = false
        )
        val repository = FakeWorkoutPlanRepository(
            cachedPlan = samplePlan(
                generatedAtEpochMillis = 1_000L,
                expiresAtEpochMillis = 2_000L,
                isFallback = false
            ),
            remotePlan = remotePlan
        )
        val useCase = GenerateWorkoutPlanUseCase(
            repository = repository,
            clock = FixedWorkoutPlanClock(10_000L)
        )

        val result = useCase(request)

        assertEquals(Result.Success(remotePlan), result)
        assertTrue(repository.remoteCalled)
        assertTrue(repository.savedPlans.contains(remotePlan))
        assertFalse(repository.fallbackCalled)
    }

    @Test
    fun `returns remote success even when persistence fails`() = runTest {
        val request = sampleRequest()
        val remotePlan = samplePlan(
            generatedAtEpochMillis = 20_000L,
            expiresAtEpochMillis = 30_000L,
            isFallback = false
        )
        val repository = FakeWorkoutPlanRepository(
            remotePlan = remotePlan,
            saveFailure = WorkoutGenerationError.PersistenceFailed
        )
        val useCase = GenerateWorkoutPlanUseCase(
            repository = repository,
            clock = FixedWorkoutPlanClock(10_000L)
        )

        val result = useCase(request)

        assertEquals(Result.Failure(WorkoutGenerationError.PersistenceFailed), result)
        assertTrue(repository.remoteCalled)
        assertTrue(repository.saveCalled)
        assertFalse(repository.fallbackCalled)
    }

    @Test
    fun `falls back when remote generation fails`() = runTest {
        val request = sampleRequest()
        val fallbackPlan = samplePlan(
            generatedAtEpochMillis = 40_000L,
            expiresAtEpochMillis = 50_000L,
            isFallback = true
        )
        val repository = FakeWorkoutPlanRepository(
            remoteFailure = WorkoutGenerationError.RemoteTimeout,
            fallbackPlan = fallbackPlan
        )
        val useCase = GenerateWorkoutPlanUseCase(
            repository = repository,
            clock = FixedWorkoutPlanClock(10_000L)
        )

        val result = useCase(request)

        assertEquals(Result.Success(fallbackPlan), result)
        assertTrue(repository.remoteCalled)
        assertTrue(repository.fallbackCalled)
        assertTrue(repository.savedPlans.contains(fallbackPlan))
    }

    @Test
    fun `falls back for rate limit http and parse remote failures`() = runTest {
        val request = sampleRequest()
        val fallbackPlan = samplePlan(
            generatedAtEpochMillis = 40_000L,
            expiresAtEpochMillis = 50_000L,
            isFallback = true
        )

        listOf(
            WorkoutGenerationError.RemoteRateLimited,
            WorkoutGenerationError.RemoteHttpError,
            WorkoutGenerationError.RemoteParseError
        ).forEach { remoteFailure ->
            val repository = FakeWorkoutPlanRepository(
                remoteFailure = remoteFailure,
                fallbackPlan = fallbackPlan
            )
            val useCase = GenerateWorkoutPlanUseCase(
                repository = repository,
                clock = FixedWorkoutPlanClock(10_000L)
            )

            val result = useCase(request)

            assertEquals(Result.Success(fallbackPlan), result)
            assertTrue(repository.remoteCalled)
            assertTrue(repository.fallbackCalled)
        }
    }

    @Test
    fun `returns fallback failure when no fallback template matches`() = runTest {
        val request = sampleRequest()
        val repository = FakeWorkoutPlanRepository(
            remoteFailure = WorkoutGenerationError.RemoteHttpError,
            fallbackFailure = WorkoutGenerationError.NoMatchingFallbackTemplate
        )
        val useCase = GenerateWorkoutPlanUseCase(
            repository = repository,
            clock = FixedWorkoutPlanClock(10_000L)
        )

        val result = useCase(request)

        assertEquals(Result.Failure(WorkoutGenerationError.NoMatchingFallbackTemplate), result)
        assertTrue(repository.remoteCalled)
        assertTrue(repository.fallbackCalled)
    }

    @Test
    fun `rejects workout generation requests that are not seven days`() {
        assertThrows(IllegalArgumentException::class.java) {
            sampleRequest().copy(requestedDays = 6)
        }
    }

    private fun sampleRequest() = WorkoutGenerationRequest(
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
        days = listOf(
            WorkoutDay(
                day = 1,
                title = "Day 1",
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
        ),
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = expiresAtEpochMillis,
        isFallback = isFallback
    )
}

private class FakeWorkoutPlanRepository(
    private val cachedPlan: WorkoutPlan? = null,
    private val remotePlan: WorkoutPlan? = null,
    private val remoteFailure: WorkoutGenerationError? = null,
    private val fallbackPlan: WorkoutPlan? = null,
    private val fallbackFailure: WorkoutGenerationError? = null,
    private val saveFailure: WorkoutGenerationError? = null
) : WorkoutPlanRepository {
    var remoteCalled = false
    var fallbackCalled = false
    var saveCalled = false
    val savedPlans = mutableListOf<WorkoutPlan>()

    override suspend fun getCachedPlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan?, WorkoutGenerationError> = Result.Success(cachedPlan)

    override suspend fun generateRemotePlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        remoteCalled = true
        return remotePlan?.let { Result.Success(it) } ?: Result.Failure(remoteFailure!!)
    }

    override suspend fun loadFallbackPlan(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        fallbackCalled = true
        return fallbackPlan?.let { Result.Success(it) } ?: Result.Failure(fallbackFailure!!)
    }

    override suspend fun savePlan(plan: WorkoutPlan): Result<Unit, WorkoutGenerationError> {
        saveCalled = true
        return saveFailure?.let { Result.Failure(it) } ?: run {
            savedPlans += plan
            Result.Success(Unit)
        }
    }
}

private class FixedWorkoutPlanClock(
    private val nowEpochMillis: Long
) : WorkoutPlanClock {
    override fun nowEpochMillis(): Long = nowEpochMillis
}
