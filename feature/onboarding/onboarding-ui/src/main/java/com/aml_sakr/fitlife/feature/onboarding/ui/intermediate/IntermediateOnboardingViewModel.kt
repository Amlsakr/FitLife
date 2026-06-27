package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOneRepMaxInput
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadIntermediateDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveIntermediateProfileUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IntermediateOnboardingViewModel(
    private val userId: String,
    private val readIntermediateDraftUseCase: ReadIntermediateDraftUseCase,
    private val saveIntermediateProfileUseCase: SaveIntermediateProfileUseCase,
    private val markOnboardingCompleteUseCase: MarkOnboardingCompleteUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseMviViewModel<IntermediateOnboardingState, IntermediateOnboardingEvent, IntermediateOnboardingAction>(
    IntermediateOnboardingState()
) {
    private val saveMutex = Mutex()

    init {
        loadDraft()
    }

    override fun handleEvent(event: IntermediateOnboardingEvent) {
        if (state.value.isLoading) return

        when (event) {
            IntermediateOnboardingEvent.BackPressed -> handleBackPressed()
            IntermediateOnboardingEvent.ContinuePressed -> handleContinuePressed()
            is IntermediateOnboardingEvent.SplitSelected -> selectSplit(event.split)
            is IntermediateOnboardingEvent.GoalToggled -> toggleGoal(event.goal)
            is IntermediateOnboardingEvent.OneRepMaxValueChanged -> updateOneRepMaxValue(event.lift, event.valueText)
            is IntermediateOnboardingEvent.OneRepMaxUnitChanged -> updateOneRepMaxUnit(event.lift, event.unit)
            is IntermediateOnboardingEvent.JumpToStep -> setAndPersist(state.value.copy(currentStep = event.step))
        }
    }

    private fun loadDraft() {
        if (state.value.isLoading) return
        val restoredState = savedStateHandle.get<IntermediateOnboardingState>(SAVED_STATE_KEY)
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = readIntermediateDraftUseCase()) {
                is Result.Success -> applyDraft(
                    draft = result.value,
                    restoredState = restoredState
                )
                is Result.Failure -> setState {
                    restoredState?.copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    ) ?: copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    private fun applyDraft(
        draft: IntermediateOnboardingDraft,
        restoredState: IntermediateOnboardingState? = null
    ) {
        val restoredSelections = restoredState?.let {
            IntermediateOnboardingState(
                selectedSplit = it.selectedSplit,
                selectedGoals = it.selectedGoals,
                oneRepMaxInputs = it.oneRepMaxInputs
            )
        }

        setAndPersist(
            IntermediateOnboardingState(
                currentStep = restoredState?.currentStep ?: draft.currentStep,
                selectedSplit = restoredSelections?.selectedSplit ?: draft.currentSplit,
                selectedGoals = restoredSelections?.selectedGoals ?: draft.goals,
                oneRepMaxInputs = restoredSelections?.oneRepMaxInputs
                    ?: draft.oneRepMaxInputs.withDefaults(),
                isLoading = false,
                errorMessage = null
            )
        )
    }

    private fun handleBackPressed() {
        when (state.value.currentStep) {
            IntermediateOnboardingStep.Split ->
                sendAction(IntermediateOnboardingAction.NavigateBackToLevelSelector)
            IntermediateOnboardingStep.Goals -> updateStep(IntermediateOnboardingStep.Split)
            IntermediateOnboardingStep.OneRepMax -> updateStep(IntermediateOnboardingStep.Goals)
        }
    }

    private fun handleContinuePressed() {
        if (state.value.isLoading || !state.value.canContinue) {
            return
        }

        when (state.value.currentStep) {
            IntermediateOnboardingStep.Split -> persistAndAdvance(IntermediateOnboardingStep.Goals)
            IntermediateOnboardingStep.Goals -> persistAndAdvance(IntermediateOnboardingStep.OneRepMax)
            IntermediateOnboardingStep.OneRepMax -> finalizeIntermediateProfile()
        }
    }

    private fun selectSplit(split: IntermediateTrainingSplit) {
        commitSelectionChange(
            state.value.copy(
                selectedSplit = split,
                errorMessage = null
            )
        )
    }

    private fun toggleGoal(goal: FitnessGoal) {
        commitSelectionChange(
            state.value.copy(
                selectedGoals = state.value.selectedGoals.toggle(goal),
                errorMessage = null
            )
        )
    }

    private fun updateOneRepMaxValue(lift: OneRepMaxLift, valueText: String) {
        commitDraftChange(
            state.value.copy(
                oneRepMaxInputs = state.value.oneRepMaxInputs + (
                    lift to state.value.oneRepMaxInputs.getValue(lift).copy(valueText = valueText)
                    ),
                errorMessage = null
            )
        )
    }

    private fun updateOneRepMaxUnit(lift: OneRepMaxLift, unit: OneRepMaxUnit) {
        commitDraftChange(
            state.value.copy(
                oneRepMaxInputs = state.value.oneRepMaxInputs + (
                    lift to state.value.oneRepMaxInputs.getValue(lift).copy(unit = unit)
                    ),
                errorMessage = null
            )
        )
    }

    private fun persistAndAdvance(nextStep: IntermediateOnboardingStep) {
        val currentState = state.value
        val transitionedState = currentState.copy(currentStep = nextStep)
        val draft = transitionedState.toDraft(nextStep)
        setAndPersist(
            transitionedState.copy(
                isLoading = true,
                errorMessage = null
            )
        )
        saveDraft(
            draft = draft,
            onSuccess = {
                setAndPersist(
                    state.value.copy(isLoading = false, errorMessage = null)
                )
            },
            onFailure = { errorMessage ->
                setAndPersist(
                    currentState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                )
            }
        )
    }

    private fun finalizeIntermediateProfile() {
        val draft = state.value.toDraft(IntermediateOnboardingStep.OneRepMax)
        setAndPersist(
            state.value.copy(
                isLoading = true,
                errorMessage = null
            )
        )
        viewModelScope.launch {
            saveMutex.withLock {
                when (val saveResult = saveIntermediateProfileUseCase(draft, userId)) {
                    is Result.Failure -> setAndPersist(
                        state.value.copy(
                            isLoading = false,
                            errorMessage = saveResult.error.message
                        )
                    )

                    is Result.Success -> when (
                        val completionResult = markOnboardingCompleteUseCase(userId)
                    ) {
                        is Result.Success -> {
                            setAndPersist(
                                state.value.copy(
                                    isLoading = false,
                                    errorMessage = null
                                )
                            )
                            sendAction(IntermediateOnboardingAction.Finish)
                        }

                        is Result.Failure -> setAndPersist(
                            state.value.copy(
                                isLoading = false,
                                errorMessage = completionResult.error.message
                            )
                        )
                    }
                }
            }
        }
    }

    private fun updateStep(step: IntermediateOnboardingStep) {
        val currentState = state.value
        val transitionedState = currentState.copy(currentStep = step, errorMessage = null)
        val updatedDraft = transitionedState.toDraft(step)
        setAndPersist(transitionedState)
        saveDraft(
            draft = updatedDraft,
            onSuccess = {
                setAndPersist(
                    state.value.copy(errorMessage = null)
                )
            },
            onFailure = { errorMessage ->
                setAndPersist(
                    currentState.copy(
                        errorMessage = errorMessage
                    )
                )
            }
        )
    }

    private fun commitDraftChange(updatedState: IntermediateOnboardingState) {
        setAndPersist(updatedState)
        autosave(updatedState.toDraft(updatedState.currentStep))
    }

    private fun autosave(draft: IntermediateOnboardingDraft) {
        saveDraft(
            draft = draft,
            onSuccess = {
                setAndPersist(
                    state.value.copy(
                        errorMessage = null
                    )
                )
            },
            onFailure = { errorMessage ->
                setAndPersist(
                    state.value.copy(
                        errorMessage = errorMessage
                    )
                )
            }
        )
    }

    private fun saveDraft(
        draft: IntermediateOnboardingDraft,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            saveMutex.withLock {
                when (val result = saveIntermediateProfileUseCase(draft, userId)) {
                    is Result.Success -> onSuccess()
                    is Result.Failure -> onFailure(result.error.message)
                }
            }
        }
    }

    private fun IntermediateOnboardingState.toDraft(
        step: IntermediateOnboardingStep
    ): IntermediateOnboardingDraft = IntermediateOnboardingDraft(
        currentStep = step,
        currentSplit = selectedSplit,
        goals = selectedGoals,
        oneRepMaxInputs = oneRepMaxInputs
    )

    private fun commitSelectionChange(updatedState: IntermediateOnboardingState) {
        setAndPersist(updatedState)
        saveDraft(
            draft = updatedState.toDraft(updatedState.currentStep),
            onFailure = { errorMessage ->
                setAndPersist(
                    state.value.copy(
                        errorMessage = errorMessage
                    )
                )
            }
        )
    }

    private fun setAndPersist(updatedState: IntermediateOnboardingState) {
        setState { updatedState }
        savedStateHandle[SAVED_STATE_KEY] = updatedState
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (contains(value)) filterNot { it == value }.toSet() else plus(value)

    private fun Map<OneRepMaxLift, IntermediateOneRepMaxInput>.withDefaults():
        Map<OneRepMaxLift, OneRepMaxInput> =
        OneRepMaxLift.entries.associateWith { lift -> getOrDefault(lift, OneRepMaxInput()) }

    private companion object {
        const val SAVED_STATE_KEY = "intermediate_onboarding_state"
    }
}
