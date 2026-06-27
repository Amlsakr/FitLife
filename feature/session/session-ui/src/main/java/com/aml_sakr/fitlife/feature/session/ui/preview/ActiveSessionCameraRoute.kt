package com.aml_sakr.fitlife.feature.session.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.feature.session.ui.R

/**
 * The production active-session route for camera-based sessions.
 * AC 1, 5, 6 compliance:
 * - Full-screen background preview.
 * - Dark session treatment.
 * - Exit and audio-only toggle controls.
 * - Error/fallback state.
 */
@Composable
fun ActiveSessionCameraRoute(
    onExitSession: () -> Unit,
    onSwitchToAudioOnly: () -> Unit,
    modifier: Modifier = Modifier,
    cameraPreviewProvider: CameraPreviewProvider? = null
) {
    var previewState by remember { mutableStateOf<SessionCameraPreviewState>(SessionCameraPreviewState.Loading) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Dark session treatment
    ) {
        // Full-screen background preview
        CameraPreview(
            onStateChanged = { previewState = it },
            providerOverride = cameraPreviewProvider
        )

        // Overlay Chrome
        ActiveSessionOverlay(
            previewState = previewState,
            onExitSession = onExitSession,
            onSwitchToAudioOnly = onSwitchToAudioOnly,
            onRetryCamera = {
                previewState = SessionCameraPreviewState.Loading
            }
        )
    }
}

@Composable
private fun ActiveSessionOverlay(
    previewState: SessionCameraPreviewState,
    onExitSession: () -> Unit,
    onSwitchToAudioOnly: () -> Unit,
    onRetryCamera: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExitSession,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.session_exit_action),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onSwitchToAudioOnly,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = stringResource(R.string.session_audio_only_toggle),
                    tint = Color.White
                )
            }
        }

        // Center State (Loading or Error)
        when (previewState) {
            SessionCameraPreviewState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is SessionCameraPreviewState.Error -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.session_camera_error_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(R.string.session_camera_error_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Row {
                            TextButton(onClick = onSwitchToAudioOnly) {
                                Text(stringResource(R.string.camera_permission_audio_only))
                            }
                            Button(onClick = onRetryCamera) {
                                Text(stringResource(R.string.session_camera_error_retry))
                            }
                        }
                    }
                }
            }
            SessionCameraPreviewState.Active -> {
                // Minimal active session chrome: Non-final copy
                Text(
                    text = stringResource(R.string.session_active_preview_label),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
