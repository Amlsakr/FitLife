package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal

data class BeginnerOnboardingState(
    val currentStep: BeginnerOnboardingStep = BeginnerOnboardingStep.Goals,
    val selectedGoals: Set<FitnessGoal> = emptySet(),
    val selectedEquipment: Set<Equipment> = emptySet(),
    val weeklyFrequency: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UIState {
    val canContinue: Boolean
        get() = when (currentStep) {
            BeginnerOnboardingStep.Goals -> selectedGoals.isNotEmpty()
            BeginnerOnboardingStep.Equipment -> selectedEquipment.isNotEmpty()
            BeginnerOnboardingStep.Frequency -> weeklyFrequency in BeginnerOnboardingViewModel.ValidWeeklyFrequencyRange
        }
}
