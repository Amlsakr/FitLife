package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class SaveBeginnerProfileUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(
        draft: BeginnerOnboardingDraft,
        userId: String? = null
    ): Result<Unit, OnboardingError> {
        when (val localResult = repository.saveBeginnerDraft(draft)) {
            is Result.Failure -> return localResult
            is Result.Success -> Unit
        }

        val resolvedUserId = userId.orEmpty()
        if (resolvedUserId.isBlank()) {
            return Result.Success(Unit)
        }

        return repository.syncBeginnerProfile(resolvedUserId, draft)
    }
}
