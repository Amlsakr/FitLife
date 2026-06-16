package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashState
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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

        runCurrent()
        advanceTimeBy(4_999)

        assertTrue(viewModel.state.value.isLoading)

        advanceTimeBy(1)
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
        runCurrent()

        advanceUntilIdle()

        assertEquals(SplashAction.NavigateToOnboarding, viewModel.actions.first())
    }

    @Test
    fun init_emitsNavigateToHome_whenStartupDestinationIsHome() = runTest(dispatcher) {
        val viewModel = SplashViewModel(
            determineStartupDestination = { StartupDestination.Home },
            startupRouteErrorLogger = RecordingStartupRouteErrorLogger()
        )
        runCurrent()

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
        runCurrent()

        advanceUntilIdle()

        assertSame(failure, logger.throwable)
        assertEquals(SplashState(isLoading = false, hasRetryableError = true), viewModel.state.value)
        assertEquals(SplashAction.ShowRetryableFallback, viewModel.actions.first())
    }

    @Test
    fun retry_doesNotStartConcurrentStartupCheck() = runTest(dispatcher) {
        var invocationCount = 0
        val viewModel = SplashViewModel(
            determineStartupDestination = {
                invocationCount += 1
                awaitCancellation()
            },
            startupRouteErrorLogger = RecordingStartupRouteErrorLogger()
        )

        runCurrent()
        viewModel.onEvent(SplashEvent.RetryStartupRoute)
        runCurrent()

        assertEquals(1, invocationCount)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun cancellation_isNotLoggedAsStartupFailure() = runTest(dispatcher) {
        val logger = RecordingStartupRouteErrorLogger()
        val viewModel = SplashViewModel(
            determineStartupDestination = { throw CancellationException("cancelled") },
            startupRouteErrorLogger = logger
        )

        advanceUntilIdle()

        assertEquals(null, logger.throwable)
        assertFalse(viewModel.state.value.hasRetryableError)
    }

    private class RecordingStartupRouteErrorLogger : StartupRouteErrorLogger {
        var throwable: Throwable? = null

        override fun logStartupRouteFailure(throwable: Throwable) {
            this.throwable = throwable
        }
    }

}
