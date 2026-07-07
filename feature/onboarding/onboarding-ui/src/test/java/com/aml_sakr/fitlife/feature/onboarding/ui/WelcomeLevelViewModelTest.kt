package com.aml_sakr.fitlife.feature.onboarding.ui

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadSelectedFitnessLevelUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveSelectedFitnessLevelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeLevelViewModelTest {
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
    fun init_loadsPersistedLevelAndKeepsItEditable() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository(selectedLevel = FitnessLevel.Beginner)
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        assertEquals(FitnessLevel.Beginner, viewModel.state.value.selectedLevel)
        assertTrue(viewModel.state.value.isSelectionSaved)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun init_surfacesLoadFailureAsRecoverableError() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository(
            readResult = Result.Failure(OnboardingError.StorageReadFailure)
        )
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.selectedLevel)
        assertEquals(
            OnboardingError.StorageReadFailure.message,
            viewModel.state.value.errorMessage
        )
    }

    @Test
    fun selectLevel_persistsImmediatelyAndContinueNavigatesToBeginnerBranch() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(WelcomeLevelEvent.SelectLevel(FitnessLevel.Beginner))
        advanceUntilIdle()

        assertEquals(FitnessLevel.Beginner, repository.selectedLevel)
        assertTrue(viewModel.state.value.isSelectionSaved)
        viewModel.onEvent(WelcomeLevelEvent.ContinuePressed)
        advanceUntilIdle()
        assertEquals(WelcomeLevelAction.NavigateToBeginner, viewModel.actions.first())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun selectLevel_persistsImmediatelyAndContinueNavigatesToIntermediateBranch() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(WelcomeLevelEvent.SelectLevel(FitnessLevel.Intermediate))
        advanceUntilIdle()

        assertEquals(FitnessLevel.Intermediate, repository.selectedLevel)
        assertTrue(viewModel.state.value.isSelectionSaved)
        viewModel.onEvent(WelcomeLevelEvent.ContinuePressed)
        advanceUntilIdle()
        assertEquals(WelcomeLevelAction.NavigateToIntermediate, viewModel.actions.first())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun selectLevel_surfaceSaveFailureWithoutNavigating() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository(
            saveResult = Result.Failure(OnboardingError.StorageWriteFailure)
        )
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(WelcomeLevelEvent.SelectLevel(FitnessLevel.Beginner))
        advanceUntilIdle()

        assertEquals(
            OnboardingError.StorageWriteFailure.message,
            viewModel.state.value.errorMessage
        )
        assertTrue(viewModel.state.value.selectedLevel == FitnessLevel.Beginner)
        assertFalse(viewModel.state.value.isSelectionSaved)
    }

    private fun buildViewModel(repository: FakeOnboardingRepository): WelcomeLevelViewModel =
        WelcomeLevelViewModel(
            userId = "user-123",
            readSelectedFitnessLevel = ReadSelectedFitnessLevelUseCase(repository),
            saveSelectedFitnessLevel = SaveSelectedFitnessLevelUseCase(repository)
        )

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null,
        private val readResult: Result<FitnessLevel?, OnboardingError>? = null,
        private val saveResult: Result<Unit, OnboardingError>? = null
    ) : OnboardingRepository {
        override suspend fun getSelectedFitnessLevel(userId: String): Result<FitnessLevel?, OnboardingError> =
            readResult ?: Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(
            userId: String,
            level: FitnessLevel
        ): Result<Unit, OnboardingError> {
            selectedLevel = level
            return saveResult ?: Result.Success(Unit)
        }

        override suspend fun getBeginnerDraft(userId: String): Result<BeginnerOnboardingDraft, OnboardingError> =
            Result.Success(BeginnerOnboardingDraft())

        override suspend fun saveBeginnerDraft(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun syncBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun isOnboardingComplete(
            userId: String
        ): Result<Boolean, OnboardingError> = Result.Success(false)

        override suspend fun markOnboardingComplete(
            userId: String
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun getIntermediateDraft(userId: String): Result<IntermediateOnboardingDraft, OnboardingError> =
            Result.Success(IntermediateOnboardingDraft())

        override suspend fun saveIntermediateDraft(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun syncIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)
    }
}
