package com.aml_sakr.fitlife.feature.auth.auth_ui.event

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent

sealed interface AuthEvent : UIEvent {
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data class ConfirmPasswordChanged(val value: String) : AuthEvent
    data object Submit : AuthEvent
    data object ShowSignIn : AuthEvent
    data object ShowSignUp : AuthEvent
    data object ResendVerification : AuthEvent
    data object RefreshVerification : AuthEvent
    data object SignOut : AuthEvent
}
