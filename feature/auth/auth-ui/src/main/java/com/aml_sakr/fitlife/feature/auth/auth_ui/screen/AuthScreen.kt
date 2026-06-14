package com.aml_sakr.fitlife.feature.auth.auth_ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.action.AuthAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthMode
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onAuthenticated: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                                message = context.getString(R.string.auth_message_unable_to_continue),
                                actionLabel = context.getString(R.string.auth_action_retry)
                            ) == SnackbarResult.ActionPerformed
                        }
                    }
                }
                AuthAction.NavigateToSignIn -> Unit
                is AuthAction.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(action.messageResId))
            }
        }
    }

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = dimensionResource(CoreUiR.dimen.space_xl),
                    vertical = dimensionResource(CoreUiR.dimen.space_lg)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.auth_brand_name),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))
            Text(
                text = when (state.mode) {
                    AuthMode.SignIn -> stringResource(R.string.auth_welcome_back)
                    AuthMode.SignUp -> stringResource(R.string.auth_create_your_account)
                    AuthMode.VerifyEmail -> stringResource(R.string.auth_verify_your_email)
                },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))
            Text(
                text = when (state.mode) {
                    AuthMode.SignIn -> stringResource(R.string.auth_sign_in_description)
                    AuthMode.SignUp -> stringResource(R.string.auth_sign_up_description)
                    AuthMode.VerifyEmail ->
                        stringResource(R.string.auth_verify_email_description)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_xl)))

            when (state.mode) {
                AuthMode.SignIn,
                AuthMode.SignUp -> CredentialsForm(state = state, onEvent = onEvent)
                AuthMode.VerifyEmail -> VerificationPanel(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun CredentialsForm(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    val isSignUp = state.mode == AuthMode.SignUp

    OutlinedTextField(
        value = state.email,
        onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .contentType(ContentType.EmailAddress),
        enabled = !state.isLoading,
        label = { Text(stringResource(R.string.auth_email_label)) },
        singleLine = true,
        isError = state.emailErrorResId != null,
        supportingText = { FieldError(state.emailErrorResId) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )

    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

    OutlinedTextField(
        value = state.password,
        onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .contentType(
                if (isSignUp) ContentType.NewPassword else ContentType.Password
            ),
        enabled = !state.isLoading,
        label = { Text(stringResource(R.string.auth_password_label)) },
        singleLine = true,
        isError = state.passwordErrorResId != null,
        supportingText = { FieldError(state.passwordErrorResId) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onEvent(AuthEvent.Submit) }
        )
    )

    if (isSignUp) {
        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(AuthEvent.ConfirmPasswordChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .contentType(ContentType.NewPassword),
            enabled = !state.isLoading,
            label = { Text(stringResource(R.string.auth_confirm_password_label)) },
            singleLine = true,
            isError = state.confirmPasswordErrorResId != null,
            supportingText = { FieldError(state.confirmPasswordErrorResId) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onEvent(AuthEvent.Submit) }
            )
        )
    }

    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_lg)))

    Button(
        onClick = { onEvent(AuthEvent.Submit) },
        enabled = !state.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(CoreUiR.dimen.auth_button_height))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(CoreUiR.dimen.auth_progress_indicator_size)),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = dimensionResource(CoreUiR.dimen.auth_progress_stroke_width)
            )
        } else {
            Text(
                text = if (isSignUp) {
                    stringResource(R.string.auth_create_account_button)
                } else {
                    stringResource(R.string.auth_sign_in_button)
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) {
                stringResource(R.string.auth_already_have_an_account)
            } else {
                stringResource(R.string.auth_new_to_fitlife)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = {
                onEvent(if (isSignUp) AuthEvent.ShowSignIn else AuthEvent.ShowSignUp)
            },
            enabled = !state.isLoading
        ) {
            Text(
                text = if (isSignUp) {
                    stringResource(R.string.auth_sign_in_button)
                } else {
                    stringResource(R.string.auth_register_button)
                }
            )
        }
    }
}

@Composable
private fun VerificationPanel(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    state.verificationEmail?.takeIf { it.isNotBlank() }?.let { email ->
        Text(
            text = email,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))
    }

    Text(
        text = stringResource(R.string.auth_verify_email_instruction),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_xl)))

    Button(
        onClick = { onEvent(AuthEvent.RefreshVerification) },
        enabled = !state.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(CoreUiR.dimen.auth_button_height))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(CoreUiR.dimen.auth_progress_indicator_size)),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = dimensionResource(CoreUiR.dimen.auth_progress_stroke_width)
            )
        } else {
            Text(text = stringResource(R.string.auth_verified_email_button))
        }
    }

    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

    OutlinedButton(
        onClick = { onEvent(AuthEvent.ResendVerification) },
        enabled = !state.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(CoreUiR.dimen.auth_button_height))
    ) {
        Text(text = stringResource(R.string.auth_resend_verification_email))
    }

    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))

    TextButton(
        onClick = { onEvent(AuthEvent.SignOut) },
        enabled = !state.isLoading
    ) {
        Text(text = stringResource(R.string.auth_sign_out))
    }
}

@Composable
private fun FieldError(messageResId: Int?) {
    if (messageResId != null) {
        Text(
            text = stringResource(messageResId),
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    FitnessAppTheme {
        AuthScreen(
            state = AuthState(mode = AuthMode.SignUp),
            onEvent = {}
        )
    }
}
