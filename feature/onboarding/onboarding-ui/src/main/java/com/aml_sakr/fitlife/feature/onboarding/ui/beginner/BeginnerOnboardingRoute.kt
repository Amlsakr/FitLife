package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun BeginnerOnboardingRoute(
    viewModel: BeginnerOnboardingViewModel,
    onBackToLevelSelector: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                BeginnerOnboardingAction.NavigateBackToLevelSelector -> onBackToLevelSelector()
                BeginnerOnboardingAction.Finish -> onFinish()
            }
        }
    }

    BeginnerOnboardingScreen(
        state = state,
        onBack = { viewModel.onEvent(BeginnerOnboardingEvent.BackPressed) },
        onGoalToggled = { viewModel.onEvent(BeginnerOnboardingEvent.GoalToggled(it)) },
        onEquipmentToggled = { viewModel.onEvent(BeginnerOnboardingEvent.EquipmentToggled(it)) },
        onFrequencySelected = { viewModel.onEvent(BeginnerOnboardingEvent.FrequencySelected(it)) },
        onContinue = { viewModel.onEvent(BeginnerOnboardingEvent.ContinuePressed) }
    )
}
