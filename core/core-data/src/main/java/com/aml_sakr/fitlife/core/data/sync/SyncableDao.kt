package com.aml_sakr.fitlife.core.data.sync

interface SyncableDao<T : SyncableEntity<T>> {
    suspend fun getById(id: String): T?
    suspend fun getUnsyncedRecords(): List<T>
    suspend fun update(entity: T)
}
