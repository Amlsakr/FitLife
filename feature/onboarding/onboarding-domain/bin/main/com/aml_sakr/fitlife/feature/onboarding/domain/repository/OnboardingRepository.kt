package com.aml_sakr.fitlife.feature.onboarding.domain.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

interface OnboardingRepository {
    suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError>

    suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError>
}
