package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncCoordinator(
    private val dao: SyncTestDao,
    private val remoteClient: RemoteSyncClient,
    private val connectivityMonitor: ConnectivityMonitor
) {
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.isConnected()) {
            return@withContext SyncResult(success = false, error = "No connectivity")
        }

        var successCount = 0
        var failureCount = 0
        var conflictResolvedCount = 0

        try {
            val unsynced = dao.getUnsyncedRecords()
            for (local in unsynced) {
                val remote = remoteClient.getRecord(local.id)
                if (remote == null) {
                    // No remote record, just upload local
                    val uploaded = remoteClient.saveRecord(local)
                    if (uploaded) {
                        dao.update(local.copy(syncStatus = "SYNCED"))
                        successCount++
                    } else {
                        failureCount++
                    }
                } else {
                    // Conflict exists! Compare timestamps (latest-timestamp wins)
                    if (local.lastModified >= remote.lastModified) {
                        // Local is newer or equal, overwrite remote
                        val uploaded = remoteClient.saveRecord(local)
                        if (uploaded) {
                            dao.update(local.copy(syncStatus = "SYNCED"))
                            successCount++
                        } else {
                            failureCount++
                        }
                    } else {
                        // Remote is newer, overwrite local Room database
                        dao.update(remote.copy(syncStatus = "SYNCED"))
                        conflictResolvedCount++
                        successCount++
                    }
                }
            }
            SyncResult(
                success = true,
                successCount = successCount,
                failureCount = failureCount,
                conflictResolvedCount = conflictResolvedCount
            )
        } catch (e: Exception) {
            SyncResult(success = false, error = e.message ?: "Unknown error")
        }
    }
}

data class SyncResult(
    val success: Boolean,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val conflictResolvedCount: Int = 0,
    val error: String? = null
)
