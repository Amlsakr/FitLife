package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.feature.auth.data.AuthDataConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseUserDocumentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirebaseUserDocumentDataSource {
    private val usersCollection = firestore.collection(AuthDataConstants.FirestoreCollections.USERS)

    override suspend fun upsertAuthenticatedUser(user: FirebaseAuthUserData) {
        usersCollection.document(user.id)
            .set(
                mapOf(
                    AuthDataConstants.UserDocumentFields.ID to user.id,
                    AuthDataConstants.UserDocumentFields.EMAIL to user.email,
                    AuthDataConstants.UserDocumentFields.IS_EMAIL_VERIFIED to user.isEmailVerified
                ),
                SetOptions.merge()
            )
            .await()
    }
}
