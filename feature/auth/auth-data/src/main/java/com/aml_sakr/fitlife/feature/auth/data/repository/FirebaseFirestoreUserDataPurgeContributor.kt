package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.core.data.purge.UserDataPurgeContributor
import com.aml_sakr.fitlife.feature.auth.data.AuthDataConstants
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreUserDataPurgeContributor @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataPurgeContributor {
    override suspend fun purgeUserData(userId: String) {
        deleteCollection(
            firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                .document(userId)
                .collection(AuthDataConstants.FirestoreCollections.WORKOUT_PLANS)
        )
        deleteCollection(
            firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                .document(userId)
                .collection(AuthDataConstants.FirestoreCollections.SESSIONS)
        )
        deleteProgressDocs(userId)
    }

    private suspend fun deleteCollection(collection: com.google.firebase.firestore.CollectionReference) {
        while (true) {
            val snapshot = collection.limit(PAGE_SIZE.toLong()).get().await()
            if (snapshot.isEmpty) return

            firestore.batch().apply {
                snapshot.documents.forEach { document ->
                    delete(document.reference)
                }
            }.commit().await()
        }
    }

    private suspend fun deleteProgressDocs(userId: String) {
        while (true) {
            val snapshot = firestore.collection(AuthDataConstants.FirestoreCollections.PROGRESS)
                .whereEqualTo(AuthDataConstants.ProgressDocumentFields.USER_ID, userId)
                .limit(PAGE_SIZE.toLong())
                .get()
                .await()
            if (snapshot.isEmpty) return

            firestore.batch().apply {
                snapshot.documents.forEach { document ->
                    delete(document.reference)
                }
            }.commit().await()
        }
    }

    private companion object {
        const val PAGE_SIZE = 500
    }
}
