package com.aml_sakr.fitlife.feature.auth.domain.startup

interface OnboardingCompletionReader {
    suspend fun isOnboardingComplete(userId: String): Boolean
}
