package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction

sealed interface IntermediateOnboardingAction : OneTimeAction {
    data object NavigateBackToLevelSelector : IntermediateOnboardingAction
    data object Finish : IntermediateOnboardingAction
}
