package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft

interface IntermediateOnboardingRemoteDataSource {
    suspend fun upsertIntermediateProfile(userId: String, draft: IntermediateOnboardingDraft)
}
