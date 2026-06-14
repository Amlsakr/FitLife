package com.aml_sakr.fitlife.feature.auth.domain.error

import com.aml_sakr.fitlife.core.domain.DomainError

sealed class AuthError(
    override val code: String,
    override val message: String
) : DomainError {
    data object InvalidEmail : AuthError(
        code = "auth_invalid_email",
        message = "The email address is invalid."
    )

    data object WeakPassword : AuthError(
        code = "auth_weak_password",
        message = "The password does not meet the minimum requirements."
    )

    data object EmailAlreadyInUse : AuthError(
        code = "auth_email_already_in_use",
        message = "An account already exists for this email address."
    )

    data object InvalidCredentials : AuthError(
        code = "auth_invalid_credentials",
        message = "The supplied credentials are invalid."
    )

    data object UserDisabled : AuthError(
        code = "auth_user_disabled",
        message = "This account is disabled."
    )

    data object TooManyRequests : AuthError(
        code = "auth_too_many_requests",
        message = "Too many authentication attempts."
    )

    data object NetworkUnavailable : AuthError(
        code = "auth_network_unavailable",
        message = "Authentication could not reach the network."
    )

    data object NoAuthenticatedUser : AuthError(
        code = "auth_no_authenticated_user",
        message = "No authenticated user is available."
    )

    data object VerificationEmailFailed : AuthError(
        code = "auth_verification_email_failed",
        message = "The verification email could not be sent."
    )

    data object Unknown : AuthError(
        code = "auth_unknown",
        message = "Authentication failed unexpectedly."
    )
}
