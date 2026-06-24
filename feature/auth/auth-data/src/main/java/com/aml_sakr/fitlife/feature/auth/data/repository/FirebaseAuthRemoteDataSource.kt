package com.aml_sakr.fitlife.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseAuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthDataSource {
    override suspend fun createUser(
        email: String,
        password: String
    ): FirebaseAuthUserData? =
        firebaseAuth.createUserWithEmailAndPassword(email, password).await().user?.toData()

    override suspend fun signIn(
        email: String,
        password: String
    ): FirebaseAuthUserData? =
        firebaseAuth.signInWithEmailAndPassword(email, password).await().user?.toData()

    override suspend fun signInWithGoogle(googleIdToken: String): FirebaseAuthUserData? =
        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(googleIdToken, null)
        ).await().user?.toData()

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun currentUser(): FirebaseAuthUserData? = firebaseAuth.currentUser?.toData()

    override suspend fun sendEmailVerification() {
        val user = firebaseAuth.currentUser ?: throw NoAuthenticatedFirebaseUserException
        user.sendEmailVerification().await()
    }

    override suspend fun deleteCurrentUser() {
        val user = firebaseAuth.currentUser ?: throw NoAuthenticatedFirebaseUserException
        user.delete().await()
    }

    override suspend fun reloadCurrentUser(): FirebaseAuthUserData? {
        val user = firebaseAuth.currentUser ?: return null
        user.reload().await()
        return firebaseAuth.currentUser?.toData()
    }

    private fun FirebaseUser.toData(): FirebaseAuthUserData =
        FirebaseAuthUserData(
            id = uid,
            email = email,
        )
}
