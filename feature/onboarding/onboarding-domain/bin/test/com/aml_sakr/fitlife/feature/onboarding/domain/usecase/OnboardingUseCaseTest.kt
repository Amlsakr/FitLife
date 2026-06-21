package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingUseCaseTest {
    @Test
    fun readSelectedFitnessLevel_returnsPersistedValue() = runTest {
        val repository = FakeOnboardingRepository(selectedLevel = FitnessLevel.Beginner)
        val useCase = ReadSelectedFitnessLevelUseCase(repository)

        assertEquals(Result.Success(FitnessLevel.Beginner), useCase())
    }

    @Test
    fun saveSelectedFitnessLevel_delegatesToRepository() = runTest {
        val repository = FakeOnboardingRepository()
        val useCase = SaveSelectedFitnessLevelUseCase(repository)

        assertEquals(Result.Success(Unit), useCase(FitnessLevel.Intermediate))
        assertEquals(FitnessLevel.Intermediate, repository.selectedLevel)
    }

    private class FakeOnboardingRepository(
        var selectedLevel: FitnessLevel? = null
    ) : OnboardingRepository {
        override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> =
            Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> {
            selectedLevel = level
            return Result.Success(Unit)
        }
    }
}
