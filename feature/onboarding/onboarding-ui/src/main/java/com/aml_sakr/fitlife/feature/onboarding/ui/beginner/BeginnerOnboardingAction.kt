package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction

sealed interface BeginnerOnboardingAction : OneTimeAction {
    data object NavigateBackToLevelSelector : BeginnerOnboardingAction
    data object Finish : BeginnerOnboardingAction
}
