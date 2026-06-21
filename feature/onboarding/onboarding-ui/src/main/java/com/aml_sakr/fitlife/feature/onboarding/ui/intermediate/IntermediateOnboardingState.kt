package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import java.io.Serializable

data class IntermediateOnboardingState(
    val currentStep: IntermediateOnboardingStep = IntermediateOnboardingStep.Split,
    val selectedSplit: IntermediateTrainingSplit? = null,
    val selectedGoals: Set<FitnessGoal> = emptySet(),
    val oneRepMaxInputs: Map<OneRepMaxLift, OneRepMaxInput> = OneRepMaxLift.entries.associateWith {
        OneRepMaxInput()
    },
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UIState, Serializable {
    val canContinue: Boolean
        get() = when (currentStep) {
            IntermediateOnboardingStep.Split -> selectedSplit != null
            IntermediateOnboardingStep.Goals -> selectedGoals.isNotEmpty()
            IntermediateOnboardingStep.OneRepMax -> oneRepMaxValidationError() == null
        }

    fun oneRepMaxValidationError(): String? {
        return oneRepMaxInputs.entries
            .mapNotNull { (lift, input) -> input.validationError(lift) }
            .firstOrNull()
    }
}

private fun OneRepMaxInput.validationError(lift: OneRepMaxLift): String? {
    val value = valueText.trim()
    if (value.isBlank()) return null

    val parsed = value.toFloatOrNull() ?: return "${lift.displayName} must be a number."
    if (parsed <= 0f) return "${lift.displayName} must be greater than 0."
    return null
}

private val OneRepMaxLift.displayName: String
    get() = when (this) {
        OneRepMaxLift.BenchPress -> "Bench Press"
        OneRepMaxLift.Squat -> "Squat"
        OneRepMaxLift.Deadlift -> "Deadlift"
    }
