package com.aml_sakr.fitlife.feature.auth.data.repository

import com.aml_sakr.fitlife.feature.auth.data.AuthDataConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

internal class FirebaseFirestoreOwnedUserDataArchiveDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirebaseOwnedUserDataArchiveDataSource {
    override suspend fun snapshotUserData(userId: String): FirebaseOwnedUserDataSnapshot {
        val userDocument = firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
            .document(userId)
            .get()
            .await()
            .takeIf { it.exists() }
            ?.toOwnedDocument()

        return FirebaseOwnedUserDataSnapshot(
            userDocument = userDocument,
            workoutPlans = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                    .document(userId)
                    .collection(AuthDataConstants.FirestoreCollections.WORKOUT_PLANS)
            ),
            sessions = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                    .document(userId)
                    .collection(AuthDataConstants.FirestoreCollections.SESSIONS)
            ),
            progressDocs = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.PROGRESS)
                    .whereEqualTo(AuthDataConstants.ProgressDocumentFields.USER_ID, userId)
                    .get()
                    .await()
                    .documents
            )
        )
    }

    override suspend fun restoreUserData(snapshot: FirebaseOwnedUserDataSnapshot) {
        snapshot.userDocument?.let { restoreDocument(it) }
        snapshot.workoutPlans.forEach { restoreDocument(it) }
        snapshot.sessions.forEach { restoreDocument(it) }
        snapshot.progressDocs.forEach { restoreDocument(it) }
    }

    private suspend fun snapshotCollection(
        collection: com.google.firebase.firestore.CollectionReference
    ): List<FirebaseOwnedUserDocumentData> =
        collection.get().await().documents.map { it.toOwnedDocument() }

    private suspend fun snapshotCollection(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<FirebaseOwnedUserDocumentData> = documents.map { it.toOwnedDocument() }

    private suspend fun restoreDocument(document: FirebaseOwnedUserDocumentData) {
        val reference = firestore.document(document.path)
        if (!reference.get().await().exists()) {
            reference.set(document.data, SetOptions.merge()).await()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOwnedDocument():
        FirebaseOwnedUserDocumentData =
        FirebaseOwnedUserDocumentData(
            path = reference.path,
            data = requireNotNull(data) {
                "Expected document data for ${reference.path}"
            }
        )
}
