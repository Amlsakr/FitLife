package com.aml_sakr.fitlife.feature.session.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
    onExitSession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
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
                analyzePoseUseCase = viewModel.analyzePoseUseCase,
                viewModel = viewModel
            )
        }
    }
}
