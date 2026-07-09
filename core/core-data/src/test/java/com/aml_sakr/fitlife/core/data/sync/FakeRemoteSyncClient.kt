package com.aml_sakr.fitlife.core.data.sync

class FakeRemoteSyncClient<T : SyncableEntity<T>> : RemoteSyncClient<T> {
    private val records = mutableMapOf<String, T>()
    var nextServerTime: Long? = null

    override suspend fun getRecord(id: String): T? {
        return records[id]
    }

    override suspend fun saveRecord(record: T): Long? {
        val serverTime = nextServerTime ?: record.lastModified
        val syncedRecord = record.withSyncMetadata(SyncStatus.SYNCED, serverTime)
        records[record.syncId] = syncedRecord
        return serverTime
    }

    fun simulateRemoteWrite(record: T) {
        records[record.syncId] = record
    }
}
