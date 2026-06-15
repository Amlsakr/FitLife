package com.aml_sakr.fitlife.feature.auth.auth_ui.signin

import androidx.compose.runtime.Composable
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthCredentialForm
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthScreenHeader
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

@Composable
internal fun SignInScreenContent(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    AuthScreenHeader(
        titleResId = R.string.auth_welcome_back,
        descriptionResId = R.string.auth_sign_in_description
    )
    AuthCredentialForm(
        state = state,
        onEvent = onEvent,
        isSignUp = false,
        primaryButtonTextResId = R.string.auth_sign_in_button,
        promptTextResId = R.string.auth_new_to_fitlife,
        actionTextResId = R.string.auth_register_button
    )
}
