package com.aml_sakr.fitlife.feature.auth.domain.startup

class DetermineStartupDestinationUseCase(
    private val authSessionReader: AuthSessionReader,
    private val onboardingCompletionReader: OnboardingCompletionReader
) {
    suspend operator fun invoke(): StartupDestination {
        val session = authSessionReader.currentSession() ?: return StartupDestination.Auth

        return if (onboardingCompletionReader.isOnboardingComplete(session.userId)) {
            StartupDestination.Home
        } else {
            StartupDestination.Onboarding
        }
    }
}
