package com.aml_sakr.fitlife.feature.auth.domain.startup

import kotlinx.coroutines.CancellationException

class DetermineStartupDestinationUseCase(
    private val authSessionReader: AuthSessionReader,
    private val onboardingCompletionReader: OnboardingCompletionReader
) {
    suspend operator fun invoke(): StartupDestination {
        val session = authSessionReader.currentSession() ?: return StartupDestination.Auth
        if (session.userId.isBlank()) return StartupDestination.Auth
        if (!session.isEmailVerified) return StartupDestination.Auth

        return try {
            if (onboardingCompletionReader.isOnboardingComplete(session.userId)) {
                StartupDestination.Home
            } else {
                StartupDestination.Onboarding
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            StartupDestination.Onboarding
        }
    }
}
