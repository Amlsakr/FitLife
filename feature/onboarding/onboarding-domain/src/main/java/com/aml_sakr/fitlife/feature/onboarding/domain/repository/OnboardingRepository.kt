package com.aml_sakr.fitlife.feature.onboarding.domain.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

interface OnboardingRepository {
    suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError>

    suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError>

    suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError>

    suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError>

    suspend fun syncBeginnerProfile(userId: String, draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError>

    suspend fun isOnboardingComplete(userId: String): Result<Boolean, OnboardingError>

    suspend fun markOnboardingComplete(userId: String): Result<Unit, OnboardingError>

    suspend fun getIntermediateDraft(): Result<IntermediateOnboardingDraft, OnboardingError>

    suspend fun saveIntermediateDraft(
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError>

    suspend fun syncIntermediateProfile(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError>
}
