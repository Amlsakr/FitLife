package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class FirebaseAuthRepository @Inject internal constructor(
    private val dataSource: FirebaseAuthDataSource
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
            ?: throw IllegalStateException("Firebase returned no authenticated user")
    }

    override suspend fun signOut(): Result<Unit, AuthError> = authCall {
        dataSource.signOut()
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
            throw IllegalStateException("Firebase returned a blank user ID")
        }
        return AuthUser(
            id = id,
            email = email,
            isEmailVerified = isEmailVerified
        )
    }
}
