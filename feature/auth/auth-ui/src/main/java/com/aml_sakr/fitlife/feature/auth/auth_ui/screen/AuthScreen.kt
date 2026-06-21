package com.aml_sakr.fitlife.feature.auth.auth_ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.AuthUiConstants
import com.aml_sakr.fitlife.feature.auth.auth_ui.action.AuthAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.DefaultGoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInResult
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthMode
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.auth_ui.signin.SignInScreenContent
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.signup.SignUpScreenContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onAuthenticated: suspend (AuthUser) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignIn: (() -> Unit)? = null,
    onNavigateToSignUp: (() -> Unit)? = null,
    onNavigateToForgotPassword: (() -> Unit)? = null,
    googleClientId: String,
    googleSignInLauncher: GoogleSignInLauncher = DefaultGoogleSignInLauncher,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessages = remember { mutableStateListOf<SnackbarMessage>() }
    var nextSnackbarMessageId by remember { mutableLongStateOf(0L) }
    val nextSnackbarMessage = snackbarMessages.firstOrNull()
    val snackbarMessage = nextSnackbarMessage?.let { stringResource(it.messageResId) }

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                is AuthAction.NavigateToAuthenticatedUser -> {
                    var retryNavigation = true
                    while (retryNavigation) {
                        retryNavigation = try {
                            onAuthenticated(action.user)
                            false
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Throwable) {
                            snackbarHostState.showSnackbar(
                                message = AuthUiConstants.CONTINUE_UNAVAILABLE_MESSAGE,
                                actionLabel = AuthUiConstants.RETRY_ACTION_LABEL
                                ) == SnackbarResult.ActionPerformed
                        }
                    }
                }
                AuthAction.NavigateToOnboarding -> onNavigateToOnboarding()
                AuthAction.NavigateToSignIn -> Unit
                AuthAction.LaunchGoogleSignIn -> {
                    coroutineScope.launch {
                        when (
                            val result = googleSignInLauncher.launch(
                                context = context,
                                googleClientId = googleClientId
                            )
                        ) {
                            is GoogleSignInResult.Token ->
                                viewModel.onEvent(
                                    AuthEvent.GoogleSignInTokenReceived(result.idToken)
                                )
                            is GoogleSignInResult.Cancelled ->
                                viewModel.onEvent(AuthEvent.GoogleSignInDismissed)
                            is GoogleSignInResult.Failed ->
                                viewModel.onEvent(
                                    AuthEvent.GoogleSignInFailed(result.error)
                                )
                        }
                    }
                }
                is AuthAction.ShowMessage ->
                    snackbarMessages.add(
                        SnackbarMessage(
                            id = nextSnackbarMessageId++,
                            messageResId = action.messageResId
                        )
                    )
            }
        }
    }

    LaunchedEffect(nextSnackbarMessage?.id) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        snackbarMessages.removeAt(0)
    }

    val routedOnEvent: (AuthEvent) -> Unit = { event ->
        when (event) {
            AuthEvent.ShowSignIn -> {
                if (onNavigateToSignIn != null) {
                    onNavigateToSignIn()
                } else {
                    viewModel.onEvent(event)
                }
            }
            AuthEvent.ShowSignUp -> {
                if (onNavigateToSignUp != null) {
                    onNavigateToSignUp()
                } else {
                    viewModel.onEvent(event)
                }
            }
            AuthEvent.ResetPasswordRequested -> {
                if (onNavigateToForgotPassword != null) {
                    onNavigateToForgotPassword()
                } else {
                    viewModel.onEvent(event)
                }
            }
            else -> viewModel.onEvent(event)
        }
    }

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        googleSignInEnabled = googleClientId.isNotBlank(),
        isGoogleSignInInProgress = state.isGoogleSignInInProgress,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    googleSignInEnabled: Boolean,
    isGoogleSignInInProgress: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (state.mode) {
                AuthMode.SignIn -> SignInScreenContent(
                    state = state,
                    onEvent = onEvent,
                    googleSignInEnabled = googleSignInEnabled,
                    isGoogleSignInInProgress = isGoogleSignInInProgress
                )
                AuthMode.SignUp -> SignUpScreenContent(
                    state = state,
                    onEvent = onEvent,
                    googleSignInEnabled = googleSignInEnabled,
                    isGoogleSignInInProgress = isGoogleSignInInProgress
                )
            }
        }
    }

    if (state.isDeleteAccountConfirmationVisible) {
        DeleteAccountConfirmationDialog(onEvent = onEvent)
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onEvent: (AuthEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AuthEvent.DeleteAccountDismissed) },
        title = {
            Text(text = stringResource(R.string.auth_delete_account_title))
        },
        text = {
            Text(text = stringResource(R.string.auth_delete_account_confirmation))
        },
        confirmButton = {
            TextButton(onClick = { onEvent(AuthEvent.DeleteAccountConfirmed) }) {
                Text(text = stringResource(R.string.auth_delete_account_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(AuthEvent.DeleteAccountDismissed) }) {
                Text(text = stringResource(R.string.auth_delete_account_cancel))
            }
        }
    )
}

private data class SnackbarMessage(
    val id: Long,
    val messageResId: Int
)

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    FitnessAppTheme {
        AuthScreen(
            state = AuthState(mode = AuthMode.SignUp),
            onEvent = {},
            googleSignInEnabled = false,
            isGoogleSignInInProgress = false
        )
    }
}
