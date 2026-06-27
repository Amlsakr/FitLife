package com.aml_sakr.fitlife.feature.onboarding.domain.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

interface OnboardingRepository {
    suspend fun getSelectedFitnessLevel(userId: String): Result<FitnessLevel?, OnboardingError>

    suspend fun saveSelectedFitnessLevel(userId: String, level: FitnessLevel): Result<Unit, OnboardingError>

    suspend fun getBeginnerDraft(userId: String): Result<BeginnerOnboardingDraft, OnboardingError>

    suspend fun saveBeginnerDraft(userId: String, draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError>

    suspend fun syncBeginnerProfile(userId: String, draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError>

    suspend fun isOnboardingComplete(userId: String): Result<Boolean, OnboardingError>

    suspend fun markOnboardingComplete(userId: String): Result<Unit, OnboardingError>

    suspend fun getIntermediateDraft(userId: String): Result<IntermediateOnboardingDraft, OnboardingError>

    suspend fun saveIntermediateDraft(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError>

    suspend fun syncIntermediateProfile(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError>
}
