package com.aml_sakr.fitlife.feature.auth.ui.splash

import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_emitsNavigateToAuth_whenStartupDestinationIsAuth() = runTest(dispatcher) {
        val viewModel = SplashViewModel(
            determineStartupDestination = { StartupDestination.Auth },
            startupRouteErrorLogger = RecordingStartupRouteErrorLogger()
        )

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(SplashAction.NavigateToAuth, viewModel.actions.first())
    }

    @Test
    fun init_emitsNavigateToOnboarding_whenStartupDestinationIsOnboarding() = runTest(dispatcher) {
        val viewModel = SplashViewModel(
            determineStartupDestination = { StartupDestination.Onboarding },
            startupRouteErrorLogger = RecordingStartupRouteErrorLogger()
        )

        advanceUntilIdle()

        assertEquals(SplashAction.NavigateToOnboarding, viewModel.actions.first())
    }

    @Test
    fun init_emitsNavigateToHome_whenStartupDestinationIsHome() = runTest(dispatcher) {
        val viewModel = SplashViewModel(
            determineStartupDestination = { StartupDestination.Home },
            startupRouteErrorLogger = RecordingStartupRouteErrorLogger()
        )

        advanceUntilIdle()

        assertEquals(SplashAction.NavigateToHome, viewModel.actions.first())
    }

    @Test
    fun init_logsErrorAndEmitsRetryableFallback_whenStartupCheckFails() = runTest(dispatcher) {
        val failure = IllegalStateException("boom")
        val logger = RecordingStartupRouteErrorLogger()
        val viewModel = SplashViewModel(
            determineStartupDestination = { throw failure },
            startupRouteErrorLogger = logger
        )

        advanceUntilIdle()

        assertSame(failure, logger.throwable)
        assertEquals(SplashState(isLoading = false, hasRetryableError = true), viewModel.state.value)
        assertEquals(SplashAction.ShowRetryableFallback, viewModel.actions.first())
    }

    private class RecordingStartupRouteErrorLogger : StartupRouteErrorLogger {
        var throwable: Throwable? = null

        override fun logStartupRouteFailure(throwable: Throwable) {
            this.throwable = throwable
        }
    }
}
