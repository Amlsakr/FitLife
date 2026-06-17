package com.aml_sakr.fitlife.feature.auth.data

internal object AuthDataConstants {
    internal object FirebaseAuthErrorCodes {
        const val INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        const val EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
        const val INVALID_CREDENTIAL = "ERROR_INVALID_CREDENTIAL"
        const val INVALID_LOGIN_CREDENTIALS = "ERROR_INVALID_LOGIN_CREDENTIALS"
        const val WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val USER_DISABLED = "ERROR_USER_DISABLED"
        const val TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS"
        const val NETWORK_REQUEST_FAILED = "ERROR_NETWORK_REQUEST_FAILED"
        const val REQUIRES_RECENT_LOGIN = "ERROR_REQUIRES_RECENT_LOGIN"
    }

    internal object ExceptionMessages {
        const val NO_AUTHENTICATED_USER = "Firebase returned no authenticated user"
        const val BLANK_USER_ID = "Firebase returned a blank user ID"
    }

    internal object FirestoreCollections {
        const val USERS = "users"
    }

    internal object UserDocumentFields {
        const val ID = "id"
        const val EMAIL = "email"
        const val IS_EMAIL_VERIFIED = "isEmailVerified"
    }
}
