package com.aml_sakr.fitlife.feature.auth.domain.startup

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DetermineStartupDestinationUseCaseTest {
    @Test
    fun invoke_returnsAuth_whenUserIsNotAuthenticated() = runTest {
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(session = null),
            onboardingCompletionReader = FakeOnboardingCompletionReader(isComplete = false)
        )

        assertEquals(StartupDestination.Auth, useCase())
    }

    @Test
    fun invoke_returnsAuth_whenAuthenticatedSessionHasBlankUserId() = runTest {
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(
                session = AuthSession(userId = "  ")
            ),
            onboardingCompletionReader = FakeOnboardingCompletionReader(isComplete = true)
        )

        assertEquals(StartupDestination.Auth, useCase())
    }

    @Test
    fun invoke_returnsOnboarding_whenUserIsAuthenticatedAndOnboardingIsIncomplete() = runTest {
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(
                session = AuthSession(userId = "user-1")
            ),
            onboardingCompletionReader = FakeOnboardingCompletionReader(isComplete = false)
        )

        assertEquals(StartupDestination.Onboarding, useCase())
    }

    @Test
    fun invoke_returnsHome_whenUserIsAuthenticatedAndOnboardingIsComplete() = runTest {
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(
                session = AuthSession(userId = "user-1")
            ),
            onboardingCompletionReader = FakeOnboardingCompletionReader(isComplete = true)
        )

        assertEquals(StartupDestination.Home, useCase())
    }

    @Test
    fun invoke_returnsOnboarding_whenCompletionCheckFails() = runTest {
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(
                session = AuthSession(userId = "user-1")
            ),
            onboardingCompletionReader = FakeOnboardingCompletionReader(
                isComplete = true,
                failure = IllegalStateException("preferences unavailable")
            )
        )

        assertEquals(StartupDestination.Onboarding, useCase())
    }

    @Test
    fun invoke_propagatesStartupCheckFailure() = runTest {
        val failure = IllegalStateException("session unavailable")
        val useCase = DetermineStartupDestinationUseCase(
            authSessionReader = FakeAuthSessionReader(session = null, failure = failure),
            onboardingCompletionReader = FakeOnboardingCompletionReader(isComplete = false)
        )

        try {
            useCase()
            fail("Expected startup check failure")
        } catch (actual: IllegalStateException) {
            assertEquals(failure, actual)
        }
    }

    private class FakeAuthSessionReader(
        private val session: AuthSession?,
        private val failure: Throwable? = null
    ) : AuthSessionReader {
        override suspend fun currentSession(): AuthSession? {
            failure?.let { throw it }
            return session
        }
    }

    private class FakeOnboardingCompletionReader(
        private val isComplete: Boolean,
        private val failure: Exception? = null
    ) : OnboardingCompletionReader {
        override suspend fun isOnboardingComplete(userId: String): Boolean {
            failure?.let { throw it }
            return isComplete
        }
    }
}
