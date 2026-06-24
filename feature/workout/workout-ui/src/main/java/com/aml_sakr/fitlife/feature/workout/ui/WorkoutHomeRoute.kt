package com.aml_sakr.fitlife.feature.workout.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun WorkoutHomeRoute(
    viewModel: WorkoutHomeViewModel,
    onMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                is WorkoutHomeAction.ShowMessage -> onMessage(action.message)
            }
        }
    }

    WorkoutHomeScreen(
        state = state,
        onRequestPlan = { viewModel.onEvent(WorkoutHomeEvent.RequestPlan) },
        modifier = modifier
    )
}
