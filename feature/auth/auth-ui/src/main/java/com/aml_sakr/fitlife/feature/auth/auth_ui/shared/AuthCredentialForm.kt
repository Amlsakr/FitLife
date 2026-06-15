package com.aml_sakr.fitlife.feature.auth.auth_ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

@Composable
internal fun AuthCredentialForm(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    isSignUp: Boolean,
    primaryButtonTextResId: Int,
    promptTextResId: Int,
    actionTextResId: Int
) {
    Column {
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
                Text(text = stringResource(primaryButtonTextResId))
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_md)))

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(promptTextResId),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    onEvent(if (isSignUp) AuthEvent.ShowSignIn else AuthEvent.ShowSignUp)
                },
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(actionTextResId))
            }
        }
    }
}

@Composable
private fun FieldError(messageResId: Int?) {
    if (messageResId != null) {
        Text(text = stringResource(messageResId))
    }
}
