package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOneRepMaxInput
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadIntermediateDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveIntermediateProfileUseCase
import androidx.lifecycle.SavedStateHandle
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
class IntermediateOnboardingViewModelTest {
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
    fun init_loadsPersistedIntermediateDraft() = runTest(dispatcher) {
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.Goals,
            currentSplit = IntermediateTrainingSplit.UpperLower,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("80")
            )
        )
        val repository = FakeOnboardingRepository(intermediateDraft = draft)
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        assertEquals(IntermediateOnboardingStep.Goals, viewModel.state.value.currentStep)
        assertEquals(IntermediateTrainingSplit.UpperLower, viewModel.state.value.selectedSplit)
        assertEquals(setOf(FitnessGoal.Strength), viewModel.state.value.selectedGoals)
        assertEquals("80", viewModel.state.value.oneRepMaxInputs.getValue(OneRepMaxLift.BenchPress).valueText)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun init_prefersSavedStateHandleStepOverPersistedDraft() = runTest(dispatcher) {
        val restoredState = IntermediateOnboardingState(
            currentStep = IntermediateOnboardingStep.OneRepMax,
            selectedSplit = IntermediateTrainingSplit.UpperLower,
            selectedGoals = setOf(FitnessGoal.Strength)
        )
        val repositoryDraft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.Split,
            currentSplit = IntermediateTrainingSplit.FullBody,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("100")
            )
        )
        val savedStateHandle = SavedStateHandle(
            mapOf(SAVED_STATE_KEY to restoredState)
        )
        val repository = FakeOnboardingRepository(intermediateDraft = repositoryDraft)
        val viewModel = buildViewModel(repository, savedStateHandle)

        advanceUntilIdle()

        assertEquals(IntermediateOnboardingStep.OneRepMax, viewModel.state.value.currentStep)
        assertEquals(IntermediateTrainingSplit.UpperLower, viewModel.state.value.selectedSplit)
        assertEquals(setOf(FitnessGoal.Strength), viewModel.state.value.selectedGoals)
    }

    @Test
    fun init_fallsBackToSavedStateHandleWhenDraftReadFails() = runTest(dispatcher) {
        val restoredState = IntermediateOnboardingState(
            currentStep = IntermediateOnboardingStep.Goals,
            selectedSplit = IntermediateTrainingSplit.PushPullLegs,
            selectedGoals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("90", OneRepMaxUnit.Pounds)
            )
        )
        val savedStateHandle = SavedStateHandle(
            mapOf(SAVED_STATE_KEY to restoredState)
        )
        val repository = FakeOnboardingRepository(
            intermediateDraftResult = Result.Failure(OnboardingError.StorageReadFailure)
        )
        val viewModel = buildViewModel(repository, savedStateHandle)

        advanceUntilIdle()

        assertEquals(IntermediateOnboardingStep.Goals, viewModel.state.value.currentStep)
        assertEquals(IntermediateTrainingSplit.PushPullLegs, viewModel.state.value.selectedSplit)
        assertEquals(setOf(FitnessGoal.Strength), viewModel.state.value.selectedGoals)
        assertEquals(
            "90",
            viewModel.state.value.oneRepMaxInputs.getValue(OneRepMaxLift.BenchPress).valueText
        )
        assertEquals(
            OneRepMaxUnit.Pounds,
            viewModel.state.value.oneRepMaxInputs.getValue(OneRepMaxLift.BenchPress).unit
        )
        assertEquals(OnboardingError.StorageReadFailure.message, viewModel.state.value.errorMessage)
    }

    @Test
    fun init_restoresInvalidOneRepMaxDraftTextAndUnit() = runTest(dispatcher) {
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.OneRepMax,
            currentSplit = IntermediateTrainingSplit.FullBody,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("-1", OneRepMaxUnit.Pounds)
            )
        )
        val repository = FakeOnboardingRepository(intermediateDraft = draft)
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        assertEquals("-1", viewModel.state.value.oneRepMaxInputs.getValue(OneRepMaxLift.BenchPress).valueText)
        assertEquals(OneRepMaxUnit.Pounds, viewModel.state.value.oneRepMaxInputs.getValue(OneRepMaxLift.BenchPress).unit)
    }

    @Test
    fun updatesPersistInSavedStateHandleImmediately() = runTest(dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository, savedStateHandle)

        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(IntermediateTrainingSplit.FullBody))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.GoalToggled(FitnessGoal.Strength))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.OneRepMaxValueChanged(OneRepMaxLift.BenchPress, "-1"))
        advanceUntilIdle()

        val restored = savedStateHandle.get<IntermediateOnboardingState>(SAVED_STATE_KEY)
        assertEquals(IntermediateTrainingSplit.FullBody, restored?.selectedSplit)
        assertEquals(setOf(FitnessGoal.Strength), restored?.selectedGoals)
        assertEquals("-1", restored?.oneRepMaxInputs?.getValue(OneRepMaxLift.BenchPress)?.valueText)
    }

    @Test
    fun invalidOneRepMaxBlocksContinue() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(IntermediateTrainingSplit.FullBody))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.GoalToggled(FitnessGoal.Strength))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.OneRepMaxValueChanged(OneRepMaxLift.BenchPress, "-1"))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        assertEquals(IntermediateOnboardingStep.OneRepMax, viewModel.state.value.currentStep)
        assertEquals(
            "Bench Press must be greater than 0.",
            viewModel.state.value.oneRepMaxValidationError()
        )
        assertEquals(null, viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.canContinue.not())
    }

    @Test
    fun splitSelectionSaveFailureShowsRecoverableError() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository(
            saveIntermediateDraftResult = Result.Failure(OnboardingError.StorageWriteFailure)
        )
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(IntermediateTrainingSplit.FullBody))
        advanceUntilIdle()

        assertEquals(
            OnboardingError.StorageWriteFailure.message,
            viewModel.state.value.errorMessage
        )
        assertEquals(IntermediateTrainingSplit.FullBody, viewModel.state.value.selectedSplit)
        assertEquals(IntermediateOnboardingStep.Split, viewModel.state.value.currentStep)
    }

    @Test
    fun completeFlow_emitsFinishActionAndNormalizesOneRepMax() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository()
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(IntermediateTrainingSplit.PushPullLegs))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.GoalToggled(FitnessGoal.Strength))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.OneRepMaxUnitChanged(OneRepMaxLift.BenchPress, OneRepMaxUnit.Pounds))
        viewModel.onEvent(IntermediateOnboardingEvent.OneRepMaxValueChanged(OneRepMaxLift.BenchPress, "100"))
        advanceUntilIdle()

        val action = async { viewModel.actions.first() }
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        assertEquals(IntermediateOnboardingAction.Finish, action.await())
        assertEquals("user-123", repository.syncedIntermediateUserId)
        assertEquals(IntermediateTrainingSplit.PushPullLegs, repository.syncedIntermediateDraft?.currentSplit)
        assertEquals(
            45.359237f,
            repository.syncedIntermediateDraft?.oneRepMax?.get(OneRepMaxLift.BenchPress) ?: 0f,
            0.0001f
        )
    }

    @Test
    fun completeFlow_showsErrorWhenCompletionPersistenceFails() = runTest(dispatcher) {
        val repository = FakeOnboardingRepository().apply {
            markCompletionResult = Result.Failure(OnboardingError.StorageWriteFailure)
        }
        val viewModel = buildViewModel(repository)

        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(IntermediateTrainingSplit.PushPullLegs))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.GoalToggled(FitnessGoal.Strength))
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()
        viewModel.onEvent(IntermediateOnboardingEvent.OneRepMaxValueChanged(OneRepMaxLift.BenchPress, "100"))
        advanceUntilIdle()

        viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed)
        advanceUntilIdle()

        assertEquals(
            OnboardingError.StorageWriteFailure.message,
            viewModel.state.value.errorMessage
        )
        assertEquals(IntermediateOnboardingStep.OneRepMax, viewModel.state.value.currentStep)
        assertFalse(repository.onboardingComplete)
    }

    private fun buildViewModel(
        repository: FakeOnboardingRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): IntermediateOnboardingViewModel =
        IntermediateOnboardingViewModel(
            userId = "user-123",
            readIntermediateDraftUseCase = ReadIntermediateDraftUseCase(repository),
            saveIntermediateProfileUseCase = SaveIntermediateProfileUseCase(repository),
            markOnboardingCompleteUseCase = MarkOnboardingCompleteUseCase(repository),
            savedStateHandle = savedStateHandle
        )

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null,
        var beginnerDraft: BeginnerOnboardingDraft = BeginnerOnboardingDraft(),
        var intermediateDraft: IntermediateOnboardingDraft = IntermediateOnboardingDraft(),
        var intermediateDraftResult: Result<IntermediateOnboardingDraft, OnboardingError> = Result.Success(
            intermediateDraft
        ),
        var syncedIntermediateUserId: String? = null,
        var syncedIntermediateDraft: IntermediateOnboardingDraft? = null,
        var saveIntermediateDraftResult: Result<Unit, OnboardingError> = Result.Success(Unit),
        var syncIntermediateProfileResult: Result<Unit, OnboardingError> = Result.Success(Unit),
        var onboardingComplete: Boolean = false,
        var completedUserId: String? = null,
        var markCompletionResult: Result<Unit, OnboardingError> = Result.Success(Unit)
    ) : OnboardingRepository {
        override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> =
            Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> {
            selectedLevel = level
            return Result.Success(Unit)
        }

        override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> =
            Result.Success(beginnerDraft)

        override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> =
            Result.Success(Unit)

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
            intermediateDraftResult

        override suspend fun saveIntermediateDraft(
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            intermediateDraft = draft
            return saveIntermediateDraftResult
        }

        override suspend fun syncIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            syncedIntermediateUserId = userId
            syncedIntermediateDraft = draft
            return syncIntermediateProfileResult
        }
    }

    private companion object {
        const val SAVED_STATE_KEY = "intermediate_onboarding_state"
    }
}
