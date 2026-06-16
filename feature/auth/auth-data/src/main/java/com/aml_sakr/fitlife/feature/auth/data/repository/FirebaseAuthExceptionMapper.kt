package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.feature.auth.data.AuthDataConstants
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

    fun mapGoogleAccountSetupFailure(throwable: Throwable): AuthError {
        if (throwable === NoAuthenticatedFirebaseUserException) {
            return AuthError.GoogleAccountSetupFailed
        }
        return when {
            throwable is FirebaseNetworkException -> AuthError.GoogleAccountSetupFailed
            throwable is FirebaseAuthException -> AuthError.GoogleAccountSetupFailed
            else -> AuthError.GoogleAccountSetupFailed
        }
    }

    internal fun mapFailure(
        isNetworkFailure: Boolean,
        errorCode: String?
    ): AuthError =
        if (isNetworkFailure) AuthError.NetworkUnavailable else mapCode(errorCode)

    fun mapCode(errorCode: String?): AuthError =
        when (errorCode) {
            AuthDataConstants.FirebaseAuthErrorCodes.INVALID_EMAIL -> AuthError.InvalidEmail
            AuthDataConstants.FirebaseAuthErrorCodes.WEAK_PASSWORD -> AuthError.WeakPassword
            AuthDataConstants.FirebaseAuthErrorCodes.EMAIL_ALREADY_IN_USE -> AuthError.EmailAlreadyInUse
            AuthDataConstants.FirebaseAuthErrorCodes.INVALID_CREDENTIAL,
            AuthDataConstants.FirebaseAuthErrorCodes.INVALID_LOGIN_CREDENTIALS,
            AuthDataConstants.FirebaseAuthErrorCodes.WRONG_PASSWORD,
            AuthDataConstants.FirebaseAuthErrorCodes.USER_NOT_FOUND -> AuthError.InvalidCredentials
            AuthDataConstants.FirebaseAuthErrorCodes.USER_DISABLED -> AuthError.UserDisabled
            AuthDataConstants.FirebaseAuthErrorCodes.TOO_MANY_REQUESTS -> AuthError.TooManyRequests
            AuthDataConstants.FirebaseAuthErrorCodes.NETWORK_REQUEST_FAILED -> AuthError.NetworkUnavailable
            else -> AuthError.Unknown
        }
}
