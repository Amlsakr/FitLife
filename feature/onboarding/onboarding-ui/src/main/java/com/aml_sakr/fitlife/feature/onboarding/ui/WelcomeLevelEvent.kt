package com.aml_sakr.fitlife.feature.onboarding.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

sealed interface WelcomeLevelEvent : UIEvent {
    data object LoadSelectedLevel : WelcomeLevelEvent
    data class LevelSelected(val level: FitnessLevel) : WelcomeLevelEvent
    data class SelectLevel(val level: FitnessLevel) : WelcomeLevelEvent
    data object ContinuePressed : WelcomeLevelEvent
}
