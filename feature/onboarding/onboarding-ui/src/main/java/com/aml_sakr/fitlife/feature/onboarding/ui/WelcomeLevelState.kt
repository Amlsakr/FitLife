package com.aml_sakr.fitlife.feature.onboarding.ui

import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel

data class WelcomeLevelState(
    val selectedLevel: FitnessLevel? = null,
    val isSelectionSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UIState
