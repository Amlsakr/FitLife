package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BeginnerOnboardingUseCaseTest {
    @Test
    fun readBeginnerDraft_returnsPersistedDraft() = runTest {
        val draft = BeginnerOnboardingDraft(
            currentStep = BeginnerOnboardingStep.Equipment,
            goals = setOf(FitnessGoal.WeightLoss),
            equipment = setOf(Equipment.None),
            weeklyFrequency = 2
        )
        val repository = FakeOnboardingRepository(beginnerDraft = draft)
        val useCase = ReadBeginnerDraftUseCase(repository)

        assertEquals(Result.Success(draft), useCase())
    }

    @Test
    fun saveBeginnerProfile_persistsLocallyAndSyncsWhenUserIdExists() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveBeginnerProfileUseCase(repository)
        val draft = BeginnerOnboardingDraft(weeklyFrequency = 5)

        assertEquals(Result.Success(Unit), useCase(draft, userId = "user-123"))
        assertEquals(draft, repository.beginnerDraft)
        assertEquals("user-123", repository.syncedUserId)
        assertEquals(draft, repository.syncedDraft)
    }

    @Test
    fun saveBeginnerProfile_skipsSyncWhenUserIdIsBlank() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveBeginnerProfileUseCase(repository)

        assertEquals(Result.Success(Unit), useCase(BeginnerOnboardingDraft()))
        assertEquals(null, repository.syncedUserId)
        assertEquals(BeginnerOnboardingDraft(), repository.beginnerDraft)
    }

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null,
        var beginnerDraft: BeginnerOnboardingDraft = BeginnerOnboardingDraft(),
        var syncedUserId: String? = null,
        var syncedDraft: BeginnerOnboardingDraft? = null
    ) : OnboardingRepository {
        override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> =
            Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> {
            selectedLevel = level
            return Result.Success(Unit)
        }

        override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> =
            Result.Success(beginnerDraft)

        override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> {
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
