package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent

sealed interface SplashEvent : UIEvent {
    data object CheckStartupRoute : SplashEvent
    data object RetryStartupRoute : SplashEvent
}
