package com.aml_sakr.fitlife.feature.auth.auth_ui.verification

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

@Composable
internal fun VerificationScreenContent(
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

    TextButton(
        onClick = { onEvent(AuthEvent.DeleteAccountRequested) },
        enabled = !state.isLoading
    ) {
        Text(text = stringResource(R.string.auth_delete_account))
    }
}
