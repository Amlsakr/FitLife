package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadBeginnerDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveBeginnerProfileUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
class BeginnerOnboardingViewModelTest {
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
    fun init_blocksInputUntilDraftLoads() = runTest(dispatcher) {
        val readResult = CompletableDeferred<Result<BeginnerOnboardingDraft, OnboardingError>>()
        val repository = FakeOnboardingRepository(readResult = readResult)
        val viewModel = buildViewModel(repository)

        assertTrue(viewModel.state.value.isLoading)

        viewModel.onEvent(BeginnerOnboardingEvent.GoalToggled(FitnessGoal.Strength))
        assertTrue(viewModel.state.value.selectedGoals.isEmpty())

        val draft = BeginnerOnboardingDraft(
            currentStep = BeginnerOnboardingStep.Goals,
            goals = setOf(FitnessGoal.GeneralHealth)
        )
        readResult.complete(Result.Success(draft))

        advanceUntilIdle()

        assertEquals(draft.goals, viewModel.state.value.selectedGoals)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun autosavesAreSerializedAndLatestDraftWins() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository(blockFirstSave = true)
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        viewModel.onEvent(BeginnerOnboardingEvent.GoalToggled(FitnessGoal.WeightLoss))
        advanceUntilIdle()
        assertEquals(1, repository.savedDrafts.size)

        viewModel.onEvent(BeginnerOnboardingEvent.EquipmentToggled(Equipment.Dumbbells))
        advanceUntilIdle()
        assertEquals(1, repository.savedDrafts.size)

        repository.releaseFirstSave.complete(Unit)
        advanceUntilIdle()

        assertEquals(2, repository.savedDrafts.size)
        assertEquals(
            BeginnerOnboardingDraft(
                currentStep = BeginnerOnboardingStep.Goals,
                goals = setOf(FitnessGoal.WeightLoss)
            ),
            repository.savedDrafts.first()
        )
        assertEquals(
            BeginnerOnboardingDraft(
                currentStep = BeginnerOnboardingStep.Goals,
                goals = setOf(FitnessGoal.WeightLoss),
                equipment = setOf(Equipment.Dumbbells)
            ),
            repository.savedDrafts.last()
        )
    }

    @Test
    fun invalidFrequencyEventDoesNotEnableContinue() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        viewModel.onEvent(BeginnerOnboardingEvent.FrequencySelected(99))

        assertTrue(viewModel.state.value.weeklyFrequency == null)
        assertTrue(viewModel.state.value.errorMessage != null)
        assertFalse(viewModel.state.value.canContinue)
    }

    @Test
    fun completeFlow_marksCompletionBeforeEmittingFinishAction() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.GoalToggled(FitnessGoal.WeightLoss))
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.EquipmentToggled(Equipment.Dumbbells))
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.FrequencySelected(3))
        advanceUntilIdle()

        val action = async { viewModel.actions.first() }
        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        assertEquals(BeginnerOnboardingAction.Finish, action.await())
        assertEquals("user-123", repository.completedUserId)
        assertTrue(repository.onboardingComplete)
    }

    @Test
    fun completeFlow_showsErrorWhenCompletionPersistenceFails() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository().apply {
            markCompletionResult = Result.Failure(OnboardingError.StorageWriteFailure)
        }
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.GoalToggled(FitnessGoal.WeightLoss))
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.EquipmentToggled(Equipment.Dumbbells))
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(BeginnerOnboardingEvent.FrequencySelected(3))
        advanceUntilIdle()

        viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        assertEquals(
            OnboardingError.StorageWriteFailure.message,
            viewModel.state.value.errorMessage
        )
        assertEquals(BeginnerOnboardingStep.Frequency, viewModel.state.value.currentStep)
        assertFalse(repository.onboardingComplete)
    }

    private fun buildViewModel(repository: FakeOnboardingRepository): BeginnerOnboardingViewModel =
        BeginnerOnboardingViewModel(
            userId = "user-123",
            readBeginnerDraftUseCase = ReadBeginnerDraftUseCase(repository),
            saveBeginnerProfileUseCase = SaveBeginnerProfileUseCase(repository),
            markOnboardingCompleteUseCase = MarkOnboardingCompleteUseCase(repository)
        )

    private class FakeOnboardingRepository(
        private val readResult: CompletableDeferred<Result<BeginnerOnboardingDraft, OnboardingError>>? = null,
        private val blockFirstSave: Boolean = false
    ) : OnboardingRepository {
        val savedDrafts = mutableListOf<BeginnerOnboardingDraft>()
        val releaseFirstSave = CompletableDeferred<Unit>()
        private val firstSaveStarted = CompletableDeferred<Unit>()
        var onboardingComplete = false
        var completedUserId: String? = null
        var markCompletionResult: Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun getSelectedFitnessLevel() = Result.Success(null)

        override suspend fun saveSelectedFitnessLevel(level: com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel) =
            Result.Success(Unit)

        override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> =
            readResult?.await() ?: Result.Success(BeginnerOnboardingDraft())

        override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> {
            savedDrafts += draft
            if (blockFirstSave && savedDrafts.size == 1) {
                firstSaveStarted.complete(Unit)
                releaseFirstSave.await()
            }
            return Result.Success(Unit)
        }

        override suspend fun syncBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun isOnboardingComplete(
            userId: String
        ): Result<Boolean, OnboardingError> = Result.Success(onboardingComplete)

        override suspend fun markOnboardingComplete(
            userId: String
        ): Result<Unit, OnboardingError> {
            completedUserId = userId
            return when (markCompletionResult) {
                is Result.Success -> {
                    onboardingComplete = true
                    Result.Success(Unit)
                }
                is Result.Failure -> markCompletionResult
            }
        }

        override suspend fun getIntermediateDraft(): Result<IntermediateOnboardingDraft, OnboardingError> =
            Result.Success(IntermediateOnboardingDraft())

        override suspend fun saveIntermediateDraft(
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun syncIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> = Result.Success(Unit)
    }
}
