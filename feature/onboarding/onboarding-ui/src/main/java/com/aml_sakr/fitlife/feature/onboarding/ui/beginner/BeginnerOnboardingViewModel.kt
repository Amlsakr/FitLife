package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadBeginnerDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveBeginnerProfileUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BeginnerOnboardingViewModel(
    private val userId: String,
    private val readBeginnerDraftUseCase: ReadBeginnerDraftUseCase,
    private val saveBeginnerProfileUseCase: SaveBeginnerProfileUseCase,
    private val markOnboardingCompleteUseCase: MarkOnboardingCompleteUseCase
) : BaseMviViewModel<BeginnerOnboardingState, BeginnerOnboardingEvent, BeginnerOnboardingAction>(
    BeginnerOnboardingState()
) {
    private val saveMutex = Mutex()

    init {
        loadDraft()
    }

    override fun handleEvent(event: BeginnerOnboardingEvent) {
        if (state.value.isLoading) return

        when (event) {
            BeginnerOnboardingEvent.BackPressed -> handleBackPressed()
            BeginnerOnboardingEvent.ContinuePressed -> handleContinuePressed()
            is BeginnerOnboardingEvent.GoalToggled -> toggleGoal(event.goal)
            is BeginnerOnboardingEvent.EquipmentToggled -> toggleEquipment(event.equipment)
            is BeginnerOnboardingEvent.FrequencySelected -> selectFrequency(event.frequency)
            is BeginnerOnboardingEvent.JumpToStep -> setState { copy(currentStep = event.step) }
        }
    }

    private fun loadDraft() {
        if (state.value.isLoading) return
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = readBeginnerDraftUseCase()) {
                is Result.Success -> applyDraft(result.value)
                is Result.Failure -> setState {
                    copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    private fun applyDraft(draft: BeginnerOnboardingDraft) {
        setState {
            copy(
                currentStep = draft.currentStep,
                selectedGoals = draft.goals,
                selectedEquipment = draft.equipment,
                weeklyFrequency = draft.weeklyFrequency,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun handleBackPressed() {
        when (state.value.currentStep) {
            BeginnerOnboardingStep.Goals -> sendAction(BeginnerOnboardingAction.NavigateBackToLevelSelector)
            BeginnerOnboardingStep.Equipment -> updateStep(BeginnerOnboardingStep.Goals)
            BeginnerOnboardingStep.Frequency -> updateStep(BeginnerOnboardingStep.Equipment)
        }
    }

    private fun handleContinuePressed() {
        if (state.value.isLoading || !state.value.canContinue) return

        when (state.value.currentStep) {
            BeginnerOnboardingStep.Goals -> persistAndAdvance(BeginnerOnboardingStep.Equipment)
            BeginnerOnboardingStep.Equipment -> persistAndAdvance(BeginnerOnboardingStep.Frequency)
            BeginnerOnboardingStep.Frequency -> finalizeBeginnerProfile()
        }
    }

    private fun toggleGoal(goal: FitnessGoal) {
        val updatedState = state.value.copy(
            selectedGoals = state.value.selectedGoals.toggle(goal),
            errorMessage = null
        )
        commitDraftChange(updatedState)
    }

    private fun toggleEquipment(equipment: Equipment) {
        val updatedState = state.value.copy(
            selectedEquipment = state.value.selectedEquipment.toggle(equipment),
            errorMessage = null
        )
        commitDraftChange(updatedState)
    }

    private fun selectFrequency(frequency: Int) {
        if (frequency !in ValidWeeklyFrequencyRange) {
            setState {
                copy(
                    weeklyFrequency = null,
                    errorMessage = "Choose a frequency from 1 to 7 days per week."
                )
            }
            return
        }

        commitDraftChange(
            state.value.copy(
                weeklyFrequency = frequency,
                errorMessage = null
            )
        )
    }

    private fun persistAndAdvance(nextStep: BeginnerOnboardingStep) {
        val draft = state.value.toDraft(nextStep)
        saveDraft(
            draft = draft,
            onSuccess = {
                setState {
                    copy(
                        currentStep = nextStep,
                        errorMessage = null
                    )
                }
            }
        )
    }

    private fun finalizeBeginnerProfile() {
        val draft = state.value.toDraft(BeginnerOnboardingStep.Frequency)
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            saveMutex.withLock {
                when (val saveResult = saveBeginnerProfileUseCase(draft, userId)) {
                    is Result.Failure -> setState {
                        copy(
                            isLoading = false,
                            errorMessage = saveResult.error.message
                        )
                    }

                    is Result.Success -> when (
                        val completionResult = markOnboardingCompleteUseCase(userId)
                    ) {
                        is Result.Success -> {
                            setState { copy(isLoading = false, errorMessage = null) }
                            sendAction(BeginnerOnboardingAction.Finish)
                        }

                        is Result.Failure -> setState {
                            copy(
                                isLoading = false,
                                errorMessage = completionResult.error.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateStep(step: BeginnerOnboardingStep) {
        val updatedDraft = state.value.toDraft(step)
        setState { copy(currentStep = step, errorMessage = null) }
        autosave(updatedDraft)
    }

    private fun commitDraftChange(updatedState: BeginnerOnboardingState) {
        setState { updatedState }
        autosave(updatedState.toDraft(updatedState.currentStep))
    }

    private fun autosave(draft: BeginnerOnboardingDraft) {
        saveDraft(draft = draft)
    }

    private fun saveDraft(
        draft: BeginnerOnboardingDraft,
        userId: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            saveMutex.withLock {
                when (val result = saveBeginnerProfileUseCase(draft, userId)) {
                    is Result.Success -> onSuccess()
                    is Result.Failure -> onFailure(result.error.message)
                }
            }
        }
    }

    private fun BeginnerOnboardingState.toDraft(step: BeginnerOnboardingStep): BeginnerOnboardingDraft =
        BeginnerOnboardingDraft(
            currentStep = step,
            goals = selectedGoals,
            equipment = selectedEquipment,
            weeklyFrequency = weeklyFrequency
        )

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (contains(value)) filterNot { it == value }.toSet() else plus(value)

    companion object {
        val ValidWeeklyFrequencyRange: IntRange = 1..7
    }
}
