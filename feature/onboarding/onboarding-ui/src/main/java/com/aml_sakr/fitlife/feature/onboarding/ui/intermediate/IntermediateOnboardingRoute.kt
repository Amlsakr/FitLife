package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun IntermediateOnboardingRoute(
    viewModel: IntermediateOnboardingViewModel,
    onBackToLevelSelector: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                IntermediateOnboardingAction.NavigateBackToLevelSelector -> onBackToLevelSelector()
                IntermediateOnboardingAction.Finish -> onFinish()
            }
        }
    }

    IntermediateOnboardingScreen(
        state = state,
        onBack = { viewModel.onEvent(IntermediateOnboardingEvent.BackPressed) },
        onSplitSelected = { viewModel.onEvent(IntermediateOnboardingEvent.SplitSelected(it)) },
        onGoalToggled = { viewModel.onEvent(IntermediateOnboardingEvent.GoalToggled(it)) },
        onOneRepMaxValueChanged = {
            lift, valueText -> viewModel.onEvent(
                IntermediateOnboardingEvent.OneRepMaxValueChanged(lift, valueText)
            )
        },
        onOneRepMaxUnitChanged = {
            lift, unit -> viewModel.onEvent(
                IntermediateOnboardingEvent.OneRepMaxUnitChanged(lift, unit)
            )
        },
        onContinue = { viewModel.onEvent(IntermediateOnboardingEvent.ContinuePressed) }
    )
}
