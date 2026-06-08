package com.aml_sakr.fitlife.core.data.sync

interface RemoteSyncClient {
    suspend fun getRecord(id: String): SyncTestEntity?
    suspend fun saveRecord(record: SyncTestEntity): Boolean
}
