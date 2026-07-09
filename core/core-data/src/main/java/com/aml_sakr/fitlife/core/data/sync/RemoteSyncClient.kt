package com.aml_sakr.fitlife.core.data.sync

interface RemoteSyncClient<T : SyncableEntity<T>> {
    suspend fun getRecord(id: String): T?
    suspend fun saveRecord(record: T): Long?
}
