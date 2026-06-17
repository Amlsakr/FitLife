package com.aml_sakr.fitlife.feature.auth.data.repository

internal data class FirebaseOwnedUserDocumentData(
    val path: String,
    val data: Map<String, Any?>
)

internal data class FirebaseOwnedUserDataSnapshot(
    val userDocument: FirebaseOwnedUserDocumentData?,
    val workoutPlans: List<FirebaseOwnedUserDocumentData>,
    val sessions: List<FirebaseOwnedUserDocumentData>,
    val progressDocs: List<FirebaseOwnedUserDocumentData>
)

internal interface FirebaseOwnedUserDataArchiveDataSource {
    suspend fun snapshotUserData(userId: String): FirebaseOwnedUserDataSnapshot

    suspend fun restoreUserData(snapshot: FirebaseOwnedUserDataSnapshot)
}
