package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.AuthUiConstants
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val startupDestinationResolver: StartupDestinationResolver,
    private val startupRouteErrorLogger: StartupRouteErrorLogger
) : BaseMviViewModel<SplashState, SplashEvent, SplashAction>(SplashState()) {
    private var startupRouteJob: Job? = null

    constructor(
        determineStartupDestination: suspend () -> StartupDestination,
        startupRouteErrorLogger: StartupRouteErrorLogger
    ) : this(
        startupDestinationResolver = StartupDestinationResolver { determineStartupDestination() },
        startupRouteErrorLogger = startupRouteErrorLogger
    )

    init {
        onEvent(SplashEvent.CheckStartupRoute)
    }

    override fun handleEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckStartupRoute,
            SplashEvent.RetryStartupRoute -> checkStartupRoute()
        }
    }

    private fun checkStartupRoute() {
        if (startupRouteJob?.isActive == true) return

        setState { copy(isLoading = true, hasRetryableError = false) }

        startupRouteJob = viewModelScope.launch {
            try {
                val startupDestination = startupDestinationResolver.resolve()
                delay(AuthUiConstants.SPLASH_DISPLAY_DURATION_MILLIS)
                when (startupDestination) {
                    StartupDestination.Auth -> sendAction(SplashAction.NavigateToAuth)
                    StartupDestination.Onboarding -> sendAction(SplashAction.NavigateToOnboarding)
                    StartupDestination.Home -> sendAction(SplashAction.NavigateToHome)
                }
                setState { copy(isLoading = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                startupRouteErrorLogger.logStartupRouteFailure(throwable)
                setState { copy(isLoading = false, hasRetryableError = true) }
                sendAction(SplashAction.ShowRetryableFallback)
            }
        }
    }
}
