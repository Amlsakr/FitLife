package com.aml_sakr.fitlife.feature.session.ui.preview

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aml_sakr.fitlife.feature.session.domain.pose.AnalyzePoseUseCase
import com.aml_sakr.fitlife.feature.session.ui.ActiveSessionAction
import com.aml_sakr.fitlife.feature.session.ui.ActiveSessionEvent
import com.aml_sakr.fitlife.feature.session.ui.ActiveSessionViewModel
import com.aml_sakr.fitlife.feature.session.ui.R
import com.aml_sakr.fitlife.feature.session.ui.components.AudioOnlyOverlay
import com.aml_sakr.fitlife.feature.session.ui.components.EquipmentUnavailableBottomSheet
import com.aml_sakr.fitlife.feature.session.ui.components.ExerciseDemo
import com.aml_sakr.fitlife.feature.session.ui.components.FatigueWarningBanner
import com.aml_sakr.fitlife.feature.session.ui.components.SkeletonOverlay
import com.aml_sakr.fitlife.feature.session.ui.service.SessionAudioService
import com.aml_sakr.fitlife.feature.session.ui.service.SessionTtsManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * The production active-session route for camera-based sessions.
 * AC 1, 3, 5, 6 compliance:
 * - Full-screen background preview.
 * - Real-time pose detection integration via [AnalyzePoseUseCase].
 * - Dark session treatment.
 * - Exit and audio-only toggle controls.
 * - Error/fallback state.
 */
