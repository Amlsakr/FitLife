package com.aml_sakr.fitlife.core.data.sync

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class FirestoreRemoteSyncClient<T : SyncableEntity<T>>(
    protected val firestore: FirebaseFirestore,
    protected val collectionPath: String
) : RemoteSyncClient<T> {

    protected val collection = firestore.collection(collectionPath)

    override suspend fun getRecord(id: String): T? {
        val snapshot = collection.document(id).get().await()
        return if (snapshot.exists()) {
            mapSnapshotToEntity(snapshot)
        } else {
            null
        }
    }

    override suspend fun saveRecord(record: T): Long? {
        val data = mapEntityToMap(record).toMutableMap()
        data["serverUpdatedAt"] = FieldValue.serverTimestamp()
        collection.document(record.syncId).set(data).await()
        
        // Fetch back to get the server timestamp for reconciliation (AC #4)
        val snapshot = collection.document(record.syncId).get().await()
        return snapshot.getTimestampMillis("serverUpdatedAt") ?: snapshot.getTimestampMillis("lastModified")
    }

    protected abstract fun mapSnapshotToEntity(snapshot: com.google.firebase.firestore.DocumentSnapshot): T?
    protected abstract fun mapEntityToMap(entity: T): Map<String, Any?>

    protected fun com.google.firebase.firestore.DocumentSnapshot.getTimestampMillis(
        field: String
    ): Long? = when (val value = get(field)) {
        is Timestamp -> value.toDate().time
        is java.util.Date -> value.time
        else -> null
    }

    protected suspend fun <R> Task<R>.await(): R = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) {
                return@addOnCompleteListener
            }
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
            }
        }
    }
}
