package com.aml_sakr.fitlife.core.data.sync

interface SyncAgent {
    suspend fun sync(): SyncResult
}

class DefaultSyncAgent<T : SyncableEntity<T>>(
    private val dao: SyncableDao<T>,
    private val remoteClient: RemoteSyncClient<T>
) : SyncAgent {
    override suspend fun sync(): SyncResult {
        var successCount = 0
        var failureCount = 0
        var conflictResolvedCount = 0
        var lastError: String? = null

        val unsynced = dao.getUnsyncedRecords()
        for (staleLocal in unsynced) {
            try {
                val local = dao.getById(staleLocal.syncId) ?: continue
                if (local.syncStatus == SyncStatus.SYNCED) continue

                val remote = remoteClient.getRecord(local.syncId)
                if (remote == null) {
                    val serverTime = remoteClient.saveRecord(local)
                    if (serverTime != null) {
                        dao.update(local.withSyncMetadata(SyncStatus.SYNCED, serverTime))
                        successCount++
                    } else {
                        failureCount++
                    }
                    continue
                }

                when {
                    local.lastModified > remote.lastModified -> {
                        val serverTime = remoteClient.saveRecord(local)
                        if (serverTime != null) {
                            dao.update(local.withSyncMetadata(SyncStatus.SYNCED, serverTime))
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                    local.lastModified < remote.lastModified -> {
                        // Remote is newer, pull down and use remote's timestamp for syncStatus=SYNCED
                        dao.update(remote.withSyncStatus(SyncStatus.SYNCED))
                        conflictResolvedCount++
                        successCount++
                    }
                    else -> {
                        // Identical timestamps, but local is NOT_SYNCED.
                        // We push local and reconcile with server time.
                        val serverTime = remoteClient.saveRecord(local)
                        if (serverTime != null) {
                            dao.update(local.withSyncMetadata(SyncStatus.SYNCED, serverTime))
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                }
            } catch (e: Exception) {
                failureCount++
                lastError = e.message ?: "Unknown error during single record sync"
            }
        }

        return SyncResult(
            success = failureCount == 0,
            successCount = successCount,
            failureCount = failureCount,
            conflictResolvedCount = conflictResolvedCount,
            error = lastError
        )
    }
}
