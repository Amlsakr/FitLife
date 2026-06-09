package com.aml_sakr.fitlife.feature.auth.ui.splash

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import kotlinx.coroutines.launch

class SplashViewModel(
    private val determineStartupDestination: suspend () -> StartupDestination,
    private val startupRouteErrorLogger: StartupRouteErrorLogger
) : BaseMviViewModel<SplashState, SplashEvent, SplashAction>(SplashState()) {

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
        setState { copy(isLoading = true, hasRetryableError = false) }

        viewModelScope.launch {
            try {
                when (determineStartupDestination()) {
                    StartupDestination.Auth -> sendAction(SplashAction.NavigateToAuth)
                    StartupDestination.Onboarding -> sendAction(SplashAction.NavigateToOnboarding)
                    StartupDestination.Home -> sendAction(SplashAction.NavigateToHome)
                }
                setState { copy(isLoading = false) }
            } catch (throwable: Throwable) {
                startupRouteErrorLogger.logStartupRouteFailure(throwable)
                setState { copy(isLoading = false, hasRetryableError = true) }
                sendAction(SplashAction.ShowRetryableFallback)
            }
        }
    }
}
