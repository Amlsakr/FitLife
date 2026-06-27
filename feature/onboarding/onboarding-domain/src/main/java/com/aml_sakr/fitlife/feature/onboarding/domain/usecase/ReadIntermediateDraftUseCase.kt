package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class ReadIntermediateDraftUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(userId: String): Result<IntermediateOnboardingDraft, OnboardingError> =
        repository.getIntermediateDraft(userId)
}
