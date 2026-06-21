package com.aml_sakr.fitlife.feature.session.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.session.ui.R

data class CameraPermissionState(
    val isRationaleRequired: Boolean = false,
    val isPermissionRequestPending: Boolean = false,
    val isAudioOnlyFallbackVisible: Boolean = false
) : UIState

data class CameraPermissionStatus(
    val isGranted: Boolean,
    val shouldShowRationale: Boolean
)

fun interface CameraPermissionRequester {
    fun request(onPermissionResult: (Boolean) -> Unit)
}

sealed interface CameraPermissionEvent : UIEvent {
    data class SessionEntered(
        val shouldShowRationale: Boolean
    ) : CameraPermissionEvent

    data class RationaleStatusChanged(
        val shouldShowRationale: Boolean
    ) : CameraPermissionEvent

    data object ContinueClicked : CameraPermissionEvent

    data object UseAudioOnlyClicked : CameraPermissionEvent

    data class SystemPermissionResult(
        val isGranted: Boolean
    ) : CameraPermissionEvent
}

sealed interface CameraPermissionAction : OneTimeAction {
    data object RequestSystemPermission : CameraPermissionAction

    data object EnterCameraSession : CameraPermissionAction

    data object EnterAudioOnlySession : CameraPermissionAction
}

class CameraPermissionViewModel : BaseMviViewModel<
    CameraPermissionState,
    CameraPermissionEvent,
    CameraPermissionAction
>(CameraPermissionState()) {
    override fun handleEvent(event: CameraPermissionEvent) {
        when (event) {
            is CameraPermissionEvent.SessionEntered -> {
                setState {
                    copy(
                        isRationaleRequired = event.shouldShowRationale,
                        isPermissionRequestPending = false
                    )
                }
            }

            is CameraPermissionEvent.RationaleStatusChanged -> {
                setState {
                    copy(isRationaleRequired = event.shouldShowRationale)
                }
            }

            CameraPermissionEvent.ContinueClicked -> {
                if (state.value.isPermissionRequestPending) return
                setState {
                    copy(isPermissionRequestPending = true)
                }
                sendAction(CameraPermissionAction.RequestSystemPermission)
            }

            CameraPermissionEvent.UseAudioOnlyClicked -> {
                setState {
                    copy(
                        isPermissionRequestPending = false,
                        isAudioOnlyFallbackVisible = true
                    )
                }
                sendAction(CameraPermissionAction.EnterAudioOnlySession)
            }

            is CameraPermissionEvent.SystemPermissionResult -> {
                setState {
                    copy(
                        isPermissionRequestPending = false,
                        isAudioOnlyFallbackVisible = !event.isGranted
                    )
                }
                if (event.isGranted) {
                    sendAction(CameraPermissionAction.EnterCameraSession)
                } else {
                    sendAction(CameraPermissionAction.EnterAudioOnlySession)
                }
            }
        }
    }
}

@Composable
fun CameraPermissionGateRoute(
    onCameraSessionReady: () -> Unit,
    onAudioOnlySession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraPermissionViewModel = viewModel(),
    permissionStatus: CameraPermissionStatus? = null,
    permissionRequester: CameraPermissionRequester? = null
) {
    val context = LocalContext.current
    val cameraPermissionStatus = permissionStatus ?: rememberCameraPermissionStatus(context)
    var hasNotifiedGrantedSession by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionStatus.isGranted) {
        if (cameraPermissionStatus.isGranted) {
            if (!hasNotifiedGrantedSession) {
                hasNotifiedGrantedSession = true
                onCameraSessionReady()
            }
        } else {
            hasNotifiedGrantedSession = false
        }
    }

    if (cameraPermissionStatus.isGranted) {
        return
    }

    val permissionLauncher = permissionRequester ?: rememberCameraPermissionRequester(
        onPermissionResult = { isGranted ->
            viewModel.onEvent(CameraPermissionEvent.SystemPermissionResult(isGranted))
        }
    )

    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onEvent(
            CameraPermissionEvent.SessionEntered(
                shouldShowRationale = cameraPermissionStatus.shouldShowRationale
            )
        )
    }

    LaunchedEffect(cameraPermissionStatus.shouldShowRationale) {
        viewModel.onEvent(
            CameraPermissionEvent.RationaleStatusChanged(
                shouldShowRationale = cameraPermissionStatus.shouldShowRationale
            )
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                CameraPermissionAction.RequestSystemPermission ->
                    permissionLauncher.request { isGranted ->
                        viewModel.onEvent(CameraPermissionEvent.SystemPermissionResult(isGranted))
                    }

                CameraPermissionAction.EnterCameraSession ->
                    onCameraSessionReady()

                CameraPermissionAction.EnterAudioOnlySession ->
                    onAudioOnlySession()
            }
        }
    }

    CameraPermissionRationaleScreen(
        state = state,
        onContinue = { viewModel.onEvent(CameraPermissionEvent.ContinueClicked) },
        onUseAudioOnly = { viewModel.onEvent(CameraPermissionEvent.UseAudioOnlyClicked) },
        modifier = modifier
    )
}

@Composable
private fun rememberCameraPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): CameraPermissionRequester {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    return remember(launcher) {
        CameraPermissionRequester { _ ->
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun CameraPermissionRationaleScreen(
    state: CameraPermissionState,
    onContinue: () -> Unit,
    onUseAudioOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(CoreUiR.dimen.space_xl)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(dimensionResource(CoreUiR.dimen.space_lg)),
            tonalElevation = dimensionResource(CoreUiR.dimen.space_sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(CoreUiR.dimen.space_lg)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))

                Text(
                    text = if (state.isRationaleRequired) {
                        stringResource(R.string.camera_permission_rationale_body)
                    } else {
                        stringResource(R.string.camera_permission_body)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )

                if (state.isAudioOnlyFallbackVisible) {
                    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_lg)))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.camera_permission_audio_only_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(dimensionResource(CoreUiR.dimen.space_md))
                        )
                    }
                }

                if (state.isPermissionRequestPending) {
                    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))
                    Text(
                        text = stringResource(R.string.camera_permission_waiting_for_prompt),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_lg)))

                Button(
                    onClick = onContinue,
                    enabled = !state.isPermissionRequestPending,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.camera_permission_continue))
                }

                Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))

                OutlinedButton(
                    onClick = onUseAudioOnly,
                    enabled = !state.isPermissionRequestPending,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(text = stringResource(R.string.camera_permission_audio_only))
                }
            }
        }
    }
}

@Composable
private fun rememberCameraPermissionStatus(context: Context): CameraPermissionStatus {
    val activity = context.findActivity()
    val isGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    val shouldShowRationale =
        activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } == true

    return CameraPermissionStatus(
        isGranted = isGranted,
        shouldShowRationale = shouldShowRationale
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun CameraPermissionRationaleScreenPreview() {
    FitnessAppTheme {
        CameraPermissionRationaleScreen(
            state = CameraPermissionState(isRationaleRequired = true),
            onContinue = {},
            onUseAudioOnly = {}
        )
    }
}
