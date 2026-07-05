package com.aml_sakr.fitlife.feature.session.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.aml_sakr.fitlife.feature.session.ui.permission.CameraPermissionGateRoute
import com.aml_sakr.fitlife.feature.session.ui.preview.ActiveSessionCameraRoute

@Composable
fun SessionEntryDestination(
    userId: String,
    planId: String,
    workoutDayId: String,
    onExitSession: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(userId, planId, workoutDayId) {
        viewModel.onEvent(ActiveSessionEvent.Initialize(userId, planId, workoutDayId))
    }

    var isPermissionGateActive by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPermissionGateActive) {
            CameraPermissionGateRoute(
                onCameraSessionReady = {
                    isPermissionGateActive = false
                },
                onAudioOnlySession = {
                    isPermissionGateActive = false
                    viewModel.onEvent(ActiveSessionEvent.ToggleAudioOnlyMode)
                }
            )
        } else {
            ActiveSessionCameraRoute(
                onExitSession = onExitSession,
                onNavigateToSummary = onNavigateToSummary,
                analyzePoseUseCase = viewModel.analyzePoseUseCase,
                viewModel = viewModel
            )
        }
    }
}
