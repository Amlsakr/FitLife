package com.aml_sakr.fitlife.feature.onboarding.ui

import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

sealed interface WelcomeLevelAction : OneTimeAction {
    data object NavigateToBeginner : WelcomeLevelAction
    data object NavigateToIntermediate : WelcomeLevelAction
    data class ShowMessage(val message: String) : WelcomeLevelAction

    companion object {
        fun forLevel(level: FitnessLevel): WelcomeLevelAction =
            when (level) {
                FitnessLevel.Beginner -> NavigateToBeginner
                FitnessLevel.Intermediate -> NavigateToIntermediate
            }
    }
}
