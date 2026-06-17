package com.aml_sakr.fitlife.feature.auth.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aml_sakr.fitlife.core.data.purge.UserDataPurgeCoordinator
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRemoteDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRepository
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseFirestoreOwnedUserDataArchiveDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseFirestoreUserDataPurgeContributor
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseUserDocumentRemoteDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.GoogleCredentialStateDataSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthEmulatorInstrumentedTest {
    @Test
    fun deleteAccount_purgesFirestoreAndAuthUserData_usesAuthEmulator() = runBlocking {
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
        val firestore = FirebaseFirestore.getInstance(firebaseApp).apply {
            useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
        }
        val repository = FirebaseAuthRepository(
            FirebaseAuthRemoteDataSource(firebaseAuth),
            FirebaseUserDocumentRemoteDataSource(firestore),
            UserDataPurgeCoordinator(
                setOf(FirebaseFirestoreUserDataPurgeContributor(firestore))
            ),
            FirebaseFirestoreOwnedUserDataArchiveDataSource(firestore),
            NoOpGoogleCredentialStateDataSource
        )
        val email = "auth001-${UUID.randomUUID()}@example.com"
        val password = "FitLife-test-123"

        try {
            val signUpResult = repository.signUp(email, password)
            assertTrue(signUpResult is Result.Success)
            val createdUser = (signUpResult as Result.Success).value
            val idToken = requireNotNull(firebaseAuth.currentUser)
                .getIdToken(true)
                .await()
                .token

            seedOwnedData(firestore, createdUser.id)
            assertDocumentResponse("users/${createdUser.id}", idToken, 200)
            assertDocumentResponse(
                "users/${createdUser.id}/$WORKOUT_PLANS_COLLECTION/plan-1",
                idToken,
                200
            )
            assertDocumentResponse(
                "users/${createdUser.id}/$SESSIONS_COLLECTION/session-1",
                idToken,
                200
            )
            assertDocumentResponse("$PROGRESS_COLLECTION/progress-1", idToken, 200)

            assertEquals(Result.Success(Unit), repository.deleteAccount())

            assertEquals(Result.Success(null), repository.currentUser())
            assertDocumentResponse("users/${createdUser.id}", idToken, 403, 404)
            assertDocumentResponse(
                "users/${createdUser.id}/$WORKOUT_PLANS_COLLECTION/plan-1",
                idToken,
                403,
                404
            )
            assertDocumentResponse(
                "users/${createdUser.id}/$SESSIONS_COLLECTION/session-1",
                idToken,
                403,
                404
            )
            assertDocumentResponse("$PROGRESS_COLLECTION/progress-1", idToken, 403, 404)
        } finally {
            runCatching { firebaseAuth.signOut() }
            runCatching { firebaseApp.delete() }
        }
    }

    private suspend fun seedOwnedData(
        firestore: FirebaseFirestore,
        userId: String
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .set(
                mapOf(
                    "id" to userId,
                    "email" to "seed@example.com",
                    "isEmailVerified" to true
                ),
                SetOptions.merge()
            )
            .await()

        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(WORKOUT_PLANS_COLLECTION)
            .document("plan-1")
            .set(
                mapOf(
                    "planId" to "plan-1",
                    "userId" to userId
                ),
                SetOptions.merge()
            )
            .await()

        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(SESSIONS_COLLECTION)
            .document("session-1")
            .set(
                mapOf(
                    "sessionId" to "session-1",
                    "userId" to userId
                ),
                SetOptions.merge()
            )
            .await()

        firestore.collection(PROGRESS_COLLECTION)
            .document("progress-1")
            .set(
                mapOf(
                    "progressId" to "progress-1",
                    PROGRESS_USER_ID_FIELD to userId
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun assertDocumentResponse(
        documentPath: String,
        idToken: String?,
        vararg expectedCodes: Int
    ) {
        val connection = URL(
            "http://$EMULATOR_HOST:$FIRESTORE_PORT/v1/projects/$PROJECT_ID/databases/(default)/documents/$documentPath"
        ).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        if (!idToken.isNullOrBlank()) {
            connection.setRequestProperty("Authorization", "Bearer $idToken")
        }

        try {
            assertTrue(expectedCodes.contains(connection.responseCode))
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val PROJECT_ID = "fitlife-1fdd1"
        const val EMULATOR_HOST = "10.0.2.2"
        const val EMULATOR_PORT = 9099
        const val FIRESTORE_PORT = 8080
        const val USERS_COLLECTION = "users"
        const val WORKOUT_PLANS_COLLECTION = "workoutPlans"
        const val SESSIONS_COLLECTION = "sessions"
        const val PROGRESS_COLLECTION = "progress"
        const val PROGRESS_USER_ID_FIELD = "userId"
    }

    private object NoOpGoogleCredentialStateDataSource : GoogleCredentialStateDataSource {
        override suspend fun clearCredentialState() = Unit
    }
}
