package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class FirebaseAuthRepositoryTest {
    @Test
    fun signUp_createsUser_sendsVerification_andReturnsUnverifiedUser() = runTest {
        val dataSource = FakeFirebaseAuthDataSource(
            createdUser = FirebaseAuthUserData(
                id = "user-1",
                email = "amal@example.com",
                isEmailVerified = true
            )
        )
        val repository = FirebaseAuthRepository(dataSource)

        val result = repository.signUp("amal@example.com", "secret1")

        assertEquals(
            Result.Success(
                com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser(
                    id = "user-1",
                    email = "amal@example.com",
                    isEmailVerified = false
                )
            ),
            result
        )
        assertEquals("amal@example.com" to "secret1", dataSource.createRequest)
        assertEquals(1, dataSource.verificationCount)
    }

    @Test
    fun signUp_returnsVerificationFailure_whenAccountExistsButEmailCannotBeSent() = runTest {
        val dataSource = FakeFirebaseAuthDataSource(
            createdUser = FirebaseAuthUserData("user-1", "amal@example.com", false),
            verificationFailure = IllegalStateException("mail unavailable")
        )
        val repository = FirebaseAuthRepository(dataSource)

        val result = repository.signUp("amal@example.com", "secret1")

        assertEquals(Result.Failure(AuthError.VerificationEmailFailed), result)
    }

    @Test
    fun signIn_mapsFirebaseUserToDomain() = runTest {
        val dataSource = FakeFirebaseAuthDataSource(
            signedInUser = FirebaseAuthUserData("user-1", "amal@example.com", true)
        )
        val repository = FirebaseAuthRepository(dataSource)

        val result = repository.signIn("amal@example.com", "secret1")

        assertEquals(
            Result.Success(
                com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser(
                    id = "user-1",
                    email = "amal@example.com",
                    isEmailVerified = true
                )
            ),
            result
        )
    }

    @Test
    fun refreshCurrentUser_reloadsBeforeReturningUser() = runTest {
        val dataSource = FakeFirebaseAuthDataSource(
            reloadedUser = FirebaseAuthUserData("user-1", "amal@example.com", true)
        )
        val repository = FirebaseAuthRepository(dataSource)

        val result = repository.refreshCurrentUser()

        assertEquals(true, (result as Result.Success).value?.isEmailVerified)
        assertEquals(1, dataSource.reloadCount)
    }

    @Test
    fun signOut_clearsFirebaseSession() = runTest {
        val dataSource = FakeFirebaseAuthDataSource()
        val repository = FirebaseAuthRepository(dataSource)

        assertEquals(Result.Success(Unit), repository.signOut())
        assertEquals(1, dataSource.signOutCount)
    }

    @Test
    fun sendVerification_returnsNoUser_whenNoSessionExists() = runTest {
        val dataSource = FakeFirebaseAuthDataSource(
            verificationFailure = NoAuthenticatedFirebaseUserException
        )
        val repository = FirebaseAuthRepository(dataSource)

        assertEquals(
            Result.Failure(AuthError.NoAuthenticatedUser),
            repository.sendEmailVerification()
        )
    }

    @Test
    fun repository_mapsAuthErrorCodes() {
        val cases = listOf(
            "ERROR_INVALID_EMAIL" to AuthError.InvalidEmail,
            "ERROR_WEAK_PASSWORD" to AuthError.WeakPassword,
            "ERROR_EMAIL_ALREADY_IN_USE" to AuthError.EmailAlreadyInUse,
            "ERROR_INVALID_CREDENTIAL" to AuthError.InvalidCredentials,
            "ERROR_INVALID_LOGIN_CREDENTIALS" to AuthError.InvalidCredentials,
            "ERROR_WRONG_PASSWORD" to AuthError.InvalidCredentials,
            "ERROR_USER_NOT_FOUND" to AuthError.InvalidCredentials,
            "ERROR_USER_DISABLED" to AuthError.UserDisabled,
            "ERROR_TOO_MANY_REQUESTS" to AuthError.TooManyRequests,
            "ERROR_NETWORK_REQUEST_FAILED" to AuthError.NetworkUnavailable
        )

        cases.forEach { (code, expected) ->
            assertEquals(
                "Expected $code to map to ${expected.code}",
                expected,
                FirebaseAuthExceptionMapper.mapCode(code)
            )
        }

        assertEquals(AuthError.Unknown, FirebaseAuthExceptionMapper.mapCode(null))
        assertEquals(AuthError.Unknown, FirebaseAuthExceptionMapper.mapCode("ERROR_UNEXPECTED"))
        assertEquals(
            AuthError.NetworkUnavailable,
            FirebaseAuthExceptionMapper.mapFailure(
                isNetworkFailure = true,
                errorCode = null
            )
        )
    }

    @Test
    fun repository_rethrowsCancellation() = runTest {
        val cancellation = CancellationException("cancelled")
        val repository = FirebaseAuthRepository(
            FakeFirebaseAuthDataSource(signInFailure = cancellation)
        )

        try {
            repository.signIn("amal@example.com", "secret1")
            fail("Expected cancellation")
        } catch (actual: CancellationException) {
            assertEquals(cancellation, actual)
        }
    }

    private class FakeFirebaseAuthDataSource(
        private val createdUser: FirebaseAuthUserData? = null,
        private val signedInUser: FirebaseAuthUserData? = null,
        private val currentUser: FirebaseAuthUserData? = null,
        private val reloadedUser: FirebaseAuthUserData? = null,
        private val createFailure: Throwable? = null,
        private val signInFailure: Throwable? = null,
        private val signOutFailure: Throwable? = null,
        private val verificationFailure: Throwable? = null,
        private val reloadFailure: Throwable? = null
    ) : FirebaseAuthDataSource {
        var createRequest: Pair<String, String>? = null
        var verificationCount = 0
        var reloadCount = 0
        var signOutCount = 0

        override suspend fun createUser(
            email: String,
            password: String
        ): FirebaseAuthUserData? {
            createFailure?.let { throw it }
            createRequest = email to password
            return createdUser
        }

        override suspend fun signIn(
            email: String,
            password: String
        ): FirebaseAuthUserData? {
            signInFailure?.let { throw it }
            return signedInUser
        }

        override fun signOut() {
            signOutFailure?.let { throw it }
            signOutCount += 1
        }

        override fun currentUser(): FirebaseAuthUserData? = currentUser

        override suspend fun sendEmailVerification() {
            verificationCount += 1
            verificationFailure?.let { throw it }
        }

        override suspend fun reloadCurrentUser(): FirebaseAuthUserData? {
            reloadCount += 1
            reloadFailure?.let { throw it }
            return reloadedUser
        }
    }
}
