package com.aml_sakr.fitlife.feature.auth.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUseCasesTest {
    private val user = AuthUser(
        id = "user-1",
        email = "amal@example.com",
        isEmailVerified = false
    )

    @Test
    fun signUp_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(signUpResult = Result.Success(user))

        val result = SignUpUseCase(repository)("amal@example.com", "secret1")

        assertEquals(Result.Success(user), result)
        assertEquals("amal@example.com" to "secret1", repository.signUpRequest)
    }

    @Test
    fun signIn_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(signInResult = Result.Success(user))

        val result = SignInUseCase(repository)("amal@example.com", "secret1")

        assertEquals(Result.Success(user), result)
        assertEquals("amal@example.com" to "secret1", repository.signInRequest)
    }

    @Test
    fun signInWithGoogle_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(googleSignInResult = Result.Success(user))

        val result = SignInWithGoogleUseCase(repository)("google-id-token")

        assertEquals(Result.Success(user), result)
        assertEquals("google-id-token", repository.googleSignInRequest)
    }

    @Test
    fun signOut_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(signOutResult = Result.Success(Unit))

        assertEquals(Result.Success(Unit), SignOutUseCase(repository)())
        assertEquals(1, repository.signOutCount)
    }

    @Test
    fun getCurrentUser_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(currentUserResult = Result.Success(user))

        assertEquals(Result.Success(user), GetCurrentUserUseCase(repository)())
    }

    @Test
    fun resendVerification_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository(verificationResult = Result.Success(Unit))

        assertEquals(Result.Success(Unit), ResendEmailVerificationUseCase(repository)())
        assertEquals(1, repository.verificationCount)
    }

    @Test
    fun refreshCurrentUser_delegatesToRepository() = runTest {
        val verifiedUser = user.copy(isEmailVerified = true)
        val repository = FakeAuthRepository(refreshResult = Result.Success(verifiedUser))

        assertEquals(Result.Success(verifiedUser), RefreshCurrentUserUseCase(repository)())
        assertEquals(1, repository.refreshCount)
    }

    private class FakeAuthRepository(
        private val signUpResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val signInResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val googleSignInResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val signOutResult: Result<Unit, AuthError> = Result.Failure(AuthError.Unknown),
        private val currentUserResult: Result<AuthUser?, AuthError> = Result.Success(null),
        private val verificationResult: Result<Unit, AuthError> = Result.Failure(AuthError.Unknown),
        private val refreshResult: Result<AuthUser?, AuthError> = Result.Success(null)
    ) : AuthRepository {
        var signUpRequest: Pair<String, String>? = null
        var signInRequest: Pair<String, String>? = null
        var googleSignInRequest: String? = null
        var signOutCount = 0
        var verificationCount = 0
        var refreshCount = 0

        override suspend fun signUp(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> {
            signUpRequest = email to password
            return signUpResult
        }

        override suspend fun signIn(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> {
            signInRequest = email to password
            return signInResult
        }

        override suspend fun signInWithGoogle(
            googleIdToken: String
        ): Result<AuthUser, AuthError> {
            googleSignInRequest = googleIdToken
            return googleSignInResult
        }

        override suspend fun signOut(): Result<Unit, AuthError> {
            signOutCount += 1
            return signOutResult
        }

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = currentUserResult

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> {
            verificationCount += 1
            return verificationResult
        }

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> {
            refreshCount += 1
            return refreshResult
        }
    }
}
