package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.data.AuthDataConstants
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class FirebaseAuthRepository @Inject internal constructor(
    private val dataSource: FirebaseAuthDataSource,
    private val userDocumentDataSource: FirebaseUserDocumentDataSource,
    private val googleCredentialStateDataSource: GoogleCredentialStateDataSource
) : AuthRepository {
    override suspend fun signUp(
        email: String,
        password: String
    ): Result<AuthUser, AuthError> {
        val createdUser = try {
            dataSource.createUser(email, password)?.toDomain()
                ?: return Result.Failure(AuthError.Unknown)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            return Result.Failure(FirebaseAuthExceptionMapper.map(throwable))
        }

        try {
            dataSource.sendEmailVerification()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            return Result.Failure(AuthError.VerificationEmailFailed)
        }

        return Result.Success(createdUser.copy(isEmailVerified = false))
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser, AuthError> = authCall {
        dataSource.signIn(email, password)?.toDomain()
            ?: throw IllegalStateException(AuthDataConstants.ExceptionMessages.NO_AUTHENTICATED_USER)
    }

    override suspend fun signInWithGoogle(
        googleIdToken: String
    ): Result<AuthUser, AuthError> {
        val signedInUser = when (val result = authCall {
            dataSource.signInWithGoogle(googleIdToken)?.toDomain()
                ?: throw IllegalStateException(
                    AuthDataConstants.ExceptionMessages.NO_AUTHENTICATED_USER
                )
        }) {
            is Result.Failure -> return result
            is Result.Success -> result.value
        }

        return try {
            userDocumentDataSource.upsertAuthenticatedUser(signedInUser.toData())
            Result.Success(signedInUser)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.Failure(FirebaseAuthExceptionMapper.mapGoogleAccountSetupFailure(throwable))
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit, AuthError> = authCall {
        dataSource.sendPasswordResetEmail(email)
    }

    override suspend fun deleteAccount(): Result<Unit, AuthError> {
        val authenticatedUser = when (val result = currentUser()) {
            is Result.Failure -> return Result.Failure(result.error)
            is Result.Success -> result.value ?: return Result.Failure(AuthError.NoAuthenticatedUser)
        }

        return try {
            userDocumentDataSource.deleteAuthenticatedUser(authenticatedUser.id)
            dataSource.deleteCurrentUser()
            Result.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.Failure(FirebaseAuthExceptionMapper.map(throwable))
        }
    }

    override suspend fun signOut(): Result<Unit, AuthError> {
        val result = authCall {
            dataSource.signOut()
        }
        if (result is Result.Success) {
            try {
                googleCredentialStateDataSource.clearCredentialState()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // Best effort only: Firebase sign-out already succeeded, so do not
                // turn a credential cleanup issue into a user-visible auth failure.
            }
        }
        return result
    }

    override suspend fun currentUser(): Result<AuthUser?, AuthError> = authCall {
        dataSource.currentUser()?.toDomain()
    }

    override suspend fun sendEmailVerification(): Result<Unit, AuthError> = authCall {
        dataSource.sendEmailVerification()
    }

    override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> = authCall {
        dataSource.reloadCurrentUser()?.toDomain()
    }

    private suspend fun <T> authCall(block: suspend () -> T): Result<T, AuthError> =
        try {
            Result.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.Failure(FirebaseAuthExceptionMapper.map(throwable))
        }

    private fun FirebaseAuthUserData.toDomain(): AuthUser {
        if (id.isBlank()) {
            throw IllegalStateException(AuthDataConstants.ExceptionMessages.BLANK_USER_ID)
        }
        return AuthUser(
            id = id,
            email = email,
            isEmailVerified = isEmailVerified
        )
    }

    private fun AuthUser.toData(): FirebaseAuthUserData =
        FirebaseAuthUserData(
            id = id,
            email = email,
            isEmailVerified = isEmailVerified
        )
}
