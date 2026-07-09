package com.aml_sakr.fitlife.core.data.sync

class FakeSyncableDao<T : SyncableEntity<T>> : SyncableDao<T> {
    private val records = mutableMapOf<String, T>()

    override suspend fun getById(id: String): T? = records[id]

    override suspend fun getUnsyncedRecords(): List<T> =
        records.values.filter { it.syncStatus == SyncStatus.NOT_SYNCED }

    override suspend fun update(entity: T) {
        records[entity.syncId] = entity
    }

    fun insert(entity: T) {
        records[entity.syncId] = entity
    }
}
