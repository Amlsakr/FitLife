package com.aml_sakr.fitlife.feature.auth.auth_ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.aml_sakr.fitlife.feature.auth.auth_ui.signin.SignInScreenContent
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.signup.SignUpScreenContent
import com.aml_sakr.fitlife.feature.auth.auth_ui.verification.VerificationScreenContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onAuthenticated: suspend () -> Unit,
    googleClientId: String,
    googleSignInLauncher: GoogleSignInLauncher = DefaultGoogleSignInLauncher,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                AuthAction.NavigateToAuthenticatedUser -> {
                    var retryNavigation = true
                    while (retryNavigation) {
                        retryNavigation = try {
                            onAuthenticated()
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
                    snackbarHostState.showSnackbar(context.getString(action.messageResId))
            }
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
                AuthMode.VerifyEmail -> VerificationScreenContent(state = state, onEvent = onEvent)
            }
        }
    }
}

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
