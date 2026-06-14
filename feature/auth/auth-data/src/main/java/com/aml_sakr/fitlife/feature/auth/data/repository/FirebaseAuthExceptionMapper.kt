package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

internal object FirebaseAuthExceptionMapper {
    fun map(throwable: Throwable): AuthError {
        if (throwable === NoAuthenticatedFirebaseUserException) {
            return AuthError.NoAuthenticatedUser
        }
        return mapFailure(
            isNetworkFailure = throwable is FirebaseNetworkException,
            errorCode = (throwable as? FirebaseAuthException)?.errorCode
        )
    }

    internal fun mapFailure(
        isNetworkFailure: Boolean,
        errorCode: String?
    ): AuthError =
        if (isNetworkFailure) AuthError.NetworkUnavailable else mapCode(errorCode)

    fun mapCode(errorCode: String?): AuthError =
        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
            "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EmailAlreadyInUse
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" -> AuthError.InvalidCredentials
            "ERROR_USER_DISABLED" -> AuthError.UserDisabled
            "ERROR_TOO_MANY_REQUESTS" -> AuthError.TooManyRequests
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NetworkUnavailable
            else -> AuthError.Unknown
        }
}
