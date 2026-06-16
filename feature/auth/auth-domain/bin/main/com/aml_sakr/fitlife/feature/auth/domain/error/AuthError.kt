package com.aml_sakr.fitlife.feature.auth.domain.error

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.feature.auth.domain.AuthDomainConstants

sealed class AuthError(
    override val code: String,
    override val message: String
) : DomainError {
    data object InvalidEmail : AuthError(
        code = AuthDomainConstants.ErrorCodes.INVALID_EMAIL,
        message = AuthDomainConstants.ErrorMessages.INVALID_EMAIL
    )

    data object WeakPassword : AuthError(
        code = AuthDomainConstants.ErrorCodes.WEAK_PASSWORD,
        message = AuthDomainConstants.ErrorMessages.WEAK_PASSWORD
    )

    data object EmailAlreadyInUse : AuthError(
        code = AuthDomainConstants.ErrorCodes.EMAIL_ALREADY_IN_USE,
        message = AuthDomainConstants.ErrorMessages.EMAIL_ALREADY_IN_USE
    )

    data object InvalidCredentials : AuthError(
        code = AuthDomainConstants.ErrorCodes.INVALID_CREDENTIALS,
        message = AuthDomainConstants.ErrorMessages.INVALID_CREDENTIALS
    )

    data object UserDisabled : AuthError(
        code = AuthDomainConstants.ErrorCodes.USER_DISABLED,
        message = AuthDomainConstants.ErrorMessages.USER_DISABLED
    )

    data object TooManyRequests : AuthError(
        code = AuthDomainConstants.ErrorCodes.TOO_MANY_REQUESTS,
        message = AuthDomainConstants.ErrorMessages.TOO_MANY_REQUESTS
    )

    data object NetworkUnavailable : AuthError(
        code = AuthDomainConstants.ErrorCodes.NETWORK_UNAVAILABLE,
        message = AuthDomainConstants.ErrorMessages.NETWORK_UNAVAILABLE
    )

    data object NoAuthenticatedUser : AuthError(
        code = AuthDomainConstants.ErrorCodes.NO_AUTHENTICATED_USER,
        message = AuthDomainConstants.ErrorMessages.NO_AUTHENTICATED_USER
    )

    data object VerificationEmailFailed : AuthError(
        code = AuthDomainConstants.ErrorCodes.VERIFICATION_EMAIL_FAILED,
        message = AuthDomainConstants.ErrorMessages.VERIFICATION_EMAIL_FAILED
    )

    data object Unknown : AuthError(
        code = AuthDomainConstants.ErrorCodes.UNKNOWN,
        message = AuthDomainConstants.ErrorMessages.UNKNOWN
    )
}
