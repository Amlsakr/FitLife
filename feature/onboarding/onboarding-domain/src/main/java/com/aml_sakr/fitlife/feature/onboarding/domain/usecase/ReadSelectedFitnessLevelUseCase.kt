package com.aml_sakr.fitlife.feature.onboarding.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class ReadSelectedFitnessLevelUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(userId: String): Result<FitnessLevel?, OnboardingError> =
        repository.getSelectedFitnessLevel(userId)
}
