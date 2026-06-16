package com.aml_sakr.fitlife.feature.auth.data.startup

import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthUserData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseAuthSessionReaderTest {
    @Test
    fun currentSession_returnsNull_whenFirebaseHasNoCurrentUser() = runTest {
        val reader = FirebaseAuthSessionReader(StubDataSource(user = null))

        assertNull(reader.currentSession())
    }

    @Test
    fun currentSession_mapsUidAndVerificationState() = runTest {
        val reader = FirebaseAuthSessionReader(
            StubDataSource(
                user = FirebaseAuthUserData(
                    id = "user-1",
                    email = "amal@example.com",
                    isEmailVerified = false
                )
            )
        )

        val session = reader.currentSession()

        assertEquals("user-1", session?.userId)
        assertEquals(false, session?.isEmailVerified)
    }

    private class StubDataSource(
        private val user: FirebaseAuthUserData?
    ) : FirebaseAuthDataSource {
        override suspend fun createUser(
            email: String,
            password: String
        ): FirebaseAuthUserData? = error("Not used")

        override suspend fun signIn(
            email: String,
            password: String
        ): FirebaseAuthUserData? = error("Not used")

        override suspend fun signInWithGoogle(googleIdToken: String): FirebaseAuthUserData? =
            error("Not used")

        override fun signOut() = Unit

        override fun currentUser(): FirebaseAuthUserData? = user

        override suspend fun sendEmailVerification() = Unit

        override suspend fun reloadCurrentUser(): FirebaseAuthUserData? = user
    }
}
