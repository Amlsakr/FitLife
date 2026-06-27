package com.aml_sakr.fitlife.feature.session.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aml_sakr.fitlife.feature.session.ui.permission.CameraPermissionGateRoute
import com.aml_sakr.fitlife.feature.session.ui.preview.ActiveSessionCameraRoute

@Composable
fun SessionEntryDestination(
    onExitSession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    var sessionMode by remember { mutableStateOf<SessionMode?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val mode = sessionMode
        if (mode == null) {
            CameraPermissionGateRoute(
                onCameraSessionReady = {
                    sessionMode = SessionMode.Camera
                },
                onAudioOnlySession = {
                    sessionMode = SessionMode.AudioOnly
                }
            )
        } else {
            when (mode) {
                SessionMode.Camera -> {
                    ActiveSessionCameraRoute(
                        onExitSession = onExitSession,
                        onSwitchToAudioOnly = { sessionMode = SessionMode.AudioOnly },
                        analyzePoseUseCase = viewModel.analyzePoseUseCase,
                        viewModel = viewModel
                    )
                }

                SessionMode.AudioOnly -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SESSION-001 audio-only guidance is active.",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onExitSession) {
                            Text("Back to home")
                        }
                    }
                }
            }
        }
    }
}

private enum class SessionMode {
    Camera,
    AudioOnly
}