@Composable
fun ActiveSessionCameraRoute(
    onExitSession: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    modifier: Modifier = Modifier,
    analyzePoseUseCase: AnalyzePoseUseCase? = null,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
    cameraPreviewProvider: CameraPreviewProvider? = null
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val ttsManager = remember(context) { SessionTtsManager(context) }

    LaunchedEffect(ttsManager) {
        SessionAudioService.start(context)
        viewModel.actions.collect { action ->
            when (action) {
                ActiveSessionAction.ExitSession -> onExitSession()
                is ActiveSessionAction.NavigateToSummary -> onNavigateToSummary(action.sessionId)
                is ActiveSessionAction.Announce -> ttsManager.speak(action.message)
            }
        }
    }

    DisposableEffect(ttsManager) {
        onDispose {
            SessionAudioService.stop(context)
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(state.isFatigued) {
        if (state.isFatigued) {
            SessionAudioService.playAlert(context)
        }
    }

    var previewState by remember { mutableStateOf<SessionCameraPreviewState>(SessionCameraPreviewState.Loading) }
    var retryKey by remember { mutableIntStateOf(0) }

    // Patch: Use a channel to decouple frame arrival from processing and prevent coroutine flooding.
    // Fix: Add onUndeliveredElement to ensure ImageProxy objects are closed if dropped due to conflation.
    val frameChannel = remember { 
        Channel<Any>(
            capacity = Channel.CONFLATED,
            onUndeliveredElement = { frame ->
                (frame as? ImageProxy)?.close()
            }
        ) 
    }

    LaunchedEffect(analyzePoseUseCase, viewModel) {
        if (analyzePoseUseCase == null) return@LaunchedEffect
        frameChannel.consumeAsFlow().collectLatest { imageProxy ->
            analyzePoseUseCase(imageProxy).collect { poseData ->
                viewModel.onEvent(ActiveSessionEvent.PoseDetected(poseData))
            }
        }
    }

    // Fix: Lifecycle-safe shutdown of the detector to prevent memory leaks and process cleanup issues.
    DisposableEffect(analyzePoseUseCase) {
        onDispose {
            analyzePoseUseCase?.close()
        }
    }

    val analyzer = remember(analyzePoseUseCase) {
        analyzePoseUseCase?.let {
            ImageAnalysis.Analyzer { imageProxy ->
                // Channel.CONFLATED ensures we only process the latest frame if processing is slow.
                frameChannel.trySend(imageProxy)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Dark session treatment
    ) {
        if (state.isAudioOnlyMode) {
            AudioOnlyOverlay()
        } else {
            // Full-screen background preview
            CameraPreview(
                onStateChanged = { 
                    previewState = it
                    viewModel.onEvent(ActiveSessionEvent.CameraStateChanged(it is SessionCameraPreviewState.Active))
                    if (it is SessionCameraPreviewState.Error) {
                        viewModel.onEvent(ActiveSessionEvent.ErrorOccurred(it.throwable))
                    }
                },
                providerOverride = cameraPreviewProvider,
                analyzer = analyzer, // Integrated with AnalyzePoseUseCase
                retryKey = retryKey
            )

            // AC 4, 6: Skeleton Overlay
            state.latestPoseData?.takeIf { state.isCameraActive }?.let { poseData ->
                SkeletonOverlay(
                    poseData = poseData,
                    isMirrored = false // Currently hardcoded to back camera in CameraPreview.kt
                )
            }
        }

        // Overlay Chrome
        ActiveSessionOverlay(
            previewState = previewState,
            isFatigued = state.isFatigued,
            isAudioOnly = state.isAudioOnlyMode,
            currentExerciseName = state.currentExerciseName,
            currentExerciseLottiePath = state.currentExerciseLottiePath,
            totalReps = state.totalReps,
            onDismissFatigue = { viewModel.onEvent(ActiveSessionEvent.DismissFatigue) },
            onExitSession = onExitSession,
            onFinishSession = { viewModel.onEvent(ActiveSessionEvent.FinishSession) },
            onSwitchToAudioOnly = { viewModel.onEvent(ActiveSessionEvent.ToggleAudioOnlyMode) },
            onEquipmentUnavailable = { viewModel.onEvent(ActiveSessionEvent.OnEquipmentUnavailable) },
            onRetryCamera = {
                retryKey++
                previewState = SessionCameraPreviewState.Loading
            }
        )

        if (state.isEquipmentSheetVisible) {
            EquipmentUnavailableBottomSheet(
                alternatives = state.alternatives,
                isLoading = state.isEquipmentSheetLoading,
                onAlternativeSelected = { viewModel.onEvent(ActiveSessionEvent.OnAlternativeSelected(it)) },
                onDismiss = { viewModel.onEvent(ActiveSessionEvent.DismissEquipmentSheet) }
            )
        }
    }
}

@Composable
private fun ActiveSessionOverlay(
    previewState: SessionCameraPreviewState,
    isFatigued: Boolean,
    isAudioOnly: Boolean,
    currentExerciseName: String?,
    currentExerciseLottiePath: String?,
    totalReps: Int,
    onDismissFatigue: () -> Unit,
    onExitSession: () -> Unit,
    onFinishSession: () -> Unit,
    onSwitchToAudioOnly: () -> Unit,
    onEquipmentUnavailable: () -> Unit,
    onRetryCamera: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fatigue Warning Banner
        FatigueWarningBanner(
            visible = isFatigued,
            onDismiss = onDismissFatigue,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp) // Below top bar
        )

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

            Button(
                onClick = onFinishSession,
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Finish")
            }

            IconButton(
                onClick = onSwitchToAudioOnly,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isAudioOnly) Icons.Default.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (isAudioOnly) stringResource(R.string.session_visual_mode_toggle) else stringResource(R.string.session_audio_only_toggle),
                    tint = if (isAudioOnly) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }

        // Center State (Loading or Error)
        if (!isAudioOnly) {
            ExerciseDemo(
                lottiePath = currentExerciseLottiePath,
                totalReps = totalReps,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp)
                    .size(100.dp)
            )
        }

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
                if (!isAudioOnly) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        currentExerciseName?.let {
                            Text(
                                text = it,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = onEquipmentUnavailable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Equipment Unavailable")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.session_active_preview_label),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
