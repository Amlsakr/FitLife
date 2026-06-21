package com.aml_sakr.fitlife.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadSelectedFitnessLevelUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveSelectedFitnessLevelUseCase
import kotlinx.coroutines.launch

class WelcomeLevelViewModel(
    private val readSelectedFitnessLevel: ReadSelectedFitnessLevelUseCase,
    private val saveSelectedFitnessLevel: SaveSelectedFitnessLevelUseCase
) : BaseMviViewModel<WelcomeLevelState, WelcomeLevelEvent, WelcomeLevelAction>(WelcomeLevelState()) {
    init {
        loadSelectedLevel()
    }

    override fun handleEvent(event: WelcomeLevelEvent) {
        when (event) {
            WelcomeLevelEvent.LoadSelectedLevel -> loadSelectedLevel()
            is WelcomeLevelEvent.LevelSelected -> setState {
                copy(selectedLevel = event.level, errorMessage = null)
            }
            is WelcomeLevelEvent.SelectLevel -> confirmSelection(event.level)
            WelcomeLevelEvent.ContinuePressed -> continueWithSelectedLevel()
        }
    }

    private fun loadSelectedLevel() {
        if (state.value.isLoading) return
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = readSelectedFitnessLevel()) {
                is Result.Success -> setState {
                    copy(
                        selectedLevel = result.value,
                        isSelectionSaved = result.value != null,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Failure -> {
                    setState {
                        copy(
                            selectedLevel = null,
                            isSelectionSaved = false,
                            isLoading = false,
                            errorMessage = mapError(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun confirmSelection(level: FitnessLevel) {
        if (state.value.isLoading) return
        setState {
            copy(
                selectedLevel = level,
                isSelectionSaved = false,
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = saveSelectedFitnessLevel(level)) {
                is Result.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            isSelectionSaved = true,
                            errorMessage = null
                        )
                    }
                }
                is Result.Failure -> {
                    setState {
                        copy(
                            isLoading = false,
                            isSelectionSaved = false,
                            errorMessage = mapError(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun continueWithSelectedLevel() {
        if (state.value.isLoading || !state.value.isSelectionSaved) return
        state.value.selectedLevel?.let { level ->
            sendAction(WelcomeLevelAction.forLevel(level))
        }
    }

    private fun mapError(error: OnboardingError): String = error.message
}
