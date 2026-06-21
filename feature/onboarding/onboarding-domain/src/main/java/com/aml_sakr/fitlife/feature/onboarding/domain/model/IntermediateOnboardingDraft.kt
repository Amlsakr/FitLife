package com.aml_sakr.fitlife.feature.onboarding.domain.model

data class IntermediateOnboardingDraft(
    val currentStep: IntermediateOnboardingStep = IntermediateOnboardingStep.Split,
    val currentSplit: IntermediateTrainingSplit? = null,
    val goals: Set<FitnessGoal> = emptySet(),
    val oneRepMaxInputs: Map<OneRepMaxLift, IntermediateOneRepMaxInput> = OneRepMaxLift.entries.associateWith {
        IntermediateOneRepMaxInput()
    }
) {
    val oneRepMax: Map<OneRepMaxLift, Float>
        get() = oneRepMaxInputs.entries.mapNotNull { (lift, input) ->
            val value = input.valueText.trim().toFloatOrNull() ?: return@mapNotNull null
            if (value <= 0f) return@mapNotNull null
            lift to (value * input.unit.kgMultiplier)
        }.toMap()

    val isComplete: Boolean
        get() = currentSplit != null && goals.isNotEmpty()
}
