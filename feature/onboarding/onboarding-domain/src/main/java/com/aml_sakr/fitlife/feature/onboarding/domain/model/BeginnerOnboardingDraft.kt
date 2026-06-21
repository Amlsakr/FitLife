package com.aml_sakr.fitlife.feature.onboarding.domain.model

data class BeginnerOnboardingDraft(
    val currentStep: BeginnerOnboardingStep = BeginnerOnboardingStep.Goals,
    val goals: Set<FitnessGoal> = emptySet(),
    val equipment: Set<Equipment> = emptySet(),
    val weeklyFrequency: Int? = null
) {
    val isComplete: Boolean
        get() = goals.isNotEmpty() && equipment.isNotEmpty() && weeklyFrequency != null
}
