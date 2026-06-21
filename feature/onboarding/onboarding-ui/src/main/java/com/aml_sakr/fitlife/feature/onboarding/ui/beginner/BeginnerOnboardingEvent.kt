package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal

sealed interface BeginnerOnboardingEvent : UIEvent {
    data object BackPressed : BeginnerOnboardingEvent
    data object ContinuePressed : BeginnerOnboardingEvent
    data class GoalToggled(val goal: FitnessGoal) : BeginnerOnboardingEvent
    data class EquipmentToggled(val equipment: Equipment) : BeginnerOnboardingEvent
    data class FrequencySelected(val frequency: Int) : BeginnerOnboardingEvent
    data class JumpToStep(val step: BeginnerOnboardingStep) : BeginnerOnboardingEvent
}
