package com.aml_sakr.fitlife.feature.auth.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRemoteDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRepository
import com.aml_sakr.fitlife.feature.auth.data.repository.GoogleCredentialStateDataSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthEmulatorInstrumentedTest {
    @Test
    fun createSignOutSignInAndSendVerification_usesAuthEmulator() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = "auth-emulator-${UUID.randomUUID()}"
        val firebaseApp = FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setApplicationId("1:962077229749:android:auth-emulator-test")
                .setApiKey("auth-emulator-api-key")
                .setProjectId(PROJECT_ID)
                .build(),
            appName
        )
        val firebaseAuth = FirebaseAuth.getInstance(firebaseApp).apply {
            useEmulator(EMULATOR_HOST, EMULATOR_PORT)
            signOut()
        }
        val repository = FirebaseAuthRepository(
            FirebaseAuthRemoteDataSource(firebaseAuth),
            NoOpFirebaseUserDocumentDataSource,
            NoOpGoogleCredentialStateDataSource
        )
        val email = "auth001-${UUID.randomUUID()}@example.com"
        val password = "FitLife-test-123"

        try {
            val signUpResult = repository.signUp(email, password)
            assertTrue(signUpResult is Result.Success)
            val createdUser = (signUpResult as Result.Success).value

            assertEquals(email, createdUser.email)
            assertEquals(false, createdUser.isEmailVerified)

            assertEquals(Result.Success(Unit), repository.signOut())
            assertEquals(Result.Success(null), repository.currentUser())

            val signInResult = repository.signIn(email, password)
            assertTrue(signInResult is Result.Success)
            assertEquals(createdUser.id, (signInResult as Result.Success).value.id)

            assertEquals(Result.Success(Unit), repository.sendEmailVerification())
        } finally {
            if (firebaseAuth.currentUser == null) {
                runCatching {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                }
            }
            runCatching { firebaseAuth.currentUser?.delete()?.await() }
            firebaseAuth.signOut()
            firebaseApp.delete()
        }
    }

    private companion object {
        const val PROJECT_ID = "fitlife-1fdd1"
        const val EMULATOR_HOST = "10.0.2.2"
        const val EMULATOR_PORT = 9099
    }

    private object NoOpFirebaseUserDocumentDataSource :
        com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseUserDocumentDataSource {
        override suspend fun upsertAuthenticatedUser(
            user: com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthUserData
        ) = Unit
    }

    private object NoOpGoogleCredentialStateDataSource : GoogleCredentialStateDataSource {
        override suspend fun clearCredentialState() = Unit
    }
}
