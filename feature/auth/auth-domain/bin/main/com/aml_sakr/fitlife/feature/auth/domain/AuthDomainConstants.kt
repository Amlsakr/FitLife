package com.aml_sakr.fitlife.feature.auth.domain

internal object AuthDomainConstants {
    internal object ErrorCodes {
        const val INVALID_EMAIL = "auth_invalid_email"
        const val WEAK_PASSWORD = "auth_weak_password"
        const val EMAIL_ALREADY_IN_USE = "auth_email_already_in_use"
        const val INVALID_CREDENTIALS = "auth_invalid_credentials"
        const val USER_DISABLED = "auth_user_disabled"
        const val TOO_MANY_REQUESTS = "auth_too_many_requests"
        const val NETWORK_UNAVAILABLE = "auth_network_unavailable"
        const val NO_AUTHENTICATED_USER = "auth_no_authenticated_user"
        const val VERIFICATION_EMAIL_FAILED = "auth_verification_email_failed"
        const val GOOGLE_SIGN_IN_FAILED = "auth_google_sign_in_failed"
        const val GOOGLE_ACCOUNT_SETUP_FAILED = "auth_google_account_setup_failed"
        const val GOOGLE_CREDENTIAL_STATE_CLEAR_FAILED =
            "auth_google_credential_state_clear_failed"
        const val REAUTHENTICATION_REQUIRED = "auth_reauthentication_required"
        const val UNKNOWN = "auth_unknown"
    }

    internal object ErrorMessages {
        const val INVALID_EMAIL = "The email address is invalid."
        const val WEAK_PASSWORD = "The password does not meet the minimum requirements."
        const val EMAIL_ALREADY_IN_USE = "An account already exists for this email address."
        const val INVALID_CREDENTIALS = "The supplied credentials are invalid."
        const val USER_DISABLED = "This account is disabled."
        const val TOO_MANY_REQUESTS = "Too many authentication attempts."
        const val NETWORK_UNAVAILABLE = "Authentication could not reach the network."
        const val NO_AUTHENTICATED_USER = "No authenticated user is available."
        const val VERIFICATION_EMAIL_FAILED = "The verification email could not be sent."
        const val GOOGLE_SIGN_IN_FAILED = "Google sign-in failed."
        const val GOOGLE_ACCOUNT_SETUP_FAILED = "Google sign-in succeeded, but account setup failed."
        const val GOOGLE_CREDENTIAL_STATE_CLEAR_FAILED =
            "Signed out, but Google sign-in state could not be cleared."
        const val REAUTHENTICATION_REQUIRED =
            "Recent sign-in is required before this action can continue."
        const val UNKNOWN = "Authentication failed unexpectedly."
    }
}
