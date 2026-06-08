package com.aml_sakr.fitlife.core.data.sync

class FakeRemoteSyncClient : RemoteSyncClient {
    private val records = mutableMapOf<String, SyncTestEntity>()

    override suspend fun getRecord(id: String): SyncTestEntity? {
        return records[id]
    }

    override suspend fun saveRecord(record: SyncTestEntity): Boolean {
        records[record.id] = record.copy(syncStatus = "SYNCED")
        return true
    }

    fun simulateRemoteWrite(record: SyncTestEntity) {
        records[record.id] = record
    }
}
