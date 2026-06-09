package com.aml_sakr.fitlife.feature.auth.ui.splash

import com.aml_sakr.fitlife.core.ui.mvi.UIState

data class SplashState(
    val isLoading: Boolean = true,
    val hasRetryableError: Boolean = false
) : UIState
