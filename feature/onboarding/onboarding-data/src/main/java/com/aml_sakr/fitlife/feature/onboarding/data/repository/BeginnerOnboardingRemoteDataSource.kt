package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft

interface BeginnerOnboardingRemoteDataSource {
    suspend fun upsertBeginnerProfile(userId: String, draft: BeginnerOnboardingDraft)
}
