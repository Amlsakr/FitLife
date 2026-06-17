package com.aml_sakr.fitlife.feature.auth.domain.startup

sealed interface StartupDestination {
    data object Auth : StartupDestination
    data object Onboarding : StartupDestination
    data object Home : StartupDestination
}
