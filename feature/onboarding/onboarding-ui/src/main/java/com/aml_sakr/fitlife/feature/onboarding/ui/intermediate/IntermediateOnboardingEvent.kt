package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift

sealed interface IntermediateOnboardingEvent : UIEvent {
    data object BackPressed : IntermediateOnboardingEvent
    data object ContinuePressed : IntermediateOnboardingEvent
    data class SplitSelected(val split: IntermediateTrainingSplit) : IntermediateOnboardingEvent
    data class GoalToggled(val goal: FitnessGoal) : IntermediateOnboardingEvent
    data class OneRepMaxValueChanged(
        val lift: OneRepMaxLift,
        val valueText: String
    ) : IntermediateOnboardingEvent
    data class OneRepMaxUnitChanged(
        val lift: OneRepMaxLift,
        val unit: OneRepMaxUnit
    ) : IntermediateOnboardingEvent
    data class JumpToStep(val step: IntermediateOnboardingStep) : IntermediateOnboardingEvent
}
