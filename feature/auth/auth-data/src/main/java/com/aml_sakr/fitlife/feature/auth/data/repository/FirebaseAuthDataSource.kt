package com.aml_sakr.fitlife.feature.auth.data.repository

internal data class FirebaseAuthUserData(
    val id: String,
    val email: String?,
    val isEmailVerified: Boolean
)

internal interface FirebaseAuthDataSource {
    suspend fun createUser(email: String, password: String): FirebaseAuthUserData?

    suspend fun signIn(email: String, password: String): FirebaseAuthUserData?

    suspend fun signInWithGoogle(googleIdToken: String): FirebaseAuthUserData?

    suspend fun sendPasswordResetEmail(email: String)

    fun signOut()

    fun currentUser(): FirebaseAuthUserData?

    suspend fun sendEmailVerification()

    suspend fun deleteCurrentUser()

    suspend fun reloadCurrentUser(): FirebaseAuthUserData?
}

internal data object NoAuthenticatedFirebaseUserException : IllegalStateException()
