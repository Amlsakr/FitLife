package com.aml_sakr.fitlife.feature.workout.ui

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.repository.WorkoutPlanRepository
import com.aml_sakr.fitlife.feature.workout.domain.usecase.GenerateWorkoutPlanUseCase
import com.aml_sakr.fitlife.feature.workout.domain.usecase.WorkoutPlanClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutHomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestPlan_fromEmptyShowsLoadingThenSuccess() = runTest(dispatcher) {
        val remoteResult = CompletableDeferred<Result<WorkoutPlan, WorkoutGenerationError>>()
        val repository = FakeWorkoutHomeRepository(remoteResult = remoteResult)
        val viewModel = buildViewModel(repository)

        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        runCurrent()

        assertEquals(WorkoutHomeState.Loading, viewModel.state.value)

        remoteResult.complete(Result.Success(WorkoutHomeTestFixtures.samplePlan()))
        advanceUntilIdle()

        assertEquals(WorkoutHomeState.Success(WorkoutHomeTestFixtures.samplePlan()), viewModel.state.value)
        assertEquals(1, repository.cacheRequests)
        assertEquals(1, repository.remoteRequests)
    }

    @Test
    fun requestPlan_ignoresRepeatTapsWhileLoading() = runTest(dispatcher) {
        val remoteResult = CompletableDeferred<Result<WorkoutPlan, WorkoutGenerationError>>()
        val repository = FakeWorkoutHomeRepository(remoteResult = remoteResult)
        val viewModel = buildViewModel(repository)

        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        runCurrent()
        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        runCurrent()

        assertEquals(WorkoutHomeState.Loading, viewModel.state.value)
        assertEquals(1, repository.cacheRequests)
        assertEquals(1, repository.remoteRequests)

        remoteResult.complete(Result.Success(WorkoutHomeTestFixtures.samplePlan()))
        advanceUntilIdle()

        assertEquals(WorkoutHomeState.Success(WorkoutHomeTestFixtures.samplePlan()), viewModel.state.value)
    }

    @Test
    fun requestPlan_fromSuccessPreservesVisiblePlanWhileRefreshing() = runTest(dispatcher) {
        val existingPlan = WorkoutHomeTestFixtures.samplePlan(dayTitlePrefix = "Current")
        val refreshedPlan = WorkoutHomeTestFixtures.samplePlan(dayTitlePrefix = "Fresh")
        val remoteResult = CompletableDeferred<Result<WorkoutPlan, WorkoutGenerationError>>()
        val repository = FakeWorkoutHomeRepository(remoteResult = remoteResult)
        val viewModel = buildViewModel(
            repository = repository,
            initialState = WorkoutHomeState.Success(existingPlan)
        )

        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        runCurrent()

        assertEquals(WorkoutHomeState.Success(existingPlan, isRefreshing = true), viewModel.state.value)

        remoteResult.complete(Result.Success(refreshedPlan))
        advanceUntilIdle()

        assertEquals(WorkoutHomeState.Success(refreshedPlan), viewModel.state.value)
        assertEquals(1, repository.cacheRequests)
        assertEquals(1, repository.remoteRequests)
    }

    @Test
    fun requestPlan_mapsDomainFailureToSafeRetryableError() = runTest(dispatcher) {
        val repository = FakeWorkoutHomeRepository(
            remoteResult = CompletableDeferred(Result.Failure(WorkoutGenerationError.RemoteHttpError)),
            fallbackResult = Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)
        )
        val viewModel = buildViewModel(repository)

        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        advanceUntilIdle()

        val state = viewModel.state.value as WorkoutHomeState.Error
        assertEquals(WorkoutHomeCopy.GenericErrorMessage, state.message)
        assertEquals("Try again", state.primaryCtaLabel)
        assertFalse(state.message.contains("HTTP", ignoreCase = true))
    }

    @Test
    fun requestPlan_rejectsPlansThatViolateSevenDayInvariant() = runTest(dispatcher) {
        val repository = FakeWorkoutHomeRepository(
            remoteResult = CompletableDeferred(
                Result.Success(WorkoutHomeTestFixtures.samplePlan(dayCount = 6))
            )
        )
        val viewModel = buildViewModel(repository)

        viewModel.onEvent(WorkoutHomeEvent.RequestPlan)
        advanceUntilIdle()

        assertEquals(
            WorkoutHomeState.Error(WorkoutHomeCopy.InvalidPlanMessage),
            viewModel.state.value
        )
    }

    private fun buildViewModel(
        repository: FakeWorkoutHomeRepository,
        initialState: WorkoutHomeState = WorkoutHomeState.Empty
    ): WorkoutHomeViewModel =
        WorkoutHomeViewModel(
            generateWorkoutPlan = GenerateWorkoutPlanUseCase(
                repository = repository,
                clock = FixedWorkoutPlanClock(nowEpochMillis = 1_000L)
            ),
            request = WorkoutHomeTestFixtures.sampleRequest(),
            initialState = initialState
        )

    private class FakeWorkoutHomeRepository(
        private val remoteResult: CompletableDeferred<Result<WorkoutPlan, WorkoutGenerationError>>,
        private val fallbackResult: Result<WorkoutPlan, WorkoutGenerationError> =
            Result.Failure(WorkoutGenerationError.NoMatchingFallbackTemplate)
    ) : WorkoutPlanRepository {
        var cacheRequests = 0
            private set
        var remoteRequests = 0
            private set

        override suspend fun getCachedPlan(
            request: WorkoutGenerationRequest
        ): Result<WorkoutPlan?, WorkoutGenerationError> {
            cacheRequests += 1
            return Result.Success(null)
        }

        override suspend fun generateRemotePlan(
            request: WorkoutGenerationRequest
        ): Result<WorkoutPlan, WorkoutGenerationError> {
            remoteRequests += 1
            return remoteResult.await()
        }

        override suspend fun loadFallbackPlan(
            request: WorkoutGenerationRequest
        ): Result<WorkoutPlan, WorkoutGenerationError> = fallbackResult

        override suspend fun savePlan(plan: WorkoutPlan): Result<Unit, WorkoutGenerationError> =
            Result.Success(Unit)
    }

    private class FixedWorkoutPlanClock(
        private val nowEpochMillis: Long
    ) : WorkoutPlanClock {
        override fun nowEpochMillis(): Long = nowEpochMillis
    }
}
