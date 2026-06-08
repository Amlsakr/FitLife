package com.aml_sakr.fitlife.core.data.sync

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCancellableCoroutine

class FirestoreRemoteSyncClient(
    private val firestore: FirebaseFirestore
) : RemoteSyncClient {

    private val collection = firestore.collection("sync_test_records")

    override suspend fun getRecord(id: String): SyncTestEntity? {
        return try {
            val snapshot = collection.document(id).get().await()
            if (snapshot.exists()) {
                val payload = snapshot.getString("payload") ?: ""
                val lastModified = snapshot.getLong("lastModified") ?: 0L
                SyncTestEntity(id, payload, lastModified, "SYNCED")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveRecord(record: SyncTestEntity): Boolean {
        return try {
            val data = mapOf(
                "id" to record.id,
                "payload" to record.payload,
                "lastModified" to record.lastModified,
                "syncStatus" to "SYNCED"
            )
            collection.document(record.id).set(data).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
            }
        }
    }
}
