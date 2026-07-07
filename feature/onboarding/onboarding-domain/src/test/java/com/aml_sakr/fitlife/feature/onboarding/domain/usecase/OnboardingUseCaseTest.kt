package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingUseCaseTest {
    @Test
    fun readSelectedFitnessLevel_returnsPersistedValue() = runTest {
        val repository = FakeOnboardingRepository(selectedLevel = FitnessLevel.Beginner)
        val useCase = ReadSelectedFitnessLevelUseCase(repository)

        assertEquals(Result.Success(FitnessLevel.Beginner), useCase("user-123"))
    }

    @Test
    fun saveSelectedFitnessLevel_delegatesToRepository() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveSelectedFitnessLevelUseCase(repository)

        assertEquals(Result.Success(Unit), useCase("user-123", FitnessLevel.Intermediate))
        assertEquals(FitnessLevel.Intermediate, repository.selectedLevel)
    }

    @Test
    fun readOnboardingCompletion_returnsPersistedValue() = runTest {
        val repository = FakeOnboardingRepository(onboardingComplete = true)
        val useCase = ReadOnboardingCompletionUseCase(repository)

        assertEquals(Result.Success(true), useCase("user-123"))
    }

    @Test
    fun markOnboardingComplete_updatesRepositoryState() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = MarkOnboardingCompleteUseCase(repository)

        assertEquals(Result.Success(Unit), useCase("user-123"))
        assertEquals(true, repository.onboardingComplete)
        assertEquals("user-123", repository.completedUserId)
    }

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null,
        var onboardingComplete: Boolean = false,
        var completedUserId: String? = null
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
        ): Result<Boolean, OnboardingError> = Result.Success(onboardingComplete)

        override suspend fun markOnboardingComplete(
            userId: String
        ): Result<Unit, OnboardingError> {
            onboardingComplete = true
            completedUserId = userId
            return Result.Success(Unit)
        }

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
