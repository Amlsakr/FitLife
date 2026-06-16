package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction

sealed interface SplashAction : OneTimeAction {
    data object NavigateToAuth : SplashAction
    data object NavigateToOnboarding : SplashAction
    data object NavigateToHome : SplashAction
    data object ShowRetryableFallback : SplashAction
}
