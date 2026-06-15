package com.aml_sakr.fitlife.feature.auth.auth_ui.signup

import androidx.compose.runtime.Composable
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthCredentialForm
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthScreenHeader
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

@Composable
internal fun SignUpScreenContent(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    AuthScreenHeader(
        titleResId = R.string.auth_create_your_account,
        descriptionResId = R.string.auth_sign_up_description
    )
    AuthCredentialForm(
        state = state,
        onEvent = onEvent,
        isSignUp = true,
        primaryButtonTextResId = R.string.auth_create_account_button,
        promptTextResId = R.string.auth_already_have_an_account,
        actionTextResId = R.string.auth_sign_in_button
    )
}
