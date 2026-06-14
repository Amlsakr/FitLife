package com.aml_sakr.fitlife.feature.auth.auth_ui.component

import androidx.annotation.StringRes
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError

internal object AuthErrorMessageMapper {
    @StringRes
    fun messageResId(error: AuthError): Int =
        when (error) {
            AuthError.InvalidEmail -> R.string.auth_error_invalid_email
            AuthError.WeakPassword -> R.string.auth_error_weak_password
            AuthError.EmailAlreadyInUse -> R.string.auth_error_email_already_in_use
            AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
            AuthError.UserDisabled -> R.string.auth_error_user_disabled
            AuthError.TooManyRequests -> R.string.auth_error_too_many_requests
            AuthError.NetworkUnavailable -> R.string.auth_error_network_unavailable
            AuthError.NoAuthenticatedUser -> R.string.auth_error_no_authenticated_user
            AuthError.VerificationEmailFailed ->
                R.string.auth_error_verification_email_failed
            AuthError.Unknown -> R.string.auth_error_unknown
        }
}
