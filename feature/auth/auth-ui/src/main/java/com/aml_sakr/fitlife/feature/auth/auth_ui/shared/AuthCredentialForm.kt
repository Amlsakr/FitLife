package com.aml_sakr.fitlife.feature.auth.auth_ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

private val AuthAccent = androidx.compose.ui.graphics.Color(0xFF0B6FAE)
private val AuthOutline = androidx.compose.ui.graphics.Color(0xFFD6E0EB)

@Composable
internal fun AuthCredentialForm(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    isSignUp: Boolean,
    primaryButtonTextResId: Int,
    socialDividerTextResId: Int? = null,
    googleButtonTextResId: Int,
    promptTextResId: Int,
    actionTextResId: Int,
    showForgotPasswordAction: Boolean,
    googleSignInEnabled: Boolean,
    isGoogleSignInInProgress: Boolean
) {
    val isBusy = state.isLoading || isGoogleSignInInProgress
    val fieldShape = RoundedCornerShape(18.dp)

    Column {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .contentType(ContentType.EmailAddress),
            enabled = !isBusy,
            label = { Text(stringResource(R.string.auth_email_label)) },
            placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
            singleLine = true,
            isError = state.emailErrorResId != null,
            supportingText = { FieldError(state.emailErrorResId) },
            shape = fieldShape,
            colors = authFieldColors(),
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
                .contentType(if (isSignUp) ContentType.NewPassword else ContentType.Password),
            enabled = !isBusy,
            label = { Text(stringResource(R.string.auth_password_label)) },
            placeholder = { Text(stringResource(R.string.auth_password_placeholder)) },
            singleLine = true,
            isError = state.passwordErrorResId != null,
            supportingText = { FieldError(state.passwordErrorResId) },
            visualTransformation = PasswordVisualTransformation(),
            shape = fieldShape,
            colors = authFieldColors(),
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
                enabled = !isBusy,
                label = { Text(stringResource(R.string.auth_confirm_password_label)) },
                placeholder = { Text(stringResource(R.string.auth_password_placeholder)) },
                singleLine = true,
                isError = state.confirmPasswordErrorResId != null,
                supportingText = { FieldError(state.confirmPasswordErrorResId) },
                visualTransformation = PasswordVisualTransformation(),
                shape = fieldShape,
                colors = authFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onEvent(AuthEvent.Submit) }
                )
            )
        }

        if (showForgotPasswordAction) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onEvent(AuthEvent.ResetPasswordRequested) },
                    enabled = !isBusy
                ) {
                    Text(text = stringResource(R.string.auth_forgot_password))
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))
        }

        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_lg)))

        Button(
            onClick = { onEvent(AuthEvent.Submit) },
            enabled = !isBusy,
            shape = fieldShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = AuthAccent.copy(alpha = 0.36f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
            ),
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
                Text(text = stringResource(primaryButtonTextResId))
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

        socialDividerTextResId?.let { dividerTextResId ->
            DividerLabel(textResId = dividerTextResId)
            Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))
        }

        OutlinedButton(
            onClick = { onEvent(AuthEvent.GoogleSignInRequested) },
            enabled = !isBusy && googleSignInEnabled,
            shape = fieldShape,
            border = BorderStroke(1.dp, AuthOutline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(CoreUiR.dimen.auth_button_height))
        ) {
            GoogleButtonMark()
            Spacer(modifier = Modifier.width(dimensionResource(CoreUiR.dimen.space_sm)))
            Text(text = stringResource(googleButtonTextResId))
        }

        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(promptTextResId),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    onEvent(if (isSignUp) AuthEvent.ShowSignIn else AuthEvent.ShowSignUp)
                },
                enabled = !isBusy
            ) {
                Text(text = stringResource(actionTextResId))
            }
        }
    }
}

@Composable
private fun FieldError(messageResId: Int?) {
    if (messageResId != null) {
        Text(
            text = stringResource(messageResId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DividerLabel(textResId: Int) {
    Text(
        text = stringResource(textResId),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GoogleButtonMark() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "G",
                style = MaterialTheme.typography.labelLarge,
                color = AuthAccent
            )
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AuthAccent,
    unfocusedBorderColor = AuthOutline,
    focusedLabelColor = AuthAccent,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = AuthAccent,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error,
    errorCursorColor = MaterialTheme.colorScheme.error
)
