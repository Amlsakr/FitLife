package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOneRepMaxInput
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IntermediateOnboardingUseCaseTest {
    @Test
    fun readIntermediateDraft_returnsPersistedDraft() = runTest {
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.Goals,
            currentSplit = IntermediateTrainingSplit.UpperLower,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("85")
            )
        )
        val repository = FakeOnboardingRepository(intermediateDraft = draft)
        val useCase = ReadIntermediateDraftUseCase(repository)

        assertEquals(Result.Success(draft), useCase("user-123"))
    }

    @Test
    fun saveIntermediateProfile_persistsLocallyAndSyncsWhenUserIdExists() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveIntermediateProfileUseCase(repository)
        val draft = IntermediateOnboardingDraft(
            currentSplit = IntermediateTrainingSplit.PushPullLegs,
            goals = setOf(FitnessGoal.WeightLoss)
        )

        assertEquals(Result.Success(Unit), useCase(draft, userId = "user-123"))
        assertEquals(draft, repository.intermediateDraft)
        assertEquals("user-123", repository.syncedIntermediateUserId)
        assertEquals(draft, repository.syncedIntermediateDraft)
    }

    @Test
    fun saveIntermediateProfile_skipsSyncWhenUserIdIsBlank() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveIntermediateProfileUseCase(repository)

        assertEquals(Result.Success(Unit), useCase(IntermediateOnboardingDraft(), userId = ""))
        assertEquals(null, repository.syncedIntermediateUserId)
        assertEquals(IntermediateOnboardingDraft(), repository.intermediateDraft)
    }

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null,
        var beginnerDraft: BeginnerOnboardingDraft = BeginnerOnboardingDraft(),
        var intermediateDraft: IntermediateOnboardingDraft = IntermediateOnboardingDraft(),
        var syncedUserId: String? = null,
        var syncedDraft: BeginnerOnboardingDraft? = null,
        var syncedIntermediateUserId: String? = null,
        var syncedIntermediateDraft: IntermediateOnboardingDraft? = null
    ) : OnboardingRepository {
        override suspend fun getSelectedFitnessLevel(userId: String): Result<FitnessLevel?, OnboardingError> =
            Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(
            userId: String,
            level: FitnessLevel
        ): Result<Unit, OnboardingError> {
            selectedLevel = level
            return Result.Success(Unit)
        }

        override suspend fun getBeginnerDraft(userId: String): Result<BeginnerOnboardingDraft, OnboardingError> =
            Result.Success(beginnerDraft)

        override suspend fun saveBeginnerDraft(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> {
            beginnerDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun syncBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> {
            syncedUserId = userId
            syncedDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun isOnboardingComplete(
            userId: String
        ): Result<Boolean, OnboardingError> = Result.Success(false)

        override suspend fun markOnboardingComplete(
            userId: String
        ): Result<Unit, OnboardingError> = Result.Success(Unit)

        override suspend fun getIntermediateDraft(userId: String): Result<IntermediateOnboardingDraft, OnboardingError> =
            Result.Success(intermediateDraft)

        override suspend fun saveIntermediateDraft(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            intermediateDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun syncIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            syncedIntermediateUserId = userId
            syncedIntermediateDraft = draft
            return Result.Success(Unit)
        }
    }
}
