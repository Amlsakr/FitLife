package com.aml_sakr.fitlife.feature.auth.domain.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<AuthUser, AuthError>

    suspend fun signIn(email: String, password: String): Result<AuthUser, AuthError>

    suspend fun signInWithGoogle(googleIdToken: String): Result<AuthUser, AuthError>

    suspend fun resetPassword(email: String): Result<Unit, AuthError>

    suspend fun deleteAccount(): Result<Unit, AuthError>

    suspend fun signOut(): Result<Unit, AuthError>

    suspend fun currentUser(): Result<AuthUser?, AuthError>

    suspend fun sendEmailVerification(): Result<Unit, AuthError>

    suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError>
}
