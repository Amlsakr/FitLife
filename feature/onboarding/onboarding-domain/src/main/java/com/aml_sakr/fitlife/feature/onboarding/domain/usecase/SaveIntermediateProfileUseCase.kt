package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class SaveIntermediateProfileUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(
        draft: IntermediateOnboardingDraft,
        userId: String
    ): Result<Unit, OnboardingError> {
        when (val localResult = repository.saveIntermediateDraft(userId, draft)) {
            is Result.Failure -> return localResult
            is Result.Success -> Unit
        }

        if (userId.isBlank()) {
            return Result.Success(Unit)
        }

        return repository.syncIntermediateProfile(userId, draft)
    }
}
