package com.aml_sakr.fitlife.feature.auth.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreSecurityRulesEmulatorTest {

    private lateinit var context: Context
    private lateinit var firebaseApp: FirebaseApp
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appName: String

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        appName = "rules-${UUID.randomUUID()}"
        firebaseApp = FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setProjectId(FIREBASE_PROJECT_ID)
                .setApplicationId("1:962077229749:android:firestore-rules-test")
                .setApiKey("firestore-rules-api-key")
                .build(),
            appName
        ) ?: error("FirebaseApp initialization failed")
        auth = FirebaseAuth.getInstance(firebaseApp).apply {
            useEmulator(resolveEmulatorHost(), AUTH_EMULATOR_PORT)
            signOut()
        }
        firestore = FirebaseFirestore.getInstance(firebaseApp).apply {
            useEmulator(resolveEmulatorHost(), FIRESTORE_EMULATOR_PORT)
        }
    }

    @After
    fun tearDown() {
        runCatching { auth.signOut() }
    }

    @Test
    fun ownerCanReadWriteUpdateAndDeleteOwnFirestorePaths() {
        runBlocking {
            val owner = createUser("owner")
            signIn(owner.email, owner.password)

            val userDoc = usersCollection.document(owner.uid)
            userDoc.set(
                mapOf(
                    "id" to owner.uid,
                    "email" to owner.email,
                    "isEmailVerified" to true
                ),
                SetOptions.merge()
            ).await()

            val workoutPlanDoc = userDoc.collection("workoutPlans").document("plan-1")
            workoutPlanDoc.set(
                mapOf(
                    "planId" to "plan-1",
                    "userId" to owner.uid,
                    "title" to "Strength Plan"
                ),
                SetOptions.merge()
            ).await()

            val sessionDoc = userDoc.collection("sessions").document("session-1")
            sessionDoc.set(
                mapOf(
                    "sessionId" to "session-1",
                    "userId" to owner.uid,
                    "status" to "active"
                ),
                SetOptions.merge()
            ).await()

            val progressDoc = firestore.collection(PROGRESS_COLLECTION).document("progress-1")
            progressDoc.set(
                mapOf(
                    "progressId" to "progress-1",
                    "userId" to owner.uid,
                    "weeklyCalories" to 1200L
                ),
                SetOptions.merge()
            ).await()

            assertEquals(owner.uid, userDoc.get().await().getString("id"))
            assertEquals(owner.uid, workoutPlanDoc.get().await().getString("userId"))
            assertEquals(owner.uid, sessionDoc.get().await().getString("userId"))
            assertEquals(owner.uid, progressDoc.get().await().getString("userId"))

            progressDoc.update("weeklyCalories", 1500L).await()
            assertEquals(1500L, progressDoc.get().await().getLong("weeklyCalories"))

            progressDoc.delete().await()

            sessionDoc.delete().await()
        }
    }

    @Test
    fun unauthenticatedWritesAndCrossUserAccessAreDenied() {
        runBlocking {
            val owner = createUser("owner")
            val other = createUser("other")

            signIn(owner.email, owner.password)
            val ownerUserDoc = usersCollection.document(owner.uid)
            ownerUserDoc.set(
                mapOf(
                    "id" to owner.uid,
                    "email" to owner.email,
                    "isEmailVerified" to true
                ),
                SetOptions.merge()
            ).await()

            assertPermissionDenied {
                usersCollection.document(other.uid)
                    .set(
                        mapOf(
                            "id" to other.uid,
                            "email" to other.email,
                            "isEmailVerified" to false
                        ),
                        SetOptions.merge()
                    )
                    .await()
            }

            assertPermissionDenied {
                usersCollection.document(other.uid)
                    .collection("workoutPlans")
                    .document("plan-1")
                    .set(
                        mapOf(
                            "planId" to "plan-1",
                            "userId" to other.uid
                        )
                    )
                    .await()
            }

            assertPermissionDenied {
                usersCollection.document(other.uid)
                    .collection("sessions")
                    .document("session-1")
                    .set(
                        mapOf(
                            "sessionId" to "session-1",
                            "userId" to other.uid
                        )
                    )
                    .await()
            }

            assertPermissionDenied {
                firestore.collection(PROGRESS_COLLECTION)
                    .document("progress-denied")
                    .set(
                        mapOf(
                            "progressId" to "progress-denied",
                            "userId" to other.uid,
                            "weeklyCalories" to 800L
                        )
                    )
                    .await()
            }

            signOut()

            assertPermissionDenied {
                ownerUserDoc
                    .set(
                        mapOf(
                            "id" to owner.uid,
                            "email" to owner.email,
                            "isEmailVerified" to true
                        )
                    )
                    .await()
            }

            assertPermissionDenied {
                ownerUserDoc.get().await()
            }

            assertPermissionDenied {
                ownerUserDoc.collection("sessions")
                    .document("session-unauth")
                    .get()
                    .await()
            }
        }
    }

    @Test
    fun progressDocumentMustKeepUserIdAlignedWithAuthenticatedOwner() {
        runBlocking {
            val owner = createUser("owner")
            signIn(owner.email, owner.password)
            val progressDocId = "progress-guard-${UUID.randomUUID()}"

            val progressDoc = firestore.collection(PROGRESS_COLLECTION).document(progressDocId)
            progressDoc.set(
                mapOf(
                    "progressId" to progressDocId,
                    "userId" to owner.uid,
                    "weeklyCalories" to 900L
                )
            ).await()

            assertEquals(owner.uid, progressDoc.get().await().getString("userId"))

            assertPermissionDenied {
                progressDoc.update("userId", "someone-else").await()
            }

            assertPermissionDenied {
                firestore.collection(PROGRESS_COLLECTION)
                    .document("progress-foreign-${UUID.randomUUID()}")
                    .set(
                        mapOf(
                            "progressId" to "progress-foreign",
                            "userId" to "someone-else",
                            "weeklyCalories" to 1000L
                        )
                    )
                    .await()
            }
        }
    }

    private suspend fun createUser(prefix: String): AuthUserSeed {
        val email = "$prefix-${UUID.randomUUID()}@example.com"
        val password = "FitLife-test-123"
        auth.createUserWithEmailAndPassword(email, password).await()
        val currentUser = requireNotNull(auth.currentUser)
        val seed = AuthUserSeed(currentUser.uid, email, password)
        auth.signOut()
        return seed
    }

    private suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    private fun signOut() {
        auth.signOut()
    }

    private suspend fun assertPermissionDenied(block: suspend () -> Unit) {
        val failure = runCatching { block() }.exceptionOrNull()
        assertNotNull("Expected Firestore permission denial", failure)
        assertTrue(
            "Expected a Firestore exception but got ${failure!!.javaClass.name}",
            failure is FirebaseFirestoreException
        )
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (failure as FirebaseFirestoreException).code
        )
    }

    private val usersCollection get() = firestore.collection(USERS_COLLECTION)

    private data class AuthUserSeed(
        val uid: String,
        val email: String,
        val password: String
    )

    private companion object {
        private const val FIREBASE_PROJECT_ID = "fitlife-1fdd1"
        private const val AUTH_EMULATOR_PORT = 9099
        private const val FIRESTORE_EMULATOR_PORT = 8080
        private const val USERS_COLLECTION = "users"
        private const val PROGRESS_COLLECTION = "progress"

        fun resolveEmulatorHost(): String {
            val args = InstrumentationRegistry.getArguments()
            val override = args.getString("fitlifeFirebaseHost")
                ?: System.getProperty("fitlife.firebase.host")
            if (!override.isNullOrBlank()) {
                return override
            }
            return "10.0.2.2"
        }
    }
}
