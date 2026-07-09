package com.aml_sakr.fitlife.core.data.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeSyncTestDao : SyncTestDao {
    private val records = mutableMapOf<String, SyncTestEntity>()

    override suspend fun insert(entity: SyncTestEntity) {
        records[entity.id] = entity
    }

    override suspend fun update(entity: SyncTestEntity) {
        records[entity.id] = entity
    }

    override suspend fun getById(id: String): SyncTestEntity? {
        return records[id]
    }

    override suspend fun getUnsyncedRecords(): List<SyncTestEntity> {
        return records.values.filter { it.syncStatus == SyncStatus.NOT_SYNCED }
    }

    override fun observeUnsyncedCount(): Flow<Int> {
        return flow {
            emit(records.values.count { it.syncStatus == SyncStatus.NOT_SYNCED })
        }
    }

    override suspend fun getAllRecords(): List<SyncTestEntity> {
        return records.values.toList()
    }

    override suspend fun deleteAll() {
        records.clear()
    }
}
